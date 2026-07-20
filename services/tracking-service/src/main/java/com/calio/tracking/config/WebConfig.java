package com.calio.tracking.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Exponer la carpeta local /app/images/ como la ruta estática /images/**
        // Para que se acceda en http://tracking-service:8085/images/archivo.jpg
        registry.addResourceHandler("/images/**")
                .addResourceLocations("file:/app/images/");
    }
}
