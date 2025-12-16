package dev.ignitr.ignitrbackend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {

    @Bean
    public RestClient scoringRestClient(
            @Value("${ignitr.scorer.url:http://localhost:3000/api}") String scorerUrl,
            @Value("${restclient.connect-timeout-ms:5000}") int connectTimeoutMs,
            @Value("${restclient.read-timeout-ms:5000}") int readTimeoutMs
    ) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(connectTimeoutMs);
        factory.setReadTimeout(readTimeoutMs);

        return RestClient.builder()
                .baseUrl(scorerUrl)
                .requestFactory(factory)
                .build();
    }
}