package dev.ignitr.ignitrbackend.system.service;

import dev.ignitr.ignitrbackend.common.utils.LoggingUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class SystemServiceImpl implements SystemService {

    private static final Logger logger = LoggerFactory.getLogger(SystemServiceImpl.class);

    public String isOk() {
        LoggingUtils.info(logger, "isOk", null, "Health check OK.");
        return "OK";
    }
}
