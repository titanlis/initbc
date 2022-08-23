package ru.itm.initbc.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Service;
import ru.itm.initbc.components.BCInfo;
import ru.itm.initbc.components.BCInfoImpl;

@Configuration
public class InfoService {
    @Bean
    public BCInfo bcInfoFactory(){
        return new BCInfoImpl();
    }

}
