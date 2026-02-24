package com.greateastern.warehouse.config;

import org.springframework.beans.BeanUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration(proxyBeanMethods = false)
public class SpringDocCompatibilityConfiguration {

  @Bean
  @Primary
  @ConditionalOnMissingBean(org.springframework.boot.webmvc.autoconfigure.WebMvcProperties.class)
  public org.springframework.boot.webmvc.autoconfigure.WebMvcProperties springDocWebMvcProperties(
      org.springframework.boot.autoconfigure.web.servlet.WebMvcProperties webMvcProperties) {
    org.springframework.boot.webmvc.autoconfigure.WebMvcProperties compatibleProperties =
        new org.springframework.boot.webmvc.autoconfigure.WebMvcProperties();
    BeanUtils.copyProperties(webMvcProperties, compatibleProperties);
    return compatibleProperties;
  }
}
