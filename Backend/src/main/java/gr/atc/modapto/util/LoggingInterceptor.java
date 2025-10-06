package gr.atc.modapto.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class LoggingInterceptor implements ClientHttpRequestInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(LoggingInterceptor.class);

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body,
                                        ClientHttpRequestExecution execution) throws IOException {
        logRequest(request, body);
        ClientHttpResponse response = execution.execute(request, body);
        return response;
    }

    private void logRequest(HttpRequest request, byte[] body) {
        logger.debug("=== Request ===");
        logger.debug("URI: {}", request.getURI());
        logger.debug("Method: {}", request.getMethod());
        logger.debug("Headers: {}", request.getHeaders());
        logger.debug("Body: {}", new String(body, StandardCharsets.UTF_8));
    }
}
