package gr.atc.modapto.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import gr.atc.modapto.controller.BaseResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class UnauthorizedEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException {
       // Check if the Path is excluded from Unauthorized handling
        String requestPath = request.getRequestURI();
        if (isExcludedPath(requestPath, request.getMethod())) {
            return;
        }

        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType("application/json");

        // Check the validity of the token
        String errorMessage = "Unauthorized request. Check token and try again.";
        String errorCode = "Invalid or missing Token";

        if (authException instanceof OAuth2AuthenticationException) {
            errorMessage = "Invalid JWT provided.";
            errorCode = "JWT has expired or is invalid";
        }

        BaseResponse<String> responseMessage = BaseResponse.error(errorMessage, errorCode);

        ObjectMapper mapper = new ObjectMapper();
        mapper.findAndRegisterModules();
        mapper.writeValue(response.getWriter(), responseMessage);
        
        response.getWriter().flush();
    }

    private boolean isExcludedPath(String path, String method) {
        // Define paths to exclude from unauthorized handling
        return path.equals("/api/eds/swagger") ||
                path.equals("/api/eds/swagger-ui/**") ||
                path.equals("/api/eds/v3/api-docs") ||
                path.equals("/eds/websocket/**") ||
                method.equals(HttpMethod.OPTIONS.toString());
    }
}