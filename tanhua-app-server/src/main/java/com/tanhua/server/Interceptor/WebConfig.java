package com.tanhua.server.Interceptor;

import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new TokenInterceptor()).
                addPathPatterns("/**").
                excludePathPatterns(new String[]{"/user/login","/user/loginVerification"});
    }
}
