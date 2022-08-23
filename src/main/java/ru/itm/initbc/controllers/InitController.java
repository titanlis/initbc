package ru.itm.initbc.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.itm.initbc.components.BCInfo;
import ru.itm.initbc.utils.NetworkUtils;

@RestController
@RequestMapping("/api/v1/")
public class InitController {
    private static Logger logger = LoggerFactory.getLogger(InitController.class);
    String serialNumber;

    @Autowired
    public InitController(BCInfo bcInfo) {
        serialNumber= bcInfo.getSerialNumber();
        if(serialNumber.equals("none")){
            String os = NetworkUtils.getOSName().toLowerCase();
            logger.info("OS = \'" + os + '\'');

            switch (os){
                case "linux" ->{
                    serialNumber = NetworkUtils.getSystemSerialNumberLinux();
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
    }

    @GetMapping("/getserialnumber")
    public String getSerialNumber(){
        logger.info("serial.number=" + serialNumber);
        return serialNumber;
    }

}
