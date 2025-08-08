package gr.atc.modapto.service.interfaces;

import gr.atc.modapto.dto.ModaptoModuleDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface IModaptoModuleService {
    Page<ModaptoModuleDto> retrieveAllModulesPaginated(Pageable pageable);

    List<ModaptoModuleDto> retrieveAllModules();

    ModaptoModuleDto retrieveModuleByModuleId(String moduleId);

    List<ModaptoModuleDto.SmartServiceDto> retrieveSmartServicesByModuleId(String moduleId);

    String retrieveSmartServiceUrl(String moduleId, String smartServiceId);
}
