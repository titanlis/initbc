package ru.itm.initbc.entity.builders;

import org.springframework.stereotype.Component;
import ru.itm.initbc.entity.BcService;

@Component
public interface BcServiceBuilder {
    BcService build();
}
