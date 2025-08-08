package gr.atc.modapto.service.processors;

import gr.atc.modapto.dto.dt.DtResponseDto;
import gr.atc.modapto.service.interfaces.IResponseProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

/**
 * No-op response processor for services where response processing is not required
 * To be Used for services where we only care about the invocation, not the response
 */
@Component
public class NoOpResponseProcessor implements IResponseProcessor<Object> {

    private final Logger logger = LoggerFactory.getLogger(NoOpResponseProcessor.class);

    @Override
    public Object processResponse(ResponseEntity<DtResponseDto> response, String moduleId, String smartServiceId) {
        logger.debug("Response processing skipped for service: {} in module: {} - response not relevant", 
            smartServiceId, moduleId);

        // Ignore the results and return null
        return null;
    }
}