package ru.itm.initbc.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;


@Component
public class Request {
    private static RestTemplate restTemplate = new RestTemplate();
    private static Logger logger = LoggerFactory.getLogger(Request.class);

    public static String get(String url){
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        /**Создаем get запрос и отправляем пары на сервер*/
        try {
            HttpEntity<String> request = new HttpEntity<String>("");
            /**Возвращаeтся серилизованная таблица с именем*/
            ResponseEntity<String> response
                    = restTemplate.getForEntity( url, String.class );
            logger.info("Response. The update came :\t " + response.getBody());
            return response.getBody();
        } catch (ResourceAccessException e) {
            logger.error("Connect exception \'" + url +"\'");
        }
        return null;
    }
}
