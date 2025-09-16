package gr.atc.modapto.repository;

import gr.atc.modapto.config.SetupTestContainersEnvironment;
import gr.atc.modapto.model.sew.SewMonitorKpisComponents;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.elasticsearch.DataElasticsearchTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataElasticsearchTest
@ActiveProfiles("test")
@DisplayName("SewMonitorKpisComponentsRepository Tests")
class SewMonitorKpisComponentsRepositoryTests extends SetupTestContainersEnvironment {

    @Autowired
    private SewMonitorKpisComponentsRepository sewMonitorKpisComponentsRepository;

    private SewMonitorKpisComponents testComponent;
    private SewMonitorKpisComponents anotherComponent;

    @BeforeEach
    void setUp() {
        sewMonitorKpisComponentsRepository.deleteAll();

        SewMonitorKpisComponents.Property property = SewMonitorKpisComponents.Property.builder()
                .name("Temperature")
                .lowThreshold(10)
                .highThreshold(90)
                .build();

        SewMonitorKpisComponents.SewMonitorKpisComponentData componentData = SewMonitorKpisComponents.SewMonitorKpisComponentData.builder()
                .stage("Stage1")
                .cell("Cell1")
                .plc("PLC1")
                .module("Module1")
                .subElement("SubElement1")
                .component("Component1")
                .property(List.of(property))
                .build();

        testComponent = SewMonitorKpisComponents.builder()
                .id("TEST_ID_1")
                .moduleId("TEST_MODULE")
                .timestampCreated(LocalDateTime.now())
                .components(List.of(componentData))
                .build();

        anotherComponent = SewMonitorKpisComponents.builder()
                .id("TEST_ID_2")
                .moduleId("ANOTHER_MODULE")
                .timestampCreated(LocalDateTime.now())
                .components(new ArrayList<>())
                .build();
    }

    @Nested
    @DisplayName("When saving and retrieving components")
    class SavingAndRetrieving {

        @Test
        @DisplayName("Save component : Success")
        void givenValidComponent_whenSave_thenReturnSavedComponent() {
            // When
            SewMonitorKpisComponents saved = sewMonitorKpisComponentsRepository.save(testComponent);

            // Then
            assertNotNull(saved);
            assertNotNull(saved.getId());
            assertEquals("TEST_MODULE", saved.getModuleId());
            assertEquals(1, saved.getComponents().size());
        }

        @Test
        @DisplayName("Find by ID : Success")
        void givenSavedComponent_whenFindById_thenReturnComponent() {
            // Given
            SewMonitorKpisComponents saved = sewMonitorKpisComponentsRepository.save(testComponent);

            // When
            Optional<SewMonitorKpisComponents> found = sewMonitorKpisComponentsRepository.findById(saved.getId());

            // Then
            assertTrue(found.isPresent());
            assertEquals("TEST_MODULE", found.get().getModuleId());
            assertEquals(1, found.get().getComponents().size());
        }

        @Test
        @DisplayName("Find by non-existent ID : Return empty")
        void givenNonExistentId_whenFindById_thenReturnEmpty() {
            // When
            Optional<SewMonitorKpisComponents> found = sewMonitorKpisComponentsRepository.findById("NON_EXISTENT_ID");

            // Then
            assertFalse(found.isPresent());
        }

        @Test
        @DisplayName("Find all : Return all saved components")
        void givenMultipleComponents_whenFindAll_thenReturnAllComponents() {
            // Given
            sewMonitorKpisComponentsRepository.save(testComponent);
            sewMonitorKpisComponentsRepository.save(anotherComponent);

            // When
            Iterable<SewMonitorKpisComponents> allComponents = sewMonitorKpisComponentsRepository.findAll();

            // Then
            assertNotNull(allComponents);
            List<SewMonitorKpisComponents> componentList = new ArrayList<>();
            allComponents.forEach(componentList::add);
            assertEquals(2, componentList.size());
        }
    }

    @Nested
    @DisplayName("When finding by module ID")
    class FindByModuleId {

        @Test
        @DisplayName("Find by module ID : Success")
        void givenSavedComponent_whenFindByModuleId_thenReturnComponent() {
            // Given
            sewMonitorKpisComponentsRepository.save(testComponent);

            // When
            Optional<SewMonitorKpisComponents> found = sewMonitorKpisComponentsRepository.findByModuleId("TEST_MODULE");

            // Then
            assertTrue(found.isPresent());
            assertEquals("TEST_MODULE", found.get().getModuleId());
            assertEquals("TEST_ID_1", found.get().getId());
        }

        @Test
        @DisplayName("Find by non-existent module ID : Return empty")
        void givenNonExistentModuleId_whenFindByModuleId_thenReturnEmpty() {
            // Given
            sewMonitorKpisComponentsRepository.save(testComponent);

            // When
            Optional<SewMonitorKpisComponents> found = sewMonitorKpisComponentsRepository.findByModuleId("NON_EXISTENT_MODULE");

            // Then
            assertFalse(found.isPresent());
        }

        @Test
        @DisplayName("Find by module ID with multiple modules : Return correct one")
        void givenMultipleModules_whenFindByModuleId_thenReturnCorrectModule() {
            // Given
            sewMonitorKpisComponentsRepository.save(testComponent);
            sewMonitorKpisComponentsRepository.save(anotherComponent);

            // When
            Optional<SewMonitorKpisComponents> found = sewMonitorKpisComponentsRepository.findByModuleId("ANOTHER_MODULE");

            // Then
            assertTrue(found.isPresent());
            assertEquals("ANOTHER_MODULE", found.get().getModuleId());
            assertEquals("TEST_ID_2", found.get().getId());
        }

        @Test
        @DisplayName("Find by null module ID : Return empty")
        void givenNullModuleId_whenFindByModuleId_thenReturnEmpty() {
            // Given
            sewMonitorKpisComponentsRepository.save(testComponent);

            // When
            Optional<SewMonitorKpisComponents> found = sewMonitorKpisComponentsRepository.findByModuleId(null);

            // Then
            assertFalse(found.isPresent());
        }

        @Test
        @DisplayName("Find by empty module ID : Return empty")
        void givenEmptyModuleId_whenFindByModuleId_thenReturnEmpty() {
            // Given
            sewMonitorKpisComponentsRepository.save(testComponent);

            // When
            Optional<SewMonitorKpisComponents> found = sewMonitorKpisComponentsRepository.findByModuleId("");

            // Then
            assertFalse(found.isPresent());
        }
    }

    @Nested
    @DisplayName("When deleting by module ID")
    class DeleteByModuleId {

        @Test
        @DisplayName("Delete by module ID : Success")
        void givenSavedComponent_whenDeleteByModuleId_thenComponentDeleted() {
            // Given
            sewMonitorKpisComponentsRepository.save(testComponent);
            assertTrue(sewMonitorKpisComponentsRepository.findByModuleId("TEST_MODULE").isPresent());

            // When
            sewMonitorKpisComponentsRepository.deleteByModuleId("TEST_MODULE");

            // Then
            assertFalse(sewMonitorKpisComponentsRepository.findByModuleId("TEST_MODULE").isPresent());
        }

        @Test
        @DisplayName("Delete by module ID with multiple modules : Delete only target module")
        void givenMultipleModules_whenDeleteByModuleId_thenDeleteOnlyTargetModule() {
            // Given
            sewMonitorKpisComponentsRepository.save(testComponent);
            sewMonitorKpisComponentsRepository.save(anotherComponent);
            assertTrue(sewMonitorKpisComponentsRepository.findByModuleId("TEST_MODULE").isPresent());
            assertTrue(sewMonitorKpisComponentsRepository.findByModuleId("ANOTHER_MODULE").isPresent());

            // When
            sewMonitorKpisComponentsRepository.deleteByModuleId("TEST_MODULE");

            // Then
            assertFalse(sewMonitorKpisComponentsRepository.findByModuleId("TEST_MODULE").isPresent());
            assertTrue(sewMonitorKpisComponentsRepository.findByModuleId("ANOTHER_MODULE").isPresent());
        }

        @Test
        @DisplayName("Delete by non-existent module ID : No effect")
        void givenNonExistentModuleId_whenDeleteByModuleId_thenNoEffect() {
            // Given
            sewMonitorKpisComponentsRepository.save(testComponent);
            long countBefore = sewMonitorKpisComponentsRepository.count();

            // When
            sewMonitorKpisComponentsRepository.deleteByModuleId("NON_EXISTENT_MODULE");

            // Then
            long countAfter = sewMonitorKpisComponentsRepository.count();
            assertEquals(countBefore, countAfter);
            assertTrue(sewMonitorKpisComponentsRepository.findByModuleId("TEST_MODULE").isPresent());
        }

        @Test
        @DisplayName("Delete by null module ID : No effect")
        void givenNullModuleId_whenDeleteByModuleId_thenNoEffect() {
            // Given
            sewMonitorKpisComponentsRepository.save(testComponent);
            long countBefore = sewMonitorKpisComponentsRepository.count();

            // When
            assertDoesNotThrow(() -> sewMonitorKpisComponentsRepository.deleteByModuleId(null));

            // Then
            long countAfter = sewMonitorKpisComponentsRepository.count();
            assertEquals(countBefore, countAfter);
        }

        @Test
        @DisplayName("Delete by empty module ID : No effect")
        void givenEmptyModuleId_whenDeleteByModuleId_thenNoEffect() {
            // Given
            sewMonitorKpisComponentsRepository.save(testComponent);
            long countBefore = sewMonitorKpisComponentsRepository.count();

            // When
            sewMonitorKpisComponentsRepository.deleteByModuleId("");

            // Then
            long countAfter = sewMonitorKpisComponentsRepository.count();
            assertEquals(countBefore, countAfter);
        }
    }

    @Nested
    @DisplayName("When handling component data structure")
    class ComponentDataStructure {

        @Test
        @DisplayName("Save component with complex nested data : Success")
        void givenComponentWithComplexData_whenSave_thenPreserveDataStructure() {
            // Given
            SewMonitorKpisComponents.Property property1 = SewMonitorKpisComponents.Property.builder()
                    .name("Temperature")
                    .lowThreshold(0)
                    .highThreshold(100)
                    .build();

            SewMonitorKpisComponents.Property property2 = SewMonitorKpisComponents.Property.builder()
                    .name("Pressure")
                    .lowThreshold(10)
                    .highThreshold(50)
                    .build();

            SewMonitorKpisComponents.SewMonitorKpisComponentData componentData = SewMonitorKpisComponents.SewMonitorKpisComponentData.builder()
                    .stage("ProductionStage")
                    .cell("Cell001")
                    .plc("PLC_MAIN")
                    .module("ModuleX")
                    .subElement("SubElementA")
                    .component("ComponentY")
                    .property(List.of(property1, property2))
                    .build();

            SewMonitorKpisComponents complexComponent = SewMonitorKpisComponents.builder()
                    .moduleId("COMPLEX_MODULE")
                    .timestampCreated(LocalDateTime.now())
                    .components(List.of(componentData))
                    .build();

            // When
            SewMonitorKpisComponents saved = sewMonitorKpisComponentsRepository.save(complexComponent);

            // Then
            assertNotNull(saved);
            assertEquals("COMPLEX_MODULE", saved.getModuleId());
            assertEquals(1, saved.getComponents().size());

            SewMonitorKpisComponents.SewMonitorKpisComponentData savedData = saved.getComponents().get(0);
            assertEquals("ProductionStage", savedData.getStage());
            assertEquals("Cell001", savedData.getCell());
            assertEquals("PLC_MAIN", savedData.getPlc());
            assertEquals("ModuleX", savedData.getModule());
            assertEquals("SubElementA", savedData.getSubElement());
            assertEquals("ComponentY", savedData.getComponent());
            assertEquals(2, savedData.getProperty().size());
        }

        @Test
        @DisplayName("Save component with empty components list : Success")
        void givenComponentWithEmptyList_whenSave_thenSaveSuccessfully() {
            // Given
            SewMonitorKpisComponents emptyComponent = SewMonitorKpisComponents.builder()
                    .moduleId("EMPTY_MODULE")
                    .timestampCreated(LocalDateTime.now())
                    .components(new ArrayList<>())
                    .build();

            // When
            SewMonitorKpisComponents saved = sewMonitorKpisComponentsRepository.save(emptyComponent);

            // Then
            assertNotNull(saved);
            assertEquals("EMPTY_MODULE", saved.getModuleId());
            assertTrue(saved.getComponents().isEmpty());
        }

        @Test
        @DisplayName("Save component with null timestamp : Success")
        void givenComponentWithNullTimestamp_whenSave_thenSaveSuccessfully() {
            // Given
            SewMonitorKpisComponents componentWithNullTimestamp = SewMonitorKpisComponents.builder()
                    .moduleId("NULL_TIMESTAMP_MODULE")
                    .timestampCreated(null)
                    .components(new ArrayList<>())
                    .build();

            // When
            SewMonitorKpisComponents saved = sewMonitorKpisComponentsRepository.save(componentWithNullTimestamp);

            // Then
            assertNotNull(saved);
            assertEquals("NULL_TIMESTAMP_MODULE", saved.getModuleId());
            assertNull(saved.getTimestampCreated());
        }
    }

    @Nested
    @DisplayName("When handling edge cases")
    class EdgeCases {

        @Test
        @DisplayName("Save component with null ID : Generate ID automatically")
        void givenComponentWithNullId_whenSave_thenGenerateIdAutomatically() {
            // Given
            SewMonitorKpisComponents componentWithoutId = SewMonitorKpisComponents.builder()
                    .id(null)
                    .moduleId("AUTO_ID_MODULE")
                    .timestampCreated(LocalDateTime.now())
                    .components(new ArrayList<>())
                    .build();

            // When
            SewMonitorKpisComponents saved = sewMonitorKpisComponentsRepository.save(componentWithoutId);

            // Then
            assertNotNull(saved);
            assertNotNull(saved.getId());
            assertEquals("AUTO_ID_MODULE", saved.getModuleId());
        }

        @Test
        @DisplayName("Count components : Return correct count")
        void givenMultipleComponents_whenCount_thenReturnCorrectCount() {
            // Given
            sewMonitorKpisComponentsRepository.save(testComponent);
            sewMonitorKpisComponentsRepository.save(anotherComponent);

            // When
            long count = sewMonitorKpisComponentsRepository.count();

            // Then
            assertEquals(2, count);
        }

        @Test
        @DisplayName("Delete all components : Remove all components")
        void givenMultipleComponents_whenDeleteAll_thenRemoveAllComponents() {
            // Given
            sewMonitorKpisComponentsRepository.save(testComponent);
            sewMonitorKpisComponentsRepository.save(anotherComponent);
            assertEquals(2, sewMonitorKpisComponentsRepository.count());

            // When
            sewMonitorKpisComponentsRepository.deleteAll();

            // Then
            assertEquals(0, sewMonitorKpisComponentsRepository.count());
        }

        @Test
        @DisplayName("Exists by ID : Return correct existence status")
        void givenSavedComponent_whenExistsById_thenReturnTrue() {
            // Given
            SewMonitorKpisComponents saved = sewMonitorKpisComponentsRepository.save(testComponent);

            // When
            boolean exists = sewMonitorKpisComponentsRepository.existsById(saved.getId());

            // Then
            assertTrue(exists);
        }

        @Test
        @DisplayName("Exists by non-existent ID : Return false")
        void givenNonExistentId_whenExistsById_thenReturnFalse() {
            // When
            boolean exists = sewMonitorKpisComponentsRepository.existsById("NON_EXISTENT_ID");

            // Then
            assertFalse(exists);
        }
    }
}