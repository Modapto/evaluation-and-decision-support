package gr.atc.modapto;

import gr.atc.modapto.repository.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchTemplate;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
@ActiveProfiles(profiles = "test")
class ModaptoEvaluationAndDecisionSupportApplicationTests {

	@MockitoBean
	private JwtDecoder jwtDecoder;

	@MockitoBean
	private ElasticsearchTemplate elasticsearchTemplate;

	@MockitoBean
	private MaintenanceDataRepository maintenanceDataRepository;

	@MockitoBean
	private OrderRepository orderRepository;

	@MockitoBean
	private CrfSimulationResultsRepository crfSimulationResultsRepository;

	@MockitoBean
	private CrfOptimizationResultsRepository crfOptimizationResultsRepository;

	@MockitoBean
	private SewOptimizationResultsRepository sewOptimizationResultsRepository;

	@MockitoBean
	private SewSimulationResultsRepository sewSimulationResultsRepository;

	@MockitoBean
	private ModaptoModuleRepository modaptoModuleRepository;

	@MockitoBean
	private ProductionScheduleRepository productionScheduleRepository;

	@MockitoBean
	private SewComponentInfoRepository sewComponentInfoRepository;

	@MockitoBean
	private ScheduledTaskRepository scheduledTaskRepository;

	@MockitoBean
	private SewGroupingBasedPredictiveMaintenanceRepository sewGroupingBasedPredictiveMaintenanceRepository;

	@MockitoBean
	private SewThresholdBasedPredictiveMaintenanceRepository sewThresholdBasedPredictiveMaintenanceRepository;

	@MockitoBean
	private SewSelfAwarenessMonitoringKpisResultsRepository sewSelfAwarenessMonitoringKpisResultsRepository;

    @MockitoBean
    private SewSelfAwarenessRealTimeMonitoringResultsRepository sewSelfAwarenessRealTimeMonitoringResultsRepository;

    @MockitoBean
    private SewMonitorKpisComponentsRepository sewMonitorKpisComponentsRepository;

    @MockitoBean
    private CrfKitHolderEventRepository crfKitHolderEventRepository;

    @MockitoBean
    private CrfSimulationKittingConfigRepository crfSimulationKittingConfigRepository;

    @MockitoBean
    private CrfOptimizationKittingConfigRepository crfOptimizationKittingConfigRepository;

    @MockitoBean
    private SewPlantEnvironmentRepository sewPlantEnvironmentRepository;

	@Test
	void contextLoads() {
		Assertions.assertNotNull(ApplicationContext.class);
	}

}
