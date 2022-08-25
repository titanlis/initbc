package ru.itm.initbc.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/test")
public class TestController {


    @Autowired
    public TestController() {

    }

    private static Logger logger = LoggerFactory.getLogger(TestController.class);

    @GetMapping("/1")
    public String oneTest(){
        return "none";
    }

    @GetMapping("/bcdb/isup")
    public void stopPage(){

    }

}
