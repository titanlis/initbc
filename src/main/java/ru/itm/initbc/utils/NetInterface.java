package ru.itm.initbc.utils;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @class NetInterface
 * Сетевой интерфейс, состоящий из имени, IP и MAC
 */
@Data
@AllArgsConstructor
public class NetInterface {
    private String name;
    private String ip;
    private String mac;

    public NetInterface() {
        name = "";
        ip = "0.0.0.0";
        mac = "00:00:00:00:00:00";
    }

//    public NetInterface(String name, String ip, String mac) {
//        this.name = name;
//        this.ip = ip;
//        this.mac = mac;
//    }

//    public String getName() {
//        return name;
//    }
//
//    public void setName(String name) {
//        this.name = name;
//    }
//
//    public String getIp() {
//        return ip;
//    }
//
//    public void setIp(String ip) {
//        this.ip = ip;
//    }
//
//    public String getMac() {
//        return mac;
//    }
//
//    public void setMac(String mac) {
//        this.mac = mac;
//    }
}
