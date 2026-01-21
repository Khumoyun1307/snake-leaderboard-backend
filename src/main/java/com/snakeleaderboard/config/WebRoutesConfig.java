package com.snakeleaderboard.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebRoutesConfig implements WebMvcConfigurer {

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        // Pretty URLs for static pages (forward keeps URL the same)
        registry.addViewController("/leaderboard").setViewName("forward:/leaderboard/index.html");
        registry.addViewController("/leaderboard/").setViewName("forward:/leaderboard/index.html");

        registry.addViewController("/downloads").setViewName("forward:/downloads/index.html");
        registry.addViewController("/downloads/").setViewName("forward:/downloads/index.html");

        registry.addViewController("/developers").setViewName("forward:/developers/index.html");
        registry.addViewController("/developers/").setViewName("forward:/developers/index.html");

        registry.addViewController("/about").setViewName("forward:/about/index.html");
        registry.addViewController("/about/").setViewName("forward:/about/index.html");
    }
}
