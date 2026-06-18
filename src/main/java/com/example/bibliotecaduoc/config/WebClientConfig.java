package com.example.bibliotecaduoc.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Bean("pokeApiWebClient")
    public WebClient pokeApiWebClient(WebClient.Builder builder) {
        return builder.baseUrl("https://pokeapi.co/api/v2").build();
    }

    @Bean("usuariosWebClient")
    public WebClient usuariosWebClient(WebClient.Builder builder,
            @Value("${usuarios.service.url}") String usuariosServiceUrl) {
        return builder.baseUrl(usuariosServiceUrl).build();
    }
}
