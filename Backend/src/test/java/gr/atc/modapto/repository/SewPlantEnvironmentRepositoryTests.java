package gr.atc.modapto.repository;

import gr.atc.modapto.config.SetupTestContainersEnvironment;
import gr.atc.modapto.model.sew.SewPlantEnvironment;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.elasticsearch.DataElasticsearchTest;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataElasticsearchTest
@ActiveProfiles("test")
@Testcontainers
@DisplayName("SewPlantEnvironmentRepository Tests")
class SewPlantEnvironmentRepositoryTests extends SetupTestContainersEnvironment {

    @Autowired
    private SewPlantEnvironmentRepository sewPlantEnvironmentRepository;

    private SewPlantEnvironment testEnvironment;

    @BeforeEach
    void setUp() {
        sewPlantEnvironmentRepository.deleteAll();

        Map<String, Object> cells = new HashMap<>();
        cells.put("Cell1", new HashMap<>());
        cells.put("Cell2", new HashMap<>());

        SewPlantEnvironment.Stage stage = SewPlantEnvironment.Stage.builder()
                .wipIn(5)
                .wipOut(3)
                .modules("ModuleA")
                .cells(cells)
                .build();

        Map<String, SewPlantEnvironment.Stage> stages = new HashMap<>();
        stages.put("Stage1", stage);

        Map<String, Map<String, Integer>> transTimes = new HashMap<>();
        Map<String, Integer> transTime1 = new HashMap<>();
        transTime1.put("Cell2", 10);
        transTimes.put("Cell1", transTime1);

        testEnvironment = SewPlantEnvironment.builder()
                .id("TEST_ENV_1")
                .timestampCreated(LocalDateTime.now())
                .stages(stages)
                .transTimes(transTimes)
                .build();
    }

    @Test
    @DisplayName("Save and retrieve environment : Success")
    void givenValidEnvironment_whenSaveAndRetrieve_thenReturnSavedEnvironment() {
        //Given
        SewPlantEnvironment saved = sewPlantEnvironmentRepository.save(testEnvironment);

        //When
        Optional<SewPlantEnvironment> found = sewPlantEnvironmentRepository.findById(saved.getId());

        //Then
        assertTrue(found.isPresent());
        assertEquals("TEST_ENV_1", found.get().getId());
        assertEquals(1, found.get().getStages().size());
        assertEquals(1, found.get().getTransTimes().size());
    }

    @Test
    @DisplayName("Find latest environment : Success")
    void givenMultipleEnvironments_whenFindFirstByOrderByTimestampCreatedDesc_thenReturnLatest() {
        //Given
        SewPlantEnvironment older = SewPlantEnvironment.builder()
                .timestampCreated(LocalDateTime.now().minusHours(2))
                .stages(new HashMap<>())
                .transTimes(new HashMap<>())
                .build();

        SewPlantEnvironment newer = SewPlantEnvironment.builder()
                .timestampCreated(LocalDateTime.now())
                .stages(new HashMap<>())
                .transTimes(new HashMap<>())
                .build();

        sewPlantEnvironmentRepository.save(older);
        SewPlantEnvironment savedNewer = sewPlantEnvironmentRepository.save(newer);

        //When
        Optional<SewPlantEnvironment> found = sewPlantEnvironmentRepository.findFirstByOrderByTimestampCreatedDesc();

        //Then
        assertTrue(found.isPresent());
        assertEquals(savedNewer.getId(), found.get().getId());
    }

    @Test
    @DisplayName("Delete environment : Success")
    void givenSavedEnvironment_whenDelete_thenEnvironmentDeleted() {
        //Given
        SewPlantEnvironment saved = sewPlantEnvironmentRepository.save(testEnvironment);
        assertTrue(sewPlantEnvironmentRepository.findById(saved.getId()).isPresent());

        //When
        sewPlantEnvironmentRepository.deleteById(saved.getId());

        //Then
        assertFalse(sewPlantEnvironmentRepository.findById(saved.getId()).isPresent());
    }
}
