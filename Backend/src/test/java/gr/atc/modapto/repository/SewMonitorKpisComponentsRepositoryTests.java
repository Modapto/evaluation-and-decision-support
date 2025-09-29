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
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataElasticsearchTest
@ActiveProfiles("test")
@Testcontainers
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
            SewMonitorKpisComponents saved = sewMonitorKpisComponentsRepository.save(testComponent);

            assertNotNull(saved);
            assertNotNull(saved.getId());
            assertEquals("TEST_MODULE", saved.getModuleId());
            assertEquals(1, saved.getComponents().size());
        }

        @Test
        @DisplayName("Find by ID : Success")
        void givenSavedComponent_whenFindById_thenReturnComponent() {
            SewMonitorKpisComponents saved = sewMonitorKpisComponentsRepository.save(testComponent);

            Optional<SewMonitorKpisComponents> found = sewMonitorKpisComponentsRepository.findById(saved.getId());

            assertTrue(found.isPresent());
            assertEquals("TEST_MODULE", found.get().getModuleId());
            assertEquals(1, found.get().getComponents().size());
        }

        @Test
        @DisplayName("Find by non-existent ID : Return empty")
        void givenNonExistentId_whenFindById_thenReturnEmpty() {
            Optional<SewMonitorKpisComponents> found = sewMonitorKpisComponentsRepository.findById("NON_EXISTENT_ID");

            assertFalse(found.isPresent());
        }

        @Test
        @DisplayName("Find all : Return all saved components")
        void givenMultipleComponents_whenFindAll_thenReturnAllComponents() {
            sewMonitorKpisComponentsRepository.save(testComponent);
            sewMonitorKpisComponentsRepository.save(anotherComponent);

            Iterable<SewMonitorKpisComponents> allComponents = sewMonitorKpisComponentsRepository.findAll();

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
            sewMonitorKpisComponentsRepository.save(testComponent);

            Optional<SewMonitorKpisComponents> found = sewMonitorKpisComponentsRepository.findByModuleId("TEST_MODULE");

            assertTrue(found.isPresent());
            assertEquals("TEST_MODULE", found.get().getModuleId());
            assertEquals("TEST_ID_1", found.get().getId());
        }

        @Test
        @DisplayName("Find by non-existent module ID : Return empty")
        void givenNonExistentModuleId_whenFindByModuleId_thenReturnEmpty() {
            sewMonitorKpisComponentsRepository.save(testComponent);

            Optional<SewMonitorKpisComponents> found = sewMonitorKpisComponentsRepository.findByModuleId("NON_EXISTENT_MODULE");

            assertFalse(found.isPresent());
        }

        @Test
        @DisplayName("Find by module ID with multiple modules : Return correct one")
        void givenMultipleModules_whenFindByModuleId_thenReturnCorrectModule() {
            sewMonitorKpisComponentsRepository.save(testComponent);
            sewMonitorKpisComponentsRepository.save(anotherComponent);

            Optional<SewMonitorKpisComponents> found = sewMonitorKpisComponentsRepository.findByModuleId("ANOTHER_MODULE");

            assertTrue(found.isPresent());
            assertEquals("ANOTHER_MODULE", found.get().getModuleId());
            assertEquals("TEST_ID_2", found.get().getId());
        }

        @Test
        @DisplayName("Find by null module ID : Return empty")
        void givenNullModuleId_whenFindByModuleId_thenReturnEmpty() {
            sewMonitorKpisComponentsRepository.save(testComponent);

            Optional<SewMonitorKpisComponents> found = sewMonitorKpisComponentsRepository.findByModuleId(null);

            assertFalse(found.isPresent());
        }

        @Test
        @DisplayName("Find by empty module ID : Return empty")
        void givenEmptyModuleId_whenFindByModuleId_thenReturnEmpty() {
            sewMonitorKpisComponentsRepository.save(testComponent);

            Optional<SewMonitorKpisComponents> found = sewMonitorKpisComponentsRepository.findByModuleId("");

            assertFalse(found.isPresent());
        }
    }

    @Nested
    @DisplayName("When deleting by module ID")
    class DeleteByModuleId {

        @Test
        @DisplayName("Delete by module ID : Success")
        void givenSavedComponent_whenDeleteByModuleId_thenComponentDeleted() {
            sewMonitorKpisComponentsRepository.save(testComponent);
            assertTrue(sewMonitorKpisComponentsRepository.findByModuleId("TEST_MODULE").isPresent());

            sewMonitorKpisComponentsRepository.deleteByModuleId("TEST_MODULE");

            assertFalse(sewMonitorKpisComponentsRepository.findByModuleId("TEST_MODULE").isPresent());
        }

        @Test
        @DisplayName("Delete by module ID with multiple modules : Delete only target module")
        void givenMultipleModules_whenDeleteByModuleId_thenDeleteOnlyTargetModule() {
            sewMonitorKpisComponentsRepository.save(testComponent);
            sewMonitorKpisComponentsRepository.save(anotherComponent);
            assertTrue(sewMonitorKpisComponentsRepository.findByModuleId("TEST_MODULE").isPresent());
            assertTrue(sewMonitorKpisComponentsRepository.findByModuleId("ANOTHER_MODULE").isPresent());

            sewMonitorKpisComponentsRepository.deleteByModuleId("TEST_MODULE");

            assertFalse(sewMonitorKpisComponentsRepository.findByModuleId("TEST_MODULE").isPresent());
            assertTrue(sewMonitorKpisComponentsRepository.findByModuleId("ANOTHER_MODULE").isPresent());
        }

        @Test
        @DisplayName("Delete by non-existent module ID : No effect")
        void givenNonExistentModuleId_whenDeleteByModuleId_thenNoEffect() {
            sewMonitorKpisComponentsRepository.save(testComponent);
            long countBefore = sewMonitorKpisComponentsRepository.count();

            sewMonitorKpisComponentsRepository.deleteByModuleId("NON_EXISTENT_MODULE");

            long countAfter = sewMonitorKpisComponentsRepository.count();
            assertEquals(countBefore, countAfter);
            assertTrue(sewMonitorKpisComponentsRepository.findByModuleId("TEST_MODULE").isPresent());
        }

        @Test
        @DisplayName("Delete by null module ID : No effect")
        void givenNullModuleId_whenDeleteByModuleId_thenNoEffect() {
            sewMonitorKpisComponentsRepository.save(testComponent);
            long countBefore = sewMonitorKpisComponentsRepository.count();

            assertDoesNotThrow(() -> sewMonitorKpisComponentsRepository.deleteByModuleId(null));

            long countAfter = sewMonitorKpisComponentsRepository.count();
            assertEquals(countBefore, countAfter);
        }

        @Test
        @DisplayName("Delete by empty module ID : No effect")
        void givenEmptyModuleId_whenDeleteByModuleId_thenNoEffect() {
            sewMonitorKpisComponentsRepository.save(testComponent);
            long countBefore = sewMonitorKpisComponentsRepository.count();

            sewMonitorKpisComponentsRepository.deleteByModuleId("");

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

            SewMonitorKpisComponents saved = sewMonitorKpisComponentsRepository.save(complexComponent);

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
            SewMonitorKpisComponents emptyComponent = SewMonitorKpisComponents.builder()
                    .moduleId("EMPTY_MODULE")
                    .timestampCreated(LocalDateTime.now())
                    .components(new ArrayList<>())
                    .build();

            SewMonitorKpisComponents saved = sewMonitorKpisComponentsRepository.save(emptyComponent);

            assertNotNull(saved);
            assertEquals("EMPTY_MODULE", saved.getModuleId());
            assertTrue(saved.getComponents().isEmpty());
        }

        @Test
        @DisplayName("Save component with null timestamp : Success")
        void givenComponentWithNullTimestamp_whenSave_thenSaveSuccessfully() {
            SewMonitorKpisComponents componentWithNullTimestamp = SewMonitorKpisComponents.builder()
                    .moduleId("NULL_TIMESTAMP_MODULE")
                    .timestampCreated(null)
                    .components(new ArrayList<>())
                    .build();

            SewMonitorKpisComponents saved = sewMonitorKpisComponentsRepository.save(componentWithNullTimestamp);

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
            SewMonitorKpisComponents componentWithoutId = SewMonitorKpisComponents.builder()
                    .id(null)
                    .moduleId("AUTO_ID_MODULE")
                    .timestampCreated(LocalDateTime.now())
                    .components(new ArrayList<>())
                    .build();

            SewMonitorKpisComponents saved = sewMonitorKpisComponentsRepository.save(componentWithoutId);

            assertNotNull(saved);
            assertNotNull(saved.getId());
            assertEquals("AUTO_ID_MODULE", saved.getModuleId());
        }

        @Test
        @DisplayName("Count components : Return correct count")
        void givenMultipleComponents_whenCount_thenReturnCorrectCount() {
            sewMonitorKpisComponentsRepository.save(testComponent);
            sewMonitorKpisComponentsRepository.save(anotherComponent);

            long count = sewMonitorKpisComponentsRepository.count();

            assertEquals(2, count);
        }

        @Test
        @DisplayName("Delete all components : Remove all components")
        void givenMultipleComponents_whenDeleteAll_thenRemoveAllComponents() {
            sewMonitorKpisComponentsRepository.save(testComponent);
            sewMonitorKpisComponentsRepository.save(anotherComponent);
            assertEquals(2, sewMonitorKpisComponentsRepository.count());

            sewMonitorKpisComponentsRepository.deleteAll();

            assertEquals(0, sewMonitorKpisComponentsRepository.count());
        }

        @Test
        @DisplayName("Exists by ID : Return correct existence status")
        void givenSavedComponent_whenExistsById_thenReturnTrue() {
            SewMonitorKpisComponents saved = sewMonitorKpisComponentsRepository.save(testComponent);

            boolean exists = sewMonitorKpisComponentsRepository.existsById(saved.getId());

            assertTrue(exists);
        }

        @Test
        @DisplayName("Exists by non-existent ID : Return false")
        void givenNonExistentId_whenExistsById_thenReturnFalse() {
            boolean exists = sewMonitorKpisComponentsRepository.existsById("NON_EXISTENT_ID");

            assertFalse(exists);
        }
    }
}