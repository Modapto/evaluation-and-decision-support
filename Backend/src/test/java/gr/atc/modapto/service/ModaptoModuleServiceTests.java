package gr.atc.modapto.service;

import gr.atc.modapto.dto.ModaptoModuleDto;
import gr.atc.modapto.dto.sew.DeclarationOfWorkDto;
import gr.atc.modapto.exception.CustomExceptions.ResourceNotFoundException;
import gr.atc.modapto.model.ModaptoModule;
import gr.atc.modapto.repository.ModaptoModuleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.BDDMockito;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Modapto Module Service Unit Tests")
class ModaptoModuleServiceTests {

    @Mock
    private ModaptoModuleRepository modaptoModuleRepository;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private ModaptoModuleService modaptoModuleService;

    private static final String TEST_MODULE_ID = "TEST_MODULE";
    private static final String TEST_SERVICE_ID = "THRESHOLD_SERVICE";

    private ModaptoModule testModule;
    private ModaptoModuleDto testModuleDto;
    private ModaptoModuleDto.SmartServiceDto testSmartServiceDto;
    private ModaptoModuleDto.SmartServiceDto otherSmartServiceDto;

    @BeforeEach
    void setUp() {
        ModaptoModule.SmartService testSmartService = new ModaptoModule.SmartService(
                "Threshold Maintenance Service",
                "CAT_001",
                TEST_SERVICE_ID,
                "https://dtm.example.com/api/services/threshold"
        );

        testSmartServiceDto = ModaptoModuleDto.SmartServiceDto.builder()
                .name("Threshold Maintenance Service")
                .catalogueId("CAT_001")
                .serviceId(TEST_SERVICE_ID)
                .endpoint("https://dtm.example.com/api/services/threshold")
                .build();

        otherSmartServiceDto = ModaptoModuleDto.SmartServiceDto.builder()
                .name("Other Service")
                .catalogueId("CAT_002")
                .serviceId("OTHER_SERVICE")
                .endpoint("https://dtm.example.com/api/services/other")
                .build();

        ModaptoModule.SmartService otherService = new ModaptoModule.SmartService(
                "Other Service",
                "CAT_002",
                "OTHER_SERVICE",
                "https://dtm.example.com/api/services/other"
        );

        testModule = new ModaptoModule(
                "1",
                TEST_MODULE_ID,
                "Test Production Module",
                "https://module.example.com",
                Arrays.asList(testSmartService, otherService),
                LocalDateTime.now(),
                Instant.now(),
                new ArrayList<>()
        );

        testModuleDto = ModaptoModuleDto.builder()
                .id("1")
                .moduleId(TEST_MODULE_ID)
                .name("Test Production Module")
                .endpoint("https://module.example.com")
                .smartServices(Arrays.asList(testSmartServiceDto, otherSmartServiceDto))
                .timestampDt(LocalDateTime.now())
                .build();
    }

    @Nested
    @DisplayName("Retrieve Smart Service URL")
    class RetrieveSmartServiceUrl {

        @DisplayName("Retrieve smart service URL : Success")
        @Test
        void givenValidModuleIdAndServiceId_whenRetrieveSmartServiceUrl_thenReturnUrl() {
            BDDMockito.given(modaptoModuleRepository.findByModuleId(TEST_MODULE_ID))
                    .willReturn(Optional.of(testModule));

            String url = modaptoModuleService.retrieveSmartServiceUrl(TEST_MODULE_ID, TEST_SERVICE_ID);

            assertThat(url).isEqualTo("https://dtm.example.com/api/services/threshold");
        }

        @DisplayName("Retrieve smart service URL : Module not found")
        @Test
        void givenInvalidModuleId_whenRetrieveSmartServiceUrl_thenThrowResourceNotFoundException() {
            String invalidModuleId = "INVALID_MODULE";
            BDDMockito.given(modaptoModuleRepository.findByModuleId(invalidModuleId))
                    .willReturn(Optional.empty());

            assertThatThrownBy(() -> modaptoModuleService.retrieveSmartServiceUrl(invalidModuleId, TEST_SERVICE_ID))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Module not found with moduleId: " + invalidModuleId);
        }

        @DisplayName("Retrieve smart service URL : Smart service not found")
        @Test
        void givenValidModuleIdButInvalidServiceId_whenRetrieveSmartServiceUrl_thenThrowResourceNotFoundException() {
            String invalidServiceId = "INVALID_SERVICE";
            BDDMockito.given(modaptoModuleRepository.findByModuleId(TEST_MODULE_ID))
                    .willReturn(Optional.of(testModule));

            assertThatThrownBy(() -> modaptoModuleService.retrieveSmartServiceUrl(TEST_MODULE_ID, invalidServiceId))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Smart service not found with serviceId: " + invalidServiceId);
        }
    }

    @Nested
    @DisplayName("Retrieve Module By Module ID")
    class RetrieveModuleByModuleId {

        @DisplayName("Retrieve module by ID : Success")
        @Test
        void givenValidModuleId_whenRetrieveModuleByModuleId_thenReturnModule() {
            BDDMockito.given(modaptoModuleRepository.findByModuleId(TEST_MODULE_ID))
                    .willReturn(Optional.of(testModule));
            BDDMockito.given(modelMapper.map(testModule, ModaptoModuleDto.class))
                    .willReturn(testModuleDto);

            ModaptoModuleDto foundModule = modaptoModuleService.retrieveModuleByModuleId(TEST_MODULE_ID);

            assertThat(foundModule.getModuleId()).isEqualTo(TEST_MODULE_ID);
            assertThat(foundModule.getName()).isEqualTo("Test Production Module");
        }

        @DisplayName("Retrieve module by ID : Module not found")
        @Test
        void givenInvalidModuleId_whenRetrieveModuleByModuleId_thenThrowResourceNotFoundException() {
            String invalidModuleId = "INVALID_MODULE";
            BDDMockito.given(modaptoModuleRepository.findByModuleId(invalidModuleId))
                    .willReturn(Optional.empty());

            assertThatThrownBy(() -> modaptoModuleService.retrieveModuleByModuleId(invalidModuleId))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Module not found with moduleId: " + invalidModuleId);
        }
    }

    @Nested
    @DisplayName("Retrieve All Modules Paginated")
    class RetrieveAllModulesPaginated {

        @DisplayName("Retrieve paginated modules : Success")
        @Test
        void givenValidPageable_whenRetrieveAllModulesPaginated_thenReturnPaginatedResults() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<ModaptoModule> modulePage = new PageImpl<>(Collections.singletonList(testModule));
            ModaptoModuleDto moduleDto = new ModaptoModuleDto();
            moduleDto.setModuleId(TEST_MODULE_ID);

            BDDMockito.given(modaptoModuleRepository.findAll(pageable)).willReturn(modulePage);
            BDDMockito.given(modelMapper.map(testModule, ModaptoModuleDto.class)).willReturn(moduleDto);

            Page<ModaptoModuleDto> result = modaptoModuleService.retrieveAllModulesPaginated(pageable);

            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().getFirst().getModuleId()).isEqualTo(TEST_MODULE_ID);
        }
    }

    @Nested
    @DisplayName("Retrieve All Modules")
    class RetrieveAllModules {

        @DisplayName("Retrieve all modules : Success")
        @Test
        void whenRetrieveAllModules_thenReturnAllModulesAsDto() {
            Page<ModaptoModule> modulePage = new PageImpl<>(Collections.singletonList(testModule));

            BDDMockito.given(modaptoModuleRepository.findAll(Pageable.unpaged())).willReturn(modulePage);
            BDDMockito.given(modelMapper.map(testModule, ModaptoModuleDto.class)).willReturn(testModuleDto);

            List<ModaptoModuleDto> result = modaptoModuleService.retrieveAllModules();

            assertThat(result).isNotNull().hasSize(1);
            assertThat(result.getFirst().getModuleId()).isEqualTo(TEST_MODULE_ID);
            assertThat(result.getFirst().getName()).isEqualTo("Test Production Module");
        }
    }

    @Nested
    @DisplayName("Retrieve Smart Services By Module ID")
    class RetrieveSmartServicesByModuleId {

        @DisplayName("Retrieve smart services : Success")
        @Test
        void givenValidModuleId_whenRetrieveSmartServicesByModuleId_thenReturnSmartServices() {
            BDDMockito.given(modaptoModuleRepository.findByModuleId(TEST_MODULE_ID))
                    .willReturn(Optional.of(testModule));
            BDDMockito.given(modelMapper.map(testModule, ModaptoModuleDto.class))
                    .willReturn(testModuleDto);

            List<ModaptoModuleDto.SmartServiceDto> smartServices = modaptoModuleService.retrieveSmartServicesByModuleId(TEST_MODULE_ID);

            assertThat(smartServices).isNotNull().hasSize(2);
            assertThat(smartServices.getFirst().getServiceId()).isEqualTo(TEST_SERVICE_ID);
            assertThat(smartServices.getFirst().getName()).isEqualTo("Threshold Maintenance Service");
        }

        @DisplayName("Retrieve smart services : Module not found")
        @Test
        void givenInvalidModuleId_whenRetrieveSmartServicesByModuleId_thenThrowResourceNotFoundException() {
            String invalidModuleId = "INVALID_MODULE";
            BDDMockito.given(modaptoModuleRepository.findByModuleId(invalidModuleId))
                    .willReturn(Optional.empty());

            assertThatThrownBy(() -> modaptoModuleService.retrieveSmartServicesByModuleId(invalidModuleId))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Module not found with moduleId: " + invalidModuleId);
        }
    }

    @Nested
    @DisplayName("Worker-Module Association")
    class WorkerModuleAssociationTests {

        private static final String TEST_WORKER = "worker1";

        @DisplayName("Retrieve modules by worker : Success")
        @Test
        void givenWorkerAndPageable_whenRetrieveModulesByWorkerPaginated_thenReturnPaginatedResults() {
            Pageable pageable = PageRequest.of(0, 10);
            testModule.setWorkers(new ArrayList<>(Collections.singletonList(TEST_WORKER)));
            Page<ModaptoModule> modulePage = new PageImpl<>(Collections.singletonList(testModule));

            BDDMockito.given(modaptoModuleRepository.findByWorkers(TEST_WORKER, pageable))
                    .willReturn(modulePage);
            BDDMockito.given(modelMapper.map(testModule, ModaptoModuleDto.class))
                    .willReturn(testModuleDto);

            Page<ModaptoModuleDto> result = modaptoModuleService.retrieveModulesByWorkerPaginated(TEST_WORKER, pageable);

            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().getFirst().getModuleId()).isEqualTo(TEST_MODULE_ID);
        }

        @DisplayName("Declare worker on module : Success")
        @Test
        void givenValidModuleIdAndWorker_whenDeclareWorkOnModule_thenWorkerAdded() {
            BDDMockito.given(modaptoModuleRepository.findByModuleId(TEST_MODULE_ID))
                    .willReturn(Optional.of(testModule));
            BDDMockito.given(modaptoModuleRepository.save(testModule)).willReturn(testModule);
            BDDMockito.given(modelMapper.map(testModule, ModaptoModuleDto.class))
                    .willReturn(testModuleDto);

            DeclarationOfWorkDto workData = DeclarationOfWorkDto.builder()
                    .moduleId(TEST_MODULE_ID)
                    .workers(Arrays.asList(TEST_WORKER))
                    .build();
            ModaptoModuleDto result = modaptoModuleService.declareWorkOnModule(workData);

            assertThat(testModule.getWorkers()).contains(TEST_WORKER);
            assertThat(result).isNotNull();
            assertThat(result.getModuleId()).isEqualTo(TEST_MODULE_ID);
        }

        @DisplayName("Declare worker on module : Module not found")
        @Test
        void givenInvalidModuleId_whenDeclareWorkOnModule_thenThrowResourceNotFoundException() {
            String invalidModuleId = "INVALID_MODULE";
            BDDMockito.given(modaptoModuleRepository.findByModuleId(invalidModuleId))
                    .willReturn(Optional.empty());

            DeclarationOfWorkDto workData = DeclarationOfWorkDto.builder()
                    .moduleId(invalidModuleId)
                    .workers(Arrays.asList(TEST_WORKER))
                    .build();
            assertThatThrownBy(() ->
                    modaptoModuleService.declareWorkOnModule(workData))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Module not found with moduleId: " + invalidModuleId);
        }

        @DisplayName("Undeclare worker on module : Success")
        @Test
        void givenValidModuleIdAndWorker_whenUndeclareWorkOnModule_thenWorkerRemoved() {
            testModule.setWorkers(new ArrayList<>(Collections.singletonList(TEST_WORKER)));

            BDDMockito.given(modaptoModuleRepository.findByModuleId(TEST_MODULE_ID))
                    .willReturn(Optional.of(testModule));
            BDDMockito.given(modaptoModuleRepository.save(testModule)).willReturn(testModule);
            BDDMockito.given(modelMapper.map(testModule, ModaptoModuleDto.class))
                    .willReturn(testModuleDto);

            ModaptoModuleDto result = modaptoModuleService.undeclareWorkOnModule(TEST_MODULE_ID, TEST_WORKER);

            assertThat(testModule.getWorkers()).doesNotContain(TEST_WORKER);
            assertThat(result).isNotNull();
            assertThat(result.getModuleId()).isEqualTo(TEST_MODULE_ID);
        }

        @DisplayName("Undeclare worker on module : Module not found")
        @Test
        void givenInvalidModuleId_whenUndeclareWorkOnModule_thenThrowResourceNotFoundException() {
            String invalidModuleId = "INVALID_MODULE";
            BDDMockito.given(modaptoModuleRepository.findByModuleId(invalidModuleId))
                    .willReturn(Optional.empty());

            assertThatThrownBy(() ->
                    modaptoModuleService.undeclareWorkOnModule(invalidModuleId, TEST_WORKER))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Module not found with moduleId: " + invalidModuleId);
        }
    }

}