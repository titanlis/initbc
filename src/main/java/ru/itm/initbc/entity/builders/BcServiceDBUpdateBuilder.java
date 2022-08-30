package ru.itm.initbc.entity.builders;

import lombok.NoArgsConstructor;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.itm.initbc.entity.BcService;
@Component
public class BcServiceDBUpdateBuilder implements BcServiceBuilder{
    private BcService bcService;

    @Value("${service_path}")
    private String path;

    @Value("${dbupdate.port}")
    private int port;

    @Value("${dbupdate.actuator.port}")
    private int actuatorPort;

    @Value("${dbupdate.jar}")
    private String jarName;

    public BcServiceDBUpdateBuilder() {
        bcService = new BcService();
    }

    @Override
    public BcService build(){
        bcService.setJarName(jarName);
        bcService.setPort(port);
        bcService.setActuatorPort(actuatorPort);
        bcService.setUrl(path);
        return bcService;
    }

}
