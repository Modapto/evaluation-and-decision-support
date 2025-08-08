package gr.atc.modapto.service.interfaces;

import gr.atc.modapto.dto.dt.DtResponseDto;
import org.springframework.http.ResponseEntity;

/**
 * Generic interface for processing DTM service responses
 * Supports both active processing and no-op (ignore) processing
 * For no-op processors, simply return null to indicate response was ignored
 *
 * @param <T> The expected output DTO type (can be Object for no-op processors)
 */
public interface IResponseProcessor<T> {
    
    /**
     * Process the DTM response and convert it to the expected output type
     * May return null if response processing is not required
     *
     * @param response DTM response from smart service invocation
     * @param moduleId Module identifier
     * @param smartServiceId Smart service identifier
     * @return Processed output DTO or null if processing not needed
     */
    T processResponse(ResponseEntity<DtResponseDto> response, String moduleId, String smartServiceId);
}