package gr.atc.modapto.service;

import co.elastic.clients.elasticsearch._types.ElasticsearchException;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.AllArgsConstructor;
import org.modelmapper.MappingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import gr.atc.modapto.exception.CustomExceptions.*;

import java.util.function.Supplier;

@Service
@AllArgsConstructor
public class ExceptionHandlerService {

    private static final Logger logger = LoggerFactory.getLogger(ExceptionHandlerService.class);

    public <T> T handleOperation(Supplier<T> operation, String context) {
        try {
            return operation.get();
        } catch (ElasticsearchException e) {
            logger.error("Elasticsearch error in {}: {}", context, e.getMessage(), e);
            throw new DatabaseException("Database operation failed - Error: " + e.getMessage());
        } catch (MappingException e) {
            logger.error("Mapping error in {}: {}", context, e.getMessage(), e);
            throw new ModelMappingException("Data mapping failed - Error: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error in {}: {}", context, e.getMessage(), e);
            throw new ServiceOperationException("Operation failed - Error: " + e.getMessage());
        }
    }
}
