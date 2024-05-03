package com.pser.auction.config;

import com.pser.auction.filter.CamelCaseFilter;
import jakarta.servlet.Filter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class FilterConfig implements WebMvcConfigurer {
    @Bean
    public FilterRegistrationBean<Filter> filterRegistrationBean() {
        FilterRegistrationBean<Filter> filterBean = new FilterRegistrationBean<>(new CamelCaseFilter());
        filterBean.addUrlPatterns("/*");
        return filterBean;
    }
}
