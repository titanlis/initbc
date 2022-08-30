package ru.itm.initbc.config;

import ru.itm.initbc.entity.BcService;
import ru.itm.initbc.utils.OSType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SystemConfig {
    private static OSType osType = OSType.UNKNOWN;
    private static boolean isRegisterInServer = false;  //получен ли ip с сервера
    private static List<BcService> processInWork = new ArrayList<>();    //сервис

    public static boolean isIsRegisterInServer() {
        return isRegisterInServer;
    }

    public static void setIsRegisterInServer(boolean isRegisterInServer) {
        SystemConfig.isRegisterInServer = isRegisterInServer;
    }

    public static List<BcService> getProcessInWork() {
        return processInWork;
    }

    public static void setProcessInWork(List<BcService> processInWork) {
        SystemConfig.processInWork = processInWork;
    }

    public static OSType getOsType() {
        return osType;
    }

    public static void setOsType(OSType osType) {
        SystemConfig.osType = osType;
    }

    public static void setOsType(String osTypeStr) {
        switch (osTypeStr.toLowerCase()){
            case "linux" -> { osType = OSType.LINUX; }
            case "windows 10" -> { osType = OSType.WINDOWS; }
            default -> { osType = OSType.UNKNOWN; }
        }
    }
}
