package gr.atc.modapto.repository;

import gr.atc.modapto.config.SetupTestContainersEnvironment;
import gr.atc.modapto.model.sew.SewComponentInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.elasticsearch.DataElasticsearchTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@DataElasticsearchTest
@Testcontainers
@ActiveProfiles("test")
@DisplayName("SewComponentInfoRepository Tests")
class SewComponentInfoRepositoryTests extends SetupTestContainersEnvironment{

    @Autowired
    private SewComponentInfoRepository sewComponentInfoRepository;

    private SewComponentInfo sampleComponent1;
    private SewComponentInfo sampleComponent2;

    @BeforeEach
    void setUp() {
        sewComponentInfoRepository.deleteAll();

        sampleComponent1 = new SewComponentInfo();
        sampleComponent1.setStage("Stage1");
        sampleComponent1.setCell("Cell1");
        sampleComponent1.setModule("Module1");
        sampleComponent1.setModuleId("MOD1");
        sampleComponent1.setLastMaintenanceActionTime("2024-01-15T10:30:00");

        sampleComponent2 = new SewComponentInfo();
        sampleComponent2.setStage("Stage2");
        sampleComponent2.setCell("Cell2");
        sampleComponent2.setModule("Module2");
        sampleComponent2.setModuleId("MOD2");
        sampleComponent2.setLastMaintenanceActionTime("2024-01-16T11:45:00");
    }

    @Nested
    @DisplayName("Basic CRUD Operations")
    class BasicCrudOperations {

        @Test
        @DisplayName("Save component : Success")
        void givenValidComponent_whenSave_thenPersistsSuccessfully() {
            SewComponentInfo saved = sewComponentInfoRepository.save(sampleComponent1);

            assertThat(saved).isNotNull();
            assertThat(saved.getId()).isNotNull();
            assertThat(saved.getStage()).isEqualTo("Stage1");
            assertThat(saved.getCell()).isEqualTo("Cell1");
            assertThat(saved.getModule()).isEqualTo("Module1");
            assertThat(saved.getModuleId()).isEqualTo("MOD1");
        }

        @Test
        @DisplayName("Find all components : Success")
        void givenMultipleComponents_whenFindAll_thenReturnsAllComponents() {
            sewComponentInfoRepository.saveAll(Arrays.asList(sampleComponent1, sampleComponent2));

            Page<SewComponentInfo> result = sewComponentInfoRepository.findAll(Pageable.unpaged());

            assertThat(result.getContent()).hasSize(2);
            assertThat(result.getContent())
                    .extracting(SewComponentInfo::getStage)
                    .containsExactlyInAnyOrder("Stage1", "Stage2");
        }

        @Test
        @DisplayName("Delete all components : Success")
        void givenMultipleComponents_whenDeleteAll_thenRemovesAllComponents() {
            sewComponentInfoRepository.saveAll(Arrays.asList(sampleComponent1, sampleComponent2));

            sewComponentInfoRepository.deleteAll();

            Page<SewComponentInfo> result = sewComponentInfoRepository.findAll(Pageable.unpaged());
            assertThat(result.getContent()).isEmpty();
        }
    }

    @Nested
    @DisplayName("Custom Query Methods")
    class CustomQueryMethods {

        @Test
        @DisplayName("Find by stage, cell, module and moduleId : Success")
        void givenMultipleComponents_whenFindByAllAttributes_thenReturnsMatchingComponents() {
            sewComponentInfoRepository.saveAll(Arrays.asList(sampleComponent1, sampleComponent2));

            List<SewComponentInfo> result = sewComponentInfoRepository
                    .findByStageAndCellAndModuleAndModuleId("Stage1", "Cell1", "Module1", "MOD1");

            assertThat(result).hasSize(1);
            assertThat(result.getFirst().getStage()).isEqualTo("Stage1");
            assertThat(result.getFirst().getCell()).isEqualTo("Cell1");
            assertThat(result.getFirst().getModule()).isEqualTo("Module1");
            assertThat(result.getFirst().getModuleId()).isEqualTo("MOD1");
        }

        @Test
        @DisplayName("Find by attributes : No matches")
        void givenNoMatchingComponents_whenFindByAllAttributes_thenReturnsEmptyList() {
            sewComponentInfoRepository.save(sampleComponent1);

            List<SewComponentInfo> result = sewComponentInfoRepository
                    .findByStageAndCellAndModuleAndModuleId("NonExistent", "NonExistent", "NonExistent", "NON");

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Find by attributes : Multiple matches")
        void givenMultipleMatchingComponents_whenFindByAllAttributes_thenReturnsAllMatches() {
            SewComponentInfo duplicateComponent = new SewComponentInfo();
            duplicateComponent.setStage("Stage1");
            duplicateComponent.setCell("Cell1");
            duplicateComponent.setModule("Module1");
            duplicateComponent.setModuleId("MOD1");
            duplicateComponent.setLastMaintenanceActionTime("2024-01-17T12:00:00");

            sewComponentInfoRepository.saveAll(Arrays.asList(sampleComponent1, duplicateComponent));

            List<SewComponentInfo> result = sewComponentInfoRepository
                    .findByStageAndCellAndModuleAndModuleId("Stage1", "Cell1", "Module1", "MOD1");

            assertThat(result).hasSize(2);
        }
    }

    @Nested
    @DisplayName("Pagination and Sorting")
    class PaginationAndSorting {

        @Test
        @DisplayName("Find all with pagination : Success")
        void givenMultipleComponents_whenFindAllWithPagination_thenReturnsPaginatedResults() {
            List<SewComponentInfo> components = Arrays.asList(
                    createComponent("Stage1", "Cell1", "Module1", "MOD1"),
                    createComponent("Stage2", "Cell2", "Module2", "MOD2"),
                    createComponent("Stage3", "Cell3", "Module3", "MOD3")
            );
            sewComponentInfoRepository.saveAll(components);

            Pageable pageable = PageRequest.of(0, 2);
            Page<SewComponentInfo> result = sewComponentInfoRepository.findAll(pageable);

            assertThat(result.getContent()).hasSize(2);
            assertThat(result.getTotalElements()).isEqualTo(3);
            assertThat(result.getTotalPages()).isEqualTo(2);
            assertThat(result.hasNext()).isTrue();
        }

        @Test
        @DisplayName("Find all with empty repository : Success")
        void givenEmptyRepository_whenFindAll_thenReturnsEmptyPage() {
            Page<SewComponentInfo> result = sewComponentInfoRepository.findAll(PageRequest.of(0, 10));

            assertThat(result.getContent()).isEmpty();
            assertThat(result.getTotalElements()).isZero();
            assertThat(result.getTotalPages()).isZero();
        }
    }

    @Nested
    @DisplayName("Edge Cases and Data Integrity")
    class EdgeCasesAndDataIntegrity {

        @Test
        @DisplayName("Save component with null fields : Success")
        void givenComponentWithNullFields_whenSave_thenPersistsSuccessfully() {
            SewComponentInfo componentWithNulls = new SewComponentInfo();
            componentWithNulls.setStage("Stage1");
            componentWithNulls.setCell(null);
            componentWithNulls.setModule(null);
            componentWithNulls.setModuleId("MOD1");

            SewComponentInfo saved = sewComponentInfoRepository.save(componentWithNulls);

            assertThat(saved).isNotNull();
            assertThat(saved.getStage()).isEqualTo("Stage1");
            assertThat(saved.getCell()).isNull();
            assertThat(saved.getModule()).isNull();
            assertThat(saved.getModuleId()).isEqualTo("MOD1");
        }

        @Test
        @DisplayName("Update component : Success")
        void givenExistingComponent_whenUpdate_thenUpdatesSuccessfully() {
            SewComponentInfo saved = sewComponentInfoRepository.save(sampleComponent1);
            String originalId = saved.getId();

            saved.setLastMaintenanceActionTime("2024-01-20T15:30:00");
            SewComponentInfo updated = sewComponentInfoRepository.save(saved);

            assertThat(updated.getId()).isEqualTo(originalId);
            assertThat(updated.getLastMaintenanceActionTime()).isEqualTo("2024-01-20T15:30:00");
            assertThat(updated.getStage()).isEqualTo("Stage1"); // Other fields unchanged
        }

        @Test
        @DisplayName("Search with special characters : Success")
        void givenComponentWithSpecialCharacters_whenSearch_thenFindsCorrectly() {
            SewComponentInfo specialComponent = new SewComponentInfo();
            specialComponent.setStage("Stage@#$");
            specialComponent.setCell("Cell_123");
            specialComponent.setModule("Module-ABC");
            specialComponent.setModuleId("MOD@123");

            sewComponentInfoRepository.save(specialComponent);

            List<SewComponentInfo> result = sewComponentInfoRepository
                    .findByStageAndCellAndModuleAndModuleId("Stage@#$", "Cell_123", "Module-ABC", "MOD@123");

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getStage()).isEqualTo("Stage@#$");
        }
    }

    // Helper method
    private SewComponentInfo createComponent(String stage, String cell, String module, String moduleId) {
        SewComponentInfo component = new SewComponentInfo();
        component.setStage(stage);
        component.setCell(cell);
        component.setModule(module);
        component.setModuleId(moduleId);
        component.setLastMaintenanceActionTime("2024-01-15T10:30:00");
        return component;
    }
}