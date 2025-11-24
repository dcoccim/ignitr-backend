package dev.ignitr.ignitrbackend.health.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class HealthServiceImpl implements HealthService{

    private static final Logger log = LoggerFactory.getLogger(HealthServiceImpl.class);

    public String isOk() {
        log.info("Requested Health OK.");
        return "OK";
    }
}
