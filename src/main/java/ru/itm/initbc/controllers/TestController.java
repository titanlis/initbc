package ru.itm.initbc.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.itm.initbc.components.BCInfo;
import ru.itm.initbc.utils.NetworkUtils;

import java.util.List;

@RestController
@RequestMapping("/test")
public class TestController {

    private BCInfo bcInfo;

    @Autowired
    public TestController(BCInfo bcInfo) {
        this.bcInfo = bcInfo;
    }



    private static Logger logger = LoggerFactory.getLogger(TestController.class);

    @GetMapping("/1")
    public String oneTest(){
        String s = bcInfo.getSerialNumber();
        logger.info("serial.number=" + s);
        return s;
    }

    @GetMapping("/bcdb/isup")
    public void stopPage(){

    }

}
