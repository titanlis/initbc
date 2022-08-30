package ru.itm.initbc.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.ResourceAccessException;
import ru.itm.initbc.config.SystemConfig;
import ru.itm.initbc.entity.BcService;
import ru.itm.initbc.entity.Interface;
import ru.itm.initbc.entity.MessageInterface;
import ru.itm.initbc.entity.SerialNumber;
import ru.itm.initbc.entity.builders.BcServiceBuilder;
import ru.itm.initbc.entity.builders.BcServiceDBUpdateBuilder;
import ru.itm.initbc.repository.InterfaceRepository;
import ru.itm.initbc.repository.SerialNumberRepository;
import ru.itm.initbc.utils.NetInterface;
import ru.itm.initbc.utils.NetworkUtils;
import ru.itm.initbc.utils.Request;

import java.io.IOException;
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

    private BcServiceBuilder bcServiceDBUpdateBuilder;

    @Autowired
    public void setBcServiceDbUpdateBuilder(@Qualifier("bcServiceDBUpdateBuilder") BcServiceBuilder bcServiceDBUpdateBuilder) {
        this.bcServiceDBUpdateBuilder = bcServiceDBUpdateBuilder;
    }

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
        String os = NetworkUtils.getOSName().toLowerCase(); //получаем имя операционки
        logger.info("OS = \'" + os + '\'');
        SystemConfig.setOsType(os);

        /* Серийный номер "none" бывает в том случае, когда в командной строке ничего не пришло.
         * В этом случае запускается процесс получения sn из биоса.*/
        if(serialNumber.equals("none")){
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
        /* Запрвшиваем с сервера ip и mac, соответствующий нашему серийнику.
         * Если в базе серийника нет, то через 5 сек повторяем запрос. Цикл бесконечный. Пока не получим ip или
         * пока не выключат сервис.*/
        do {
            try{
                MessageInterface messageInterface = Request.get("http://" + serverUrl + ":" + serverPort + "/api/v1/init/" + serialNumber + "/getip");

                /*Если на сервере нет нашего серийника. то ip==null, если не нулл, то запускаем обработку ip*/
                if(messageInterface.getIp()!=null){
                    //interfaceRepository.deleteAll();
                    /*Получаем список активных сетевых интерфейсов, включая виртуальные*/
                    List<NetInterface> netInterfaceList = NetworkUtils.getActiveInterfacesIPv4();

                    /*Создаем сущность Interface для записи в h2 базу*/
                    Interface inter = new Interface();
                    inter.setId(0L);

//                    try {
//                        interfaceRepository.deleteAll();
//                    }catch (DataIntegrityViolationException ex){
//                        logger.info("No interfaces table");
//                    }

                    /*Для каждого интерфейса находим имя, mac, ip  */
                    netInterfaceList.stream().forEach(netInterface -> {
                        inter.setName(netInterface.getName());
                        inter.setIp(NetworkUtils.getIpFromInterfacesName(inter.getName()));
                        inter.setMac(NetworkUtils.getMacAddress(inter.getIp()));
                        inter.setPriority(2);
                        inter.setActive(false);
                        inter.setIp_vpn(inter.getIp());

                        /**Если физ.мак соответствует маку с сервера, значит через этот интерфейс идет связь*/
                        if(messageInterface.getMac().toLowerCase().equals(inter.getMac().toLowerCase())){
                            inter.setPriority(1);       //максимальный приоритет ему за это
                            inter.setActive(true);      //активный
                            /**ip в базе сервера может не совпадать с ip интерфейса, это значит, что работаем с vpn
                             *  и ip передачи (vpn) - это ip туннеля, а не интерфейса*/
                            inter.setIp_vpn(messageInterface.getIp());
                        }
                        interfaceRepository.save(inter);    //пишем в таблицу interface
                    });
                    SystemConfig.setIsRegisterInServer(true);   //инициализация пройдена
                    interfaceRepository.flush();
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
            } catch (ResourceAccessException e) {
                logger.error("Connect exception \'" + "http://" + serverUrl + ":" + serverPort + "/api/v1/init/" + serialNumber + "/getip" +"\'");
                try {
                    TimeUnit.SECONDS.sleep(10L);
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }


        }while (!SystemConfig.isIsRegisterInServer());
        logger.info("The on-board computer has just been connected to the server.");
        startServices();
    }

    /**
     * Запуск сервисов и запуск мониторинга сервисов
     */
    private void startServices() {
        try {
            BcService bcService = bcServiceDBUpdateBuilder.build();
            if(bcService.start()){
                logger.info("Service " + bcService.getJarName() + " is start.");
                SystemConfig.getProcessInWork().add(bcService);
            }
            else{
                logger.error("Service " + bcService.getJarName() + " is not start.");
                exit();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void exit() {
        new ShutdownManager().stopPage();
    }

    public static void stopServices() {
        logger.info("Total process : " + SystemConfig.getProcessInWork().size());
        SystemConfig.getProcessInWork().stream().forEach(bcService -> {
            logger.info("Stopped: " + bcService.getJarName());
            bcService.stop();
        });
        logger.info("Services was stop.");
    }

}
