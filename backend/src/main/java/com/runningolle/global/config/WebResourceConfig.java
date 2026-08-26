package com.runningolle.global.config;

import java.nio.file.Path;
import java.nio.file.Paths;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebResourceConfig implements WebMvcConfigurer {

    private final String publicBasePath;
    private final Path localDir;

    public WebResourceConfig(
            @Value("${app.storage.local-dir:uploads}") String localDir,
            @Value("${app.storage.public-base-path:/uploads}") String publicBasePath
    ) {
        this.localDir = Paths.get(localDir).toAbsolutePath().normalize();
        this.publicBasePath = publicBasePath.startsWith("/") ? publicBasePath : "/" + publicBasePath;
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler(publicBasePath + "/**")
                .addResourceLocations(localDir.toUri().toString());
    }
}
