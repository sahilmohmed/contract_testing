package com.api.springboot.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class WebSecurityConfig {

  /**
   * Filter chain for the API to ensure the Bearer token is valid
   * 
   * @param http
   * @return
   * @throws Exception
   */
  @Bean
  @Order(2)
  public SecurityFilterChain apiFilterChain(HttpSecurity http) throws Exception {
    http.securityMatcher("/api/**")
        .csrf(AbstractHttpConfigurer::disable)
        .addFilterBefore(new BearerAuthorizationFilter(), UsernamePasswordAuthenticationFilter.class);
    return http.build();
  }

  /**
   * Filter chain for the web ui (API reference)
   * 
   * @param http
   * @return
   * @throws Exception
   */
  @Bean
  @Order(1)
  public SecurityFilterChain webFilterChain(HttpSecurity http) throws Exception {
    {
      http.securityMatcher("/api")
          .csrf(AbstractHttpConfigurer::disable);
      return http.build();
    }
  }

}
