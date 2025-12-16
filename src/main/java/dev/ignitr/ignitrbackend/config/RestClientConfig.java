package dev.ignitr.ignitrbackend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {

    @Bean
    public RestClient restClient(
            RestClient.Builder builder,
            @Value("${resttemplate.connect-timeout-ms:5000}") int connectTimeoutMs,
            @Value("${resttemplate.read-timeout-ms:5000}") int readTimeoutMs
    ) {

        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(connectTimeoutMs);
        factory.setReadTimeout(readTimeoutMs);

        return builder
                .requestFactory(factory)
                .build();
    }
}
