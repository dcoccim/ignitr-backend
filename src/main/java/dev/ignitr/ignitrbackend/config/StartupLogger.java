package dev.ignitr.ignitrbackend.config;

import dev.ignitr.ignitrbackend.common.utils.LoggingUtils;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StartupLogger {

    private final static Logger logger = LoggerFactory.getLogger(StartupLogger.class);

    private final Environment env;

    @Value("${spring.mongodb.uri:undefined}")
    private String mongoUri;

    @Value("${ignitr.scorer.url}")
    private String scorerUrl;

    @PostConstruct
    public void logConfig() {
        LoggingUtils.info(logger, "logConfig", "", "Current profile(s) active: {}", String.join(", ", env.getActiveProfiles()));
        LoggingUtils.info(logger, "logConfig", "", "MongoDB URI: {}", mongoUri);
        LoggingUtils.info(logger, "logConfig", "", "Scoring Service Base URL: {}", scorerUrl);
    }
}
