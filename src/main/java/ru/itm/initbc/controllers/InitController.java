package ru.itm.initbc.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.itm.initbc.config.SystemConfig;
import ru.itm.initbc.entity.Interface;
import ru.itm.initbc.entity.SerialNumber;
import ru.itm.initbc.repository.InterfaceRepository;
import ru.itm.initbc.repository.SerialNumberRepository;
import ru.itm.initbc.utils.NetInterface;
import ru.itm.initbc.utils.NetworkUtils;
import ru.itm.initbc.utils.Request;

import java.util.List;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/v1/")
public class InitController {
    private static Logger logger = LoggerFactory.getLogger(InitController.class);

    @Value("${serial.number}")
    private String serialNumber;

    @Value("${bk.system_password}")
    private String systemPassword;

    @Value("${server.main_server_name}")
    private String serverUrl;

    @Value("${server.main_server_port}")
    private String serverPort;

    private SerialNumberRepository serialNumberRepository;
    private InterfaceRepository interfaceRepository;

    @Autowired
    public InitController(SerialNumberRepository serialNumberRepository, InterfaceRepository interfaceRepository) {
        this.serialNumberRepository = serialNumberRepository;
        this.interfaceRepository = interfaceRepository;
    }

    @GetMapping("/getserialnumber")
    public String getSerialNumber(){
        logger.info("serial.number=" + serialNumber);
        return serialNumber;
    }

    /**
     * Автозапуск после создания контекста
     */
    @EventListener(ApplicationReadyEvent.class)
    private void startIni(){
        /* Серийный номер "none" бывает в том случае, когда в командной строке ничего не пришло.
         * В этом случае запускается процесс получения sn из биоса.*/
        if(serialNumber.equals("none")){
            String os = NetworkUtils.getOSName().toLowerCase(); //получаем имя операционки
            logger.info("OS = \'" + os + '\'');

            /*Для каждой ОС свой метод считывания sn из биоса. */
            switch (os){
                case "linux" ->{
                    serialNumber = NetworkUtils.getSystemSerialNumberLinux(systemPassword);
                }
                case "windows 10" ->{
                    serialNumber = NetworkUtils.getSystemSerialNumberWindows();
                }
                default -> {
                    throw new RuntimeException("A command line argument is required : --serial.number=111111");
                }
            }
        }
        logger.info("InitController.serial.number=\'" + serialNumber + '\'');

        /*Если sn получен, то пишем его в таблицу "serialnumbers"*/
        serialNumberRepository.save(new SerialNumber(serialNumber));
        /* Запрвшиваем с сервера ip, соответствующий нашему серийнику.
         * Если в базе серийника нет, то через 5 сек повторяем запрос. Цикл бесконечный. Пока не получим ip или
         * пока не выключат сервис.*/
        do {
            String ip = Request.get("http://" + serverUrl + ":" + serverPort + "/api/v1/init/" + serialNumber + "/getip");
            /*Если на сервере нет нашего серийника. то ip==null, если не нулл, то запускаем обработку ip*/
            if(ip!=null){
                //interfaceRepository.deleteAll();
                /*Получаем список активных сетевых интерфейсов, включая виртуальные*/
                List<NetInterface> netInterfaceList = NetworkUtils.getActiveInterfacesIPv4();

                /*Создаем сущность Interface для записи в h2 базу*/
                Interface inter = new Interface();
                inter.setId(0L);

                /*Для каждого интерфейса находим имя, mac, ip  */
                netInterfaceList.stream().forEach(netInterface -> {
                    inter.setName(netInterface.getName());
                    inter.setIp(NetworkUtils.getIpFromInterfacesName(inter.getName()));
                    inter.setMac(NetworkUtils.getMacAddress(inter.getIp()));
                    inter.setPriority(2);
                    inter.setActive(false);

                    /*Если ip совпал с тем, что был вписан в базу на сервере, значит этот интерфейс активный*/
                    if(netInterface.getIp().equals(ip)){
                        inter.setPriority(1);       //максимальный приоритет ему за это
                        inter.setActive(true);      //активный
                        inter.setIp(ip);            //ip из базы (на случай vpn, где он не совпадет с физическим)
                    }
                    interfaceRepository.save(inter);    //пишем в таблицу interface
                });
                SystemConfig.setIsRegisterInServer(true);   //инициализация пройдена
            }
            else{
                /*Если сервер возвращает вместо ip null, спим 5 сек, затем повторяем запрос.*/
                logger.info("The serial number \'" + serialNumber + "\' was not found in the database.");
                try {
                    TimeUnit.SECONDS.sleep(5L);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

        }while (!SystemConfig.isIsRegisterInServer());
        logger.info("The on-board computer has just been connected to the server.");
    }

}
