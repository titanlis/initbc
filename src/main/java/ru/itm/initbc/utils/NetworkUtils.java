package ru.itm.initbc.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import ru.itm.initbc.controllers.InitController;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @class NetworkUtils
 * @brief статические сетевые утилиты
 */
public class NetworkUtils {

    private static Logger logger = LoggerFactory.getLogger(NetworkUtils.class);

    private static String currentSerial = null;

    @Value("${bk.system_password}")
    private static String systemPassword;

    private final static String zeroIp = "0.0.0.0";
    private final static String zeroMac = "00:00:00:00:00:00";

    public static String getZeroIp() {
        return zeroIp;
    }

    public static String getZeroMac() {
        return zeroMac;
    }



    /**
     * Получить текущее имя операционной системы
     */
    public static String getOSName() {
        return System.getProperty("os.name").toLowerCase();
    }

    /**
     * Находит MAC адрес по IP
     * @param host ip
     * @return mac в формате f0:0c:00:a2:dd:9b
     */
    public static String getMacAddress(String host) {
        String tmpS;
        String sb = "";
        try {
            for(byte b : NetworkInterface.getByInetAddress(InetAddress.getByName(host)).getHardwareAddress()) {
                tmpS = Integer.toHexString(b & 0xFF);
                if(tmpS.length()==1){
                    sb+='0';
                }
                sb+=(tmpS+':');
            }
        } catch (Exception e) {
            return zeroMac;
        }
        return sb.substring(0, sb.length()-1);
    }


    /**
     * Ищет все активные сетевые интерфейсы IPv4. Имя, Ip, Mac
     * @return interfacesList список активных интерфейсов
     */
    public static List<NetInterface> getActiveInterfacesIPv4(){
        /*Пустой список*/
        List<NetInterface> interfacesList = new ArrayList<>();

        /*Находим все интерфейсы, либо в случае проблем с этим, возвращаем пустой список. */
        Enumeration<NetworkInterface> allInterfaces = null;
        try {
            allInterfaces = NetworkInterface.getNetworkInterfaces();
        } catch (SocketException e) {
            return interfacesList;
        }

        /*Проверяем все, что нашли*/
        while (allInterfaces.hasMoreElements()) {

            /*Очередной NetworkInterface из итераторного перечисления*/
            NetworkInterface currentNetworkInterface = allInterfaces.nextElement();

            /* Получение IP-адресов, привязанных к сетевому интерфейсу, обычно только один */
            Enumeration<InetAddress> addresses = currentNetworkInterface.getInetAddresses();

            /*Пройдемся по всем ip текущего интерфейса*/
            while (addresses.hasMoreElements()) {
                InetAddress addr = addresses.nextElement();
                if (addr instanceof Inet4Address) { // Учитывать только адреса IPv4
                    /*Создаем новый ивный интерфейс и добавляем все его данные в list*/
                    interfacesList.add(new NetInterface(currentNetworkInterface.getName(), addr.getHostAddress(),
                            NetworkUtils.getMacAddress(addr.getHostAddress())));
                }
            }
        }

        return interfacesList;
    }

    /**
     * Является ли аргумент именем текущего физического активного сетевого интерфейса?
     * @param nameInterface имя интерфейса
     * @return да или нет
     */
    public static boolean isPhysicalActiveInterface(String nameInterface){
        AtomicBoolean active = new AtomicBoolean(false);
        System.out.println("nameInterface = " + nameInterface);
        NetworkUtils.getActiveInterfacesIPv4().stream().filter(nI -> nI.getName().equals(nameInterface)).
                forEach(nI->{
                   if(!nI.getMac().equals(zeroMac)) active.set(true);
                });
        return active.get();
    }

    /**
     * Среди активных интерфейсов находим нужный (по имени аргумента) и возвращаем его IP.
     * Если подключения нет или интерфейс не найден, то возвращаем ip == 0.0.0.0
     * @param nameInterface
     * @return
     */
    public static String getIpFromInterfacesName(String nameInterface){
        AtomicReference<String> ip = new AtomicReference<>(zeroIp);
        NetworkUtils.getActiveInterfacesIPv4().stream().filter(nI -> nI.getName().equals(nameInterface)).
                forEach(nI->{
                    if(!nI.getMac().equals(zeroMac)) {
                        ip.set(nI.getIp());
                        return;
                    }
                });
        return ip.get();
    }


    /**
     * Существует ли интерфейс с ip адесом из строчного аргумента?
     * Среди всех активных интерфейсов Ip4 отфильтровываем Ip эквивалентные аргументу,
     * находим все и смотрим найдены ли они. Если найдены, то существует.
     * @param ip адрес по которому ищем интерфейс
     * @return true|false в зависимости от результата поиска
     */
    public static boolean isIpSetToAnyInterface(String ip){
        return NetworkUtils.getActiveInterfacesIPv4().stream().filter(nI -> nI.getIp().equals(ip)).findAny().isPresent();
    }

    /**
     * Получаем серийный номер компа на debian/ubuntu
     * Если серийника нет, то возвращает "" пустую строку
     * @return currentSerial
     */
    public static String getSystemSerialNumberLinux() {
        currentSerial = "";
        Process p = null;
        try {
            p = Runtime.getRuntime().exec(new String[] {"/bin/bash","-c","echo " + systemPassword + " | sudo -S dmidecode -s system-serial-number"});
            try (BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));) {
                String s = null;
                while ((s = br.readLine()) != null) {
                    p.waitFor();
                    if (!s.equals(" ")) {
                        currentSerial = s;
                        //System.out.println("currentSerial" + currentSerial);
                        return currentSerial;
                    }else {
                        break;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (p != null) {
                p.destroy();
            }
        }

        return currentSerial;
    }

    /**
     * Получаем серийный номер компа на windows 10
     * Если серийника нет, то возвращает "System Serial Number"
     * @return currentSerial
     */
    public static String getSystemSerialNumberWindows() {
        try {
            Process p = Runtime.getRuntime().exec("powershell Get-WmiObject -Class Win32_BIOS");
            BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line;
            while ((line = input.readLine()) != null) {
                String str[] = line.split(":");
                if(str.length==2){
                    if(str[0].trim().equals("SerialNumber")){
                        currentSerial = str[1].trim();
                        break;
                    }
                }
            }
            input.close();
        }
        catch(Exception e){
            e.printStackTrace();
        }
        return currentSerial;
    }

}
