package gr.atc.modapto.service;

import gr.atc.modapto.dto.ModaptoModuleDto;
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

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

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
                2312451L
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
            // Given
            BDDMockito.given(modaptoModuleRepository.findByModuleId(TEST_MODULE_ID))
                    .willReturn(Optional.of(testModule));

            // When
            String url = modaptoModuleService.retrieveSmartServiceUrl(TEST_MODULE_ID, TEST_SERVICE_ID);

            // Then
            assertThat(url).isEqualTo("https://dtm.example.com/api/services/threshold");
        }

        @DisplayName("Retrieve smart service URL : Module not found")
        @Test
        void givenInvalidModuleId_whenRetrieveSmartServiceUrl_thenThrowResourceNotFoundException() {
            // Given
            String invalidModuleId = "INVALID_MODULE";
            BDDMockito.given(modaptoModuleRepository.findByModuleId(invalidModuleId))
                    .willReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> modaptoModuleService.retrieveSmartServiceUrl(invalidModuleId, TEST_SERVICE_ID))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Module not found with moduleId: " + invalidModuleId);
        }

        @DisplayName("Retrieve smart service URL : Smart service not found")
        @Test
        void givenValidModuleIdButInvalidServiceId_whenRetrieveSmartServiceUrl_thenThrowResourceNotFoundException() {
            // Given
            String invalidServiceId = "INVALID_SERVICE";
            BDDMockito.given(modaptoModuleRepository.findByModuleId(TEST_MODULE_ID))
                    .willReturn(Optional.of(testModule));

            // When & Then
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
            // Given
            BDDMockito.given(modaptoModuleRepository.findByModuleId(TEST_MODULE_ID))
                    .willReturn(Optional.of(testModule));
            BDDMockito.given(modelMapper.map(testModule, ModaptoModuleDto.class))
                    .willReturn(testModuleDto);

            // When
            ModaptoModuleDto foundModule = modaptoModuleService.retrieveModuleByModuleId(TEST_MODULE_ID);

            // Then
            assertThat(foundModule.getModuleId()).isEqualTo(TEST_MODULE_ID);
            assertThat(foundModule.getName()).isEqualTo("Test Production Module");
        }

        @DisplayName("Retrieve module by ID : Module not found")
        @Test
        void givenInvalidModuleId_whenRetrieveModuleByModuleId_thenThrowResourceNotFoundException() {
            // Given
            String invalidModuleId = "INVALID_MODULE";
            BDDMockito.given(modaptoModuleRepository.findByModuleId(invalidModuleId))
                    .willReturn(Optional.empty());

            // When & Then
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
            // Given
            Pageable pageable = PageRequest.of(0, 10);
            Page<ModaptoModule> modulePage = new PageImpl<>(Collections.singletonList(testModule));
            ModaptoModuleDto moduleDto = new ModaptoModuleDto();
            moduleDto.setModuleId(TEST_MODULE_ID);

            BDDMockito.given(modaptoModuleRepository.findAll(pageable)).willReturn(modulePage);
            BDDMockito.given(modelMapper.map(testModule, ModaptoModuleDto.class)).willReturn(moduleDto);

            // When
            Page<ModaptoModuleDto> result = modaptoModuleService.retrieveAllModulesPaginated(pageable);

            // Then
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
            // Given
            Page<ModaptoModule> modulePage = new PageImpl<>(Collections.singletonList(testModule));
            
            BDDMockito.given(modaptoModuleRepository.findAll(Pageable.unpaged())).willReturn(modulePage);
            BDDMockito.given(modelMapper.map(testModule, ModaptoModuleDto.class)).willReturn(testModuleDto);

            // When
            List<ModaptoModuleDto> result = modaptoModuleService.retrieveAllModules();

            // Then
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
            // Given
            BDDMockito.given(modaptoModuleRepository.findByModuleId(TEST_MODULE_ID))
                    .willReturn(Optional.of(testModule));
            BDDMockito.given(modelMapper.map(testModule, ModaptoModuleDto.class))
                    .willReturn(testModuleDto);

            // When
            List<ModaptoModuleDto.SmartServiceDto> smartServices = modaptoModuleService.retrieveSmartServicesByModuleId(TEST_MODULE_ID);

            // Then
            assertThat(smartServices).isNotNull().hasSize(2);
            assertThat(smartServices.getFirst().getServiceId()).isEqualTo(TEST_SERVICE_ID);
            assertThat(smartServices.getFirst().getName()).isEqualTo("Threshold Maintenance Service");
        }

        @DisplayName("Retrieve smart services : Module not found")
        @Test
        void givenInvalidModuleId_whenRetrieveSmartServicesByModuleId_thenThrowResourceNotFoundException() {
            // Given
            String invalidModuleId = "INVALID_MODULE";
            BDDMockito.given(modaptoModuleRepository.findByModuleId(invalidModuleId))
                    .willReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> modaptoModuleService.retrieveSmartServicesByModuleId(invalidModuleId))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Module not found with moduleId: " + invalidModuleId);
        }
    }
}