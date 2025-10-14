package gr.atc.modapto.service.interfaces;

import gr.atc.modapto.dto.crf.CrfKitHolderEventDto;
import gr.atc.modapto.dto.crf.CrfSelfAwarenessParametersDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface ICrfSelfAwarenessService {
    void invokeKhSelfAwareness(MultipartFile eventsFile, CrfSelfAwarenessParametersDto parameters) throws IOException;

    Page<CrfKitHolderEventDto> retrievePaginatedKhEventResultsPaginated(Pageable pageable);

    Page<CrfKitHolderEventDto> retrievePaginatedKhEventResultsByModule(String moduleId, Pageable pageable);

    void registerKitHolderEvent(CrfKitHolderEventDto event);
}
