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

        Map<String, Object> stages = new HashMap<>();
        stages.put("stage1", Map.of("wipIn", 5, "wipOut", 3));

        Map<String, Object> transTimes = new HashMap<>();
        transTimes.put("transition1", Map.of("time", 10));

        SewPlantEnvironment.PlantData plantData = new SewPlantEnvironment.PlantData(stages, transTimes);

        testEnvironment = SewPlantEnvironment.builder()
                .id("TEST_ENV_1")
                .timestampCreated(LocalDateTime.now())
                .data(plantData)
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
        assertNotNull(found.get().getData());
    }

    @Test
    @DisplayName("Find latest environment : Success")
    void givenMultipleEnvironments_whenFindFirstByOrderByTimestampCreatedDesc_thenReturnLatest() {
        //Given
        Map<String, Object> stages = new HashMap<>();
        stages.put("stage1", Map.of("wipIn", 5, "wipOut", 3));

        Map<String, Object> transTimes = new HashMap<>();
        transTimes.put("transition1", Map.of("time", 10));

        SewPlantEnvironment older = SewPlantEnvironment.builder()
                .timestampCreated(LocalDateTime.now().minusHours(2))
                .data(new SewPlantEnvironment.PlantData(stages, transTimes))
                .build();

        SewPlantEnvironment newer = SewPlantEnvironment.builder()
                .timestampCreated(LocalDateTime.now())
                .data(new SewPlantEnvironment.PlantData(stages, transTimes))
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
