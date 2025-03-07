package gr.atc.modapto.filter;

import io.github.bucket4j.Bucket;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RateLimitingFilterTests {

    @Mock
    private Bucket bucket;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private RateLimitingFilter rateLimitingFilter;

    @BeforeEach
    void setUp() {
        // Mock bucket consumption behavior
        lenient().when(bucket.tryConsume(1)).thenReturn(true);
    }

    @DisplayName("Rate Limiting Filter: Success")
    @Test
    void givenRequestWithinLimit_whenDoFilter_thenContinueFilterChain() throws ServletException, IOException {
        // Execute filter
        rateLimitingFilter.doFilterInternal(request, response, filterChain);

        // Verify that the request is forwarded to the next filter
        verify(filterChain, times(1)).doFilter(request, response);
    }
}
