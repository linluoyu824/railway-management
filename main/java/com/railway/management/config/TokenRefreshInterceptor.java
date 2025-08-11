package com.railway.management.config;

import com.railway.management.common.user.service.TokenCacheService;
import jakarta.servlet.http.HttpServletRequest;
        import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.railway.management.common.user.model.LoginError;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import java.util.HashMap;
@Component
@RequiredArgsConstructor
public class TokenRefreshInterceptor implements HandlerInterceptor {

    private final TokenCacheService tokenCacheService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // Assuming the token is sent in the "Authorization" header as "Bearer <token>"
        String authorizationHeader = request.getHeader("Authorization");
        if (StringUtils.hasText(authorizationHeader) && authorizationHeader.startsWith("Bearer ")) {
            String token = authorizationHeader.substring(7); // Extract the token (remove "Bearer ")
            String username = tokenCacheService.getUsernameByToken(token); //check if the token exists

            if (StringUtils.hasText(username)) {
                // Valid token: Refresh the expiry time
                tokenCacheService.refreshTokenExpiry(token);
            } else {
                // Invalid token: Set a "re-login required" response and stop the request
                response.setStatus(HttpStatus.UNAUTHORIZED.value());
                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                response.setCharacterEncoding("UTF-8");

                // Create a JSON response with the LoginError information
                ObjectMapper objectMapper = new ObjectMapper();
                HashMap<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("code", LoginError.TOKEN_EXPIRED.getCode());
                errorResponse.put("message", LoginError.TOKEN_EXPIRED.getMessage());

                objectMapper.writeValue(response.getWriter(), errorResponse);
                return false; // Stop the request from proceeding
            }
        }
        return true; // Continue processing the request
    }
}
