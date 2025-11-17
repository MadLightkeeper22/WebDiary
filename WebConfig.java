package com.springboot.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${file.upload-dir}")
    private String uploadDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 예: /images/xxx.jpg → 실제로는 {프로젝트실행폴더}/uploads/xxx.jpg 를 보게 함
        String absolutePath = "file:" + System.getProperty("user.dir") + "/" + uploadDir + "/";
        registry.addResourceHandler("/images/**")
                .addResourceLocations(absolutePath);
    }
}
