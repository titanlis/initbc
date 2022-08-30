package ru.itm.initbc.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.ResourceAccessException;
import ru.itm.initbc.config.SystemConfig;
import ru.itm.initbc.utils.OSType;
import ru.itm.initbc.utils.Request;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Данные о сервисе. О процессе.
 */
@Data
@NoArgsConstructor
public class BcService {
    private static Logger logger = LoggerFactory.getLogger(BcService.class);
    private String jarName = null;
    private String url = "localhost";
    private int port = 0;
    private int actuatorPort = 0;
    private ProcessBuilder pb = null;
    private ExecutorService service = null;
    private Process process = null;

    public boolean start() throws IOException {
        List<String> command = new ArrayList<String>();
        if(SystemConfig.getOsType().equals(OSType.LINUX)) {
            command.add("/bin/bash");
            command.add("-c");
        } else if (SystemConfig.getOsType().equals(OSType.WINDOWS)) {
            command.add("cmd");
            command.add("/c");
        } else {
            return false;
        }
        command.add("java -DSERVER_PORT=" + port + "-DMANAGEMENT_SERVER_PORT=" + actuatorPort + " -jar " + jarName);
        System.out.println( "java -DSERVER_PORT=" + port + "-DMANAGEMENT_SERVER_PORT=" + actuatorPort + " -jar " + jarName);

        pb = new ProcessBuilder(command);
        process = pb.start();

        // читать вывод асинхронно
        InputStream inputStream = process.getInputStream();
        InputStream errorStream = process.getErrorStream();

        service = Executors.newFixedThreadPool( 2 );

        ResultStreamHandler inputStreamHandler = new ResultStreamHandler( inputStream );
        ResultStreamHandler errorStreamHandler = new ResultStreamHandler( errorStream );

        service.execute( inputStreamHandler );
        service.execute( errorStreamHandler );
        boolean b=false;
        for(int i=0; i<5 && !(b=isActive()); i++){
            try {
                TimeUnit.SECONDS.sleep(10L);
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        }

        return b;
    }

    public void stop(){
        Request.getString("http://" + url + ":" + port + "/exit");
        logger.info("http://" + url + ":" + port + "/exit");
        if (service != null) {
            service.shutdownNow();
        }
    }

    public boolean isActive(){
        try{
            MessageStatus messageStatus = Request.getMessageStatus("http://" + url + ":" + actuatorPort + "/actuator/health");
            return messageStatus.getStatus().toLowerCase().equals("up");
        }catch (ResourceAccessException e){}
        return false;
    }
}
