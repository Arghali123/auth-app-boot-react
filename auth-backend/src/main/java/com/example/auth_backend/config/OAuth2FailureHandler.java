package com.example.auth_backend.config;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Component
public class OAuth2FailureHandler implements AuthenticationFailureHandler {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Value("${security.app.auth.frontend.failure-redirect}")
    private String frontEndFailureUrl;

    @Override
    public void onAuthenticationFailure(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException exception
    ) throws IOException, ServletException {
        logger.warn("OAuth2 authentication failed: {}", exception.getMessage());

        String errorMessage = exception.getMessage() == null
                ? "OAuth2 authentication failed"
                : exception.getMessage();

        response.sendRedirect(buildFailureRedirectUrl(errorMessage));
    }

    private String buildFailureRedirectUrl(String errorMessage) {
        String separator = frontEndFailureUrl.contains("?") ? "&" : "?";
        String encodedMessage = URLEncoder.encode(errorMessage, StandardCharsets.UTF_8);
        return frontEndFailureUrl + separator + "error=" + encodedMessage;
    }
}
