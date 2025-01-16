package com.api.springboot.config;

import org.apache.commons.lang3.StringUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Base64;

@SuppressWarnings("null")
public class BearerAuthorizationFilter extends OncePerRequestFilter {

  public static final long ONE_HOUR = 60 * 60 * 1000L;

  /**
   * Override the doFilterInternal method to set the authentication if the Bearer
   * token is valid
   */
  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {
    String header = request.getHeader("Authorization");
    if (tokenValid(header)) {
      SecurityContextHolder.getContext().setAuthentication(new PreAuthenticatedAuthenticationToken("user", header));
      filterChain.doFilter(request, response);
    } else {
      response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    }
  }

  /**
   * Method to check if the Bearer token is valid, the token is decoded into a
   * timestamp and then the time stamp is used to ensure the token isnt over one
   * hour old
   * 
   * @param header
   * @return
   */
  private boolean tokenValid(String header) {
    boolean hasBearerToken = StringUtils.isNotEmpty(header) && header.startsWith("Bearer ");
    if (hasBearerToken) {
      String token = header.substring("Bearer ".length());
      ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
      buffer.put(Base64.getDecoder().decode(token));
      buffer.flip();
      long timestamp = buffer.getLong();
      return System.currentTimeMillis() - timestamp <= ONE_HOUR;
    } else {
      return false;
    }
  }
}
