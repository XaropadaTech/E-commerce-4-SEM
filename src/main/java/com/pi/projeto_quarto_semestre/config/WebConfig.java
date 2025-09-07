package com.pi.projeto_quarto_semestre.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String pastaImagens = System.getProperty("user.dir") + "/imagens_upload/";
        registry.addResourceHandler("/imagens/**")
                .addResourceLocations("file:" + pastaImagens);
    }
}