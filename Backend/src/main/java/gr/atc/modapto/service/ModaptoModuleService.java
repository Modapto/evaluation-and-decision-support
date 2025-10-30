package gr.atc.modapto.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.modelmapper.MappingException;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import gr.atc.modapto.dto.ModaptoModuleDto;
import gr.atc.modapto.dto.sew.DeclarationOfWorkDto;
import gr.atc.modapto.exception.CustomExceptions.ModelMappingException;
import gr.atc.modapto.exception.CustomExceptions.ResourceNotFoundException;
import gr.atc.modapto.model.ModaptoModule;
import gr.atc.modapto.repository.ModaptoModuleRepository;
import gr.atc.modapto.service.interfaces.IModaptoModuleService;

@Service
public class ModaptoModuleService implements IModaptoModuleService {

    private final Logger logger = LoggerFactory.getLogger(ModaptoModuleService.class);

    private final ModaptoModuleRepository modaptoModuleRepository;
    
    private final ModelMapper modelMapper;
    
    private static final String MAPPING_ERROR = "Unable to parse MODAPTO Module to DTO or vice-versa - Error: ";

    public ModaptoModuleService(ModaptoModuleRepository modaptoModuleRepository, ModelMapper modelMapper) {
        this.modaptoModuleRepository = modaptoModuleRepository;
        this.modelMapper = modelMapper;
    }

    /**
     * Retrieve smart service URL by moduleId and smartServiceId
     *
     * @param moduleId The module identifier
     * @param smartServiceId The smart service identifier (serviceId from SmartService)
     * @throws ResourceNotFoundException When the requested resource is not present
     * @return The URL of the smart service
     */
    @Override
    public String retrieveSmartServiceUrl(String moduleId, String smartServiceId) {
        logger.debug("Retrieving smart service URL for moduleId: {} and smartServiceId: {}", moduleId, smartServiceId);

        // Find the module by moduleId
        ModaptoModule module = modaptoModuleRepository.findByModuleId(moduleId)
                .orElseThrow(() -> new ResourceNotFoundException("Module not found with moduleId: " + moduleId));

        // Find the smart service within the module
        Optional<ModaptoModule.SmartService> smartService = module.getSmartServices().stream()
                .filter(service -> smartServiceId.equals(service.getServiceId()))
                .findFirst();

        if (smartService.isPresent()) {
            String url = smartService.get().getEndpoint();
            logger.debug("Found smart service URL: {} for moduleId: {} and smartServiceId: {}", url, moduleId, smartServiceId);
            return url;
        } else {
            throw new ResourceNotFoundException("Smart service not found with serviceId: " + smartServiceId + " in module: " + moduleId);
        }
    }

    /**
     * Retrieve ModaptoModule by moduleId
     *
     * @param moduleId The module identifier
     * @return The ModaptoModule entity
     * @throws ResourceNotFoundException if module is not found
     */
    @Override
    public ModaptoModuleDto retrieveModuleByModuleId(String moduleId) {
        logger.debug("Retrieving module with moduleId: {}", moduleId);
        
        ModaptoModule existingModule = modaptoModuleRepository.findByModuleId(moduleId)
                .orElseThrow(() -> new ResourceNotFoundException("Module not found with moduleId: " + moduleId));

        try{
            return modelMapper.map(existingModule, ModaptoModuleDto.class);
        } catch (MappingException e) {
            logger.error(MAPPING_ERROR + "{}", e.getMessage());
            throw new ModelMappingException(MAPPING_ERROR + e.getMessage());
        }
    }

    /**
     * Retrieve all smart services for a specific module
     *
     * @param moduleId The module identifier
     * @return List of smart services for the module
     * @throws ResourceNotFoundException if module is not found
     */
    @Override
    public List<ModaptoModuleDto.SmartServiceDto> retrieveSmartServicesByModuleId(String moduleId) {
        logger.debug("Retrieving smart services for moduleId: {}", moduleId);
        
        ModaptoModuleDto module = retrieveModuleByModuleId(moduleId);
        return module.getSmartServices();
    }

    /**
     * Retrieve all MODAPTO modules with pagination
     *
     * @param pageable Pagination parameters
     * @return Paginated ModaptoModuleDto results
     * @throws ModelMappingException if mapping fails
     */
    @Override
    public Page<ModaptoModuleDto> retrieveAllModulesPaginated(Pageable pageable) {
        logger.debug("Retrieving paginated modules with page: {}, size: {}", pageable.getPageNumber(), pageable.getPageSize());
        
        try {
            Page<ModaptoModule> modulePage = modaptoModuleRepository.findAll(pageable);
            return modulePage.map(module -> {
                ModaptoModuleDto dto = modelMapper.map(module, ModaptoModuleDto.class);
                dto.setMetadata(null);
                return dto;
            });
        } catch (MappingException e) {
            logger.error(MAPPING_ERROR + "{}", e.getMessage());
            throw new ModelMappingException(MAPPING_ERROR + e.getMessage());
        }
    }

    /**
     * Retrieve all MODAPTO modules as DTOs
     *
     * @return List of ModaptoModuleDto
     * @throws ModelMappingException if mapping fails
     */
    @Override
    public List<ModaptoModuleDto> retrieveAllModules() {
        logger.debug("Retrieving all modules");
        
        try {
            List<ModaptoModule> modules = modaptoModuleRepository.findAll(Pageable.unpaged()).getContent();
            return modules.stream()
                    .map(module -> modelMapper.map(module, ModaptoModuleDto.class))
                    .toList();
        } catch (MappingException e) {
            logger.error(MAPPING_ERROR + "{}", e.getMessage());
            throw new ModelMappingException(MAPPING_ERROR + e.getMessage());
        }
    }

    /**
     * Retrieve all MODAPTO modules a worker is working on with pagination
     *
     * @param pageable Pagination parameters
     * @return Paginated ModaptoModuleDto results
     * @throws ModelMappingException if mapping fails
     */
    @Override
    public Page<ModaptoModuleDto> retrieveModulesByWorkerPaginated(String worker, Pageable pageable) {
        logger.debug("Retrieving paginated modules the worker '{}' is working on with page: '{}', size:'{}'", worker, pageable.getPageNumber(), pageable.getPageSize());

        try {
            Page<ModaptoModule> modulePage = modaptoModuleRepository.findByWorkers(worker, pageable);

            return modulePage.map(module -> modelMapper.map(module, ModaptoModuleDto.class));
        } catch (MappingException e) {
            logger.error(MAPPING_ERROR + "{}", e.getMessage());
            throw new ModelMappingException(MAPPING_ERROR + e.getMessage());
        }
    }

    /**`
     * Declare a worker is working on a specific MODAPTO module
     *
     * @param workerData : Declarion of Work Data with Module ID and List of Workers
     * @return Updated ModaptoModuleDto
     * @throws ModelMappingException if mapping fails
     */
    @Override
        public ModaptoModuleDto declareWorkOnModule(DeclarationOfWorkDto workerData) {
            String moduleId = workerData.getModuleId();
            logger.debug("Declaring worker(s) '{}' on module '{}'", workerData.getWorkers(),moduleId);

            try {
                // Fetch the module by its moduleId
                ModaptoModule existingModule = modaptoModuleRepository.findByModuleId(moduleId)
                        .orElseThrow(() -> new ResourceNotFoundException("Module not found with moduleId: " + moduleId));

                // Initialize list if null
                if (existingModule.getWorkers() == null) {
                    existingModule.setWorkers(new ArrayList<>());
                }

                // Add worker if not already present
                List<String> newWorkers = workerData.getWorkers().stream()
                        .filter(worker -> !existingModule.getWorkers().contains(worker))
                        .toList();
                existingModule.getWorkers().addAll(newWorkers);

                // Save updated module back into ES
                ModaptoModule savedModule = modaptoModuleRepository.save(existingModule);

                // Map to DTO
                return modelMapper.map(savedModule, ModaptoModuleDto.class);

            } catch (MappingException e) {
                logger.error(MAPPING_ERROR + "{}", e.getMessage());
                throw new ModelMappingException(MAPPING_ERROR + e.getMessage());
            }
    }

    /**
     * Undeclare a worker is working on a specific MODAPTO module
     *
     * @param moduleId The module identifier
     * @param worker The user identifier of the worker that undeclared working on the module
     * @return Updated ModaptoModuleDto
     * @throws ModelMappingException if mapping fails
     */
    @Override
    public ModaptoModuleDto undeclareWorkOnModule(String moduleId, String worker) {
        logger.debug("Undeclaring worker '{}' on module '{}'", worker, moduleId);

        try {
            // Fetch the module by its moduleId
            ModaptoModule existingModule = modaptoModuleRepository.findByModuleId(moduleId)
                    .orElseThrow(() -> new ResourceNotFoundException("Module not found with moduleId: " + moduleId));

            // Initialize list if null
            if (existingModule.getWorkers() != null) {
                // Remove worker if present
                existingModule.getWorkers().remove(worker);
            }

            // Save updated module back into ES
            ModaptoModule savedModule = modaptoModuleRepository.save(existingModule);

            // Map to DTO
            return modelMapper.map(savedModule, ModaptoModuleDto.class);

        } catch (MappingException e) {
            logger.error(MAPPING_ERROR + "{}", e.getMessage());
            throw new ModelMappingException(MAPPING_ERROR + e.getMessage());
        }
    }
}