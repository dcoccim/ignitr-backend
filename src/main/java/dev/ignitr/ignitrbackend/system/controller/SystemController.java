package dev.ignitr.ignitrbackend.system.controller;

import dev.ignitr.ignitrbackend.system.service.SystemService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/health")
public class SystemController {

    private final SystemService systemService;

    public SystemController(SystemService systemService){
        this.systemService = systemService;
    }

    @GetMapping(produces = MediaType.TEXT_PLAIN_VALUE)
    public String health() {
        return systemService.isOk();
    }
}
