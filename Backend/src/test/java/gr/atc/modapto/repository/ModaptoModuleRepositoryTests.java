package gr.atc.modapto.repository;

import gr.atc.modapto.config.SetupTestContainersEnvironment;
import gr.atc.modapto.model.ModaptoModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.elasticsearch.DataElasticsearchTest;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@ActiveProfiles("test")
@Testcontainers
@DataElasticsearchTest
@DisplayName("ModaptoModuleRepository Tests")
class ModaptoModuleRepositoryTests extends SetupTestContainersEnvironment {

    @Autowired
    private ModaptoModuleRepository repository;

    private ModaptoModule sampleModule1;
    private ModaptoModule sampleModule2;

    @BeforeEach
    void setUp() {
        // Given - Clean and setup test data
        repository.deleteAll();

        sampleModule1 = new ModaptoModule();
        sampleModule1.setModuleId("TEST_MODULE_1");
        sampleModule1.setName("Test Module 1");
        sampleModule1.setEndpoint("https://module1.example.com");
        sampleModule1.setSmartServices(List.of(
                new ModaptoModule.SmartService("Service 1", "CAT1", "SERVICE_1", "https://dtm.example.com/api/services/service1")
        ));

        sampleModule2 = new ModaptoModule();
        sampleModule2.setModuleId("TEST_MODULE_2");
        sampleModule2.setName("Test Module 2");
        sampleModule2.setEndpoint("https://module2.example.com");
        sampleModule2.setSmartServices(List.of(
                new ModaptoModule.SmartService("Service 2", "CAT2", "SERVICE_2", "https://dtm.example.com/api/services/service2")
        ));
    }

    @Nested
    @DisplayName("Basic CRUD Operations")
    class BasicCrudOperations {

        @Test
        @DisplayName("Save module : Success")
        void givenValidModule_whenSave_thenPersistsSuccessfully() {
            // When
            ModaptoModule saved = repository.save(sampleModule1);

            // Then
            assertThat(saved).isNotNull();
            assertThat(saved.getId()).isNotNull();
            assertThat(saved.getModuleId()).isEqualTo("TEST_MODULE_1");
            assertThat(saved.getName()).isEqualTo("Test Module 1");
            assertThat(saved.getEndpoint()).isEqualTo("https://module1.example.com");
            assertThat(saved.getSmartServices()).hasSize(1);
            assertThat(saved.getSmartServices().getFirst().getServiceId()).isEqualTo("SERVICE_1");
        }

        @Test
        @DisplayName("Find by ID : Success")
        void givenSavedModule_whenFindById_thenReturnsModule() {
            // Given
            ModaptoModule saved = repository.save(sampleModule1);

            // When
            Optional<ModaptoModule> found = repository.findById(saved.getId());

            // Then
            assertThat(found).isPresent();
            assertThat(found.get().getModuleId()).isEqualTo("TEST_MODULE_1");
            assertThat(found.get().getName()).isEqualTo("Test Module 1");
        }

        @Test
        @DisplayName("Delete module : Success")
        void givenSavedModule_whenDelete_thenRemovesModule() {
            // Given
            ModaptoModule saved = repository.save(sampleModule1);

            // When
            repository.delete(saved);

            // Then
            Optional<ModaptoModule> found = repository.findById(saved.getId());
            assertThat(found).isNotPresent();
        }

        @Test
        @DisplayName("Count modules : Success")
        void givenMultipleModules_whenCount_thenReturnsCorrectCount() {
            // Given
            repository.saveAll(Arrays.asList(sampleModule1, sampleModule2));

            // When
            long count = repository.count();

            // Then
            assertThat(count).isEqualTo(2);
        }
    }

    @Nested
    @DisplayName("Custom Query Methods")
    class CustomQueryMethods {

        @Test
        @DisplayName("Find by module ID : Success")
        void givenExistingModule_whenFindByModuleId_thenReturnsModule() {
            // Given
            repository.save(sampleModule1);

            // When
            Optional<ModaptoModule> found = repository.findByModuleId("TEST_MODULE_1");

            // Then
            assertThat(found).isPresent();
            assertThat(found.get().getModuleId()).isEqualTo("TEST_MODULE_1");
            assertThat(found.get().getName()).isEqualTo("Test Module 1");
            assertThat(found.get().getEndpoint()).isEqualTo("https://module1.example.com");
            assertThat(found.get().getSmartServices()).hasSize(1);
            assertThat(found.get().getSmartServices().getFirst().getServiceId()).isEqualTo("SERVICE_1");
        }

        @Test
        @DisplayName("Find by module ID : No match")
        void givenNonExistentModule_whenFindByModuleId_thenReturnsEmpty() {
            // Given
            repository.save(sampleModule1);

            // When
            Optional<ModaptoModule> found = repository.findByModuleId("NON_EXISTENT_MODULE");

            // Then
            assertThat(found).isNotPresent();
        }

        @Test
        @DisplayName("Find by module ID : Case sensitivity")
        void givenCaseDifference_whenFindByModuleId_thenReturnsEmpty() {
            // Given
            repository.save(sampleModule1);

            // When
            Optional<ModaptoModule> foundLowerCase = repository.findByModuleId("test_module_1");
            Optional<ModaptoModule> foundMixedCase = repository.findByModuleId("Test_Module_1");

            // Then
            assertThat(foundLowerCase).isNotPresent();
            assertThat(foundMixedCase).isNotPresent();
        }

        @Test
        @DisplayName("Find by module ID : Multiple smart services")
        void givenModuleWithMultipleServices_whenFindByModuleId_thenReturnsModuleWithAllServices() {
            // Given
            ModaptoModule moduleWithMultipleServices = new ModaptoModule();
            moduleWithMultipleServices.setModuleId("MULTI_SERVICE_MODULE");
            moduleWithMultipleServices.setName("Multi Service Module");
            moduleWithMultipleServices.setEndpoint("https://multi.example.com");
            moduleWithMultipleServices.setSmartServices(Arrays.asList(
                    new ModaptoModule.SmartService("Service A", "CAT_A", "SERVICE_A", "https://dtm.example.com/api/services/a"),
                    new ModaptoModule.SmartService("Service B", "CAT_B", "SERVICE_B", "https://dtm.example.com/api/services/b"),
                    new ModaptoModule.SmartService("Service C", "CAT_C", "SERVICE_C", "https://dtm.example.com/api/services/c")
            ));
            repository.save(moduleWithMultipleServices);

            // When
            Optional<ModaptoModule> found = repository.findByModuleId("MULTI_SERVICE_MODULE");

            // Then
            assertThat(found).isPresent();
            assertThat(found.get().getSmartServices()).hasSize(3);
            assertThat(found.get().getSmartServices())
                    .extracting(ModaptoModule.SmartService::getServiceId)
                    .containsExactlyInAnyOrder("SERVICE_A", "SERVICE_B", "SERVICE_C");
        }
    }

    @Nested
    @DisplayName("Data Integrity and Edge Cases")
    class DataIntegrityAndEdgeCases {

        @Test
        @DisplayName("Save module with null URL : Success")
        void givenModuleWithNullUrl_whenSave_thenPersistsSuccessfully() {
            // Given
            ModaptoModule moduleWithNullUrl = new ModaptoModule();
            moduleWithNullUrl.setModuleId("NULL_URL_MODULE");
            moduleWithNullUrl.setName("Null URL Module");
            moduleWithNullUrl.setEndpoint(null);
            moduleWithNullUrl.setSmartServices(null);

            // When
            ModaptoModule saved = repository.save(moduleWithNullUrl);

            // Then
            assertThat(saved).isNotNull();
            assertThat(saved.getModuleId()).isEqualTo("NULL_URL_MODULE");
            assertThat(saved.getName()).isEqualTo("Null URL Module");
            assertThat(saved.getEndpoint()).isNull();
            assertThat(saved.getSmartServices()).isNull();
        }

        @Test
        @DisplayName("Update existing module : Success")
        void givenExistingModule_whenUpdate_thenUpdatesSuccessfully() {
            // Given
            ModaptoModule saved = repository.save(sampleModule1);
            String originalId = saved.getId();

            // When
            saved.setEndpoint("https://updated.example.com/module1");
            saved.setName("Updated Module Name");
            ModaptoModule updated = repository.save(saved);

            // Then
            assertThat(updated.getId()).isEqualTo(originalId);
            assertThat(updated.getEndpoint()).isEqualTo("https://updated.example.com/module1");
            assertThat(updated.getName()).isEqualTo("Updated Module Name");
            assertThat(updated.getModuleId()).isEqualTo("TEST_MODULE_1"); // Unchanged
        }


        @Test
        @DisplayName("Save duplicate modules : Success")
        void givenDuplicateModules_whenSave_thenBothPersistSuccessfully() {
            // Given - This tests that the repository allows duplicates (no unique constraints)
            ModaptoModule duplicate1 = new ModaptoModule();
            duplicate1.setModuleId("DUPLICATE_MODULE");
            duplicate1.setName("Duplicate Module 1");
            duplicate1.setEndpoint("https://example.com/module/duplicate1");
            duplicate1.setSmartServices(List.of());

            ModaptoModule duplicate2 = new ModaptoModule();
            duplicate2.setModuleId("DUPLICATE_MODULE");
            duplicate2.setName("Duplicate Module 2");
            duplicate2.setEndpoint("https://example.com/module/duplicate2");
            duplicate2.setSmartServices(List.of());

            // When
            ModaptoModule saved1 = repository.save(duplicate1);
            ModaptoModule saved2 = repository.save(duplicate2);

            // Then
            assertThat(saved1).isNotNull();
            assertThat(saved2).isNotNull();
            assertThat(saved1.getId()).isNotEqualTo(saved2.getId());

            // Verify count
            long count = repository.count();
            assertThat(count).isEqualTo(2);

            // Note: findByModuleId will return one of them (implementation dependent)
            Optional<ModaptoModule> found = repository.findByModuleId("DUPLICATE_MODULE");
            assertThat(found).isPresent();
        }
    }
}