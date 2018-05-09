package org.spring.springboot.conf;


import org.spring.springboot.interceptor.ProcessInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.web.servlet.config.annotation.*;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

/**
 * Created by xl on 2017/12/7.
 */
@Configuration
public class WebMvcConfig extends WebMvcConfigurerAdapter {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new ProcessInterceptor());
//        registry.addInterceptor(new AuthorizationInterceptor())
//                .addPathPatterns("/api/**")
//                .excludePathPatterns("/api/admin/**");
    }


}
