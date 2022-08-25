package ru.itm.initbc.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

public class SystemConfig {
    private static boolean isRegisterInServer = false;  //получен ли ip с сервера

    public static boolean isIsRegisterInServer() {
        return isRegisterInServer;
    }

    public static void setIsRegisterInServer(boolean isRegisterInServer) {
        SystemConfig.isRegisterInServer = isRegisterInServer;
    }

    public static void clearStop(){
    }

}
