package dev.ignitr.ignitrbackend.health.controller;

import dev.ignitr.ignitrbackend.health.service.HealthService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/health")
public class HealthController {

    private final HealthService healthService;

    public HealthController(HealthService healthService){
        this.healthService = healthService;
    }

    @GetMapping(produces = MediaType.TEXT_PLAIN_VALUE)
    public String health() {
        return healthService.isOk();
    }
}
