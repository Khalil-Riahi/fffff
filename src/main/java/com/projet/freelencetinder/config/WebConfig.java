// src/main/java/com/projet/freelencetinder/config/WebConfig.java
package com.projet.freelencetinder.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.*;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry reg) {
        reg.addResourceHandler("/uploads/**")
           .addResourceLocations("file:uploads/");   // chemin local
    }
}