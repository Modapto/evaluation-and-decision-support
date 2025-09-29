package gr.atc.modapto.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import gr.atc.modapto.config.properties.KeycloakProperties;
import gr.atc.modapto.dto.dt.DtResponseDto;
import gr.atc.modapto.dto.serviceInvocations.SewThresholdBasedMaintenanceInputDataDto;
import gr.atc.modapto.enums.ModaptoHeader;
import gr.atc.modapto.exception.CustomExceptions;
import gr.atc.modapto.exception.CustomExceptions.SmartServiceInvocationException;
import gr.atc.modapto.service.interfaces.IModaptoModuleService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import static gr.atc.modapto.exception.CustomExceptions.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
// FIX: Add required import for eq()
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("SmartServicesInvocationService Unit Tests")
class SmartServicesInvocationServiceTests {

    private static final String TEST_DTM_URL = "https://dtm.example.com";
    private static final String TEST_TOKEN = "sample-jwt-token";
    private static final String TEST_MODULE_ID = "TEST_MODULE";
    private static final String TEST_SERVICE_ID = "THRESHOLD_SERVICE";

    @Mock
    private RestClient restClient;

    @Mock
    private KeycloakProperties keycloakProperties;

    @Mock
    private IModaptoModuleService modaptoModuleService;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private RestClient.RequestBodyUriSpec requestBodyUriSpec;
    @Mock
    private RestClient.RequestBodySpec requestBodySpec;
    @Mock
    private RestClient.ResponseSpec responseSpec;

    @InjectMocks
    private SmartServicesInvocationService smartServicesInvocationService;

    private SewThresholdBasedMaintenanceInputDataDto sampleInputData;
    private ResponseEntity<DtResponseDto> sampleResponse;
    private Map<String, Object> sampleTokenResponse;
    private MultiValueMap<String, String> expectedTokenForm;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(smartServicesInvocationService, "dtmUrl", TEST_DTM_URL);

        sampleInputData = SewThresholdBasedMaintenanceInputDataDto.builder()
                .moduleId(TEST_MODULE_ID)
                .smartServiceId(TEST_SERVICE_ID)
                .build();

        DtResponseDto sampleDtResponse = DtResponseDto.builder()
                .success(true)
                .executionState("Completed")
                .outputArguments(new HashMap<>())
                .build();
        sampleResponse = new ResponseEntity<>(sampleDtResponse, HttpStatus.OK);

        sampleTokenResponse = new HashMap<>();
        sampleTokenResponse.put("access_token", TEST_TOKEN);
        sampleTokenResponse.put("token_type", "Bearer");
        sampleTokenResponse.put("expires_in", 3600);

        // Response Body for Token
        expectedTokenForm = new LinkedMultiValueMap<>();
        expectedTokenForm.add("grant_type", "client_credentials");
        expectedTokenForm.add("client_id", "test-client-id");
        expectedTokenForm.add("client_secret", "test-client-secret");

        lenient().when(keycloakProperties.clientId()).thenReturn("test-client-id");
        lenient().when(keycloakProperties.clientSecret()).thenReturn("test-client-secret");
        lenient().when(keycloakProperties.tokenUri()).thenReturn("https://keycloak.example.com/auth/realms/test/protocol/openid-connect/token");
    }

    private void stubSuccessfulTokenRetrieval() {
        ResponseEntity<Map<String, Object>> tokenEntity = new ResponseEntity<>(sampleTokenResponse, HttpStatus.OK);

        // This chain mocks the first call to restClient.post() in retrieveComponentJwtToken()
        when(restClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(keycloakProperties.tokenUri())).thenReturn(requestBodySpec);
        when(requestBodySpec.contentType(MediaType.APPLICATION_FORM_URLENCODED)).thenReturn(requestBodySpec);
        when(requestBodySpec.body(eq(expectedTokenForm))).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.toEntity(any(ParameterizedTypeReference.class))).thenReturn(tokenEntity);
    }

    @Nested
    @DisplayName("JWT Token Retrieval")
    class JwtTokenRetrieval {

        @Test
        @DisplayName("Retrieve JWT token : Success (Full Invocation)")
        void givenValidKeycloakConfig_whenRetrieveToken_thenReturnsTokenSuccessfully() {
            stubSuccessfulTokenRetrieval();
            when(modaptoModuleService.retrieveSmartServiceUrl(TEST_MODULE_ID, TEST_SERVICE_ID))
                    .thenReturn(TEST_DTM_URL + "/api/services/threshold");

            when(requestBodyUriSpec.uri("/api/services/threshold/invoke/$value")).thenReturn(requestBodySpec);
            when(requestBodySpec.header("Authorization", "Bearer " + TEST_TOKEN)).thenReturn(requestBodySpec);
            when(requestBodySpec.header("X-MODAPTO-Invocation-Id", "sync")).thenReturn(requestBodySpec);
            // FIX: Add a specific stub for the second .body() call
            when(requestBodySpec.body(eq(sampleInputData))).thenReturn(requestBodySpec);
            when(responseSpec.onStatus(any(), any())).thenReturn(responseSpec);
            when(responseSpec.toEntity(DtResponseDto.class)).thenReturn(sampleResponse);

            ResponseEntity<DtResponseDto> result = smartServicesInvocationService.invokeSmartService(
                    TEST_SERVICE_ID, TEST_MODULE_ID, sampleInputData, ModaptoHeader.SYNC);

            assertThat(result).isNotNull();
            assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
            verify(modaptoModuleService).retrieveSmartServiceUrl(TEST_MODULE_ID, TEST_SERVICE_ID);
        }

        @Test
        @DisplayName("Retrieve JWT token : Authentication failure")
        void givenAuthenticationFailure_whenRetrieveToken_thenThrowsException() {
            when(restClient.post()).thenReturn(requestBodyUriSpec);
            when(requestBodyUriSpec.uri(keycloakProperties.tokenUri())).thenReturn(requestBodySpec);
            when(requestBodySpec.contentType(MediaType.APPLICATION_FORM_URLENCODED)).thenReturn(requestBodySpec);
            when(requestBodySpec.body(eq(expectedTokenForm))).thenReturn(requestBodySpec);
            when(requestBodySpec.retrieve()).thenReturn(responseSpec);
            when(responseSpec.toEntity(any(ParameterizedTypeReference.class)))
                    .thenThrow(new RestClientException("Authentication failed"));

            assertThatThrownBy(() -> smartServicesInvocationService.invokeSmartService(
                    TEST_SERVICE_ID, TEST_MODULE_ID, sampleInputData, ModaptoHeader.SYNC))
                    .isInstanceOf(SmartServiceInvocationException.class)
                    .hasMessage("Failed to retrieve JWT token for DTM authentication");
        }

        @Test
        @DisplayName("Retrieve JWT token : No token in response")
        void givenNoTokenInResponse_whenRetrieveToken_thenThrowsException() {
            ResponseEntity<Map<String, Object>> emptyTokenEntity = new ResponseEntity<>(new HashMap<>(), HttpStatus.OK);

            when(restClient.post()).thenReturn(requestBodyUriSpec);
            when(requestBodyUriSpec.uri(keycloakProperties.tokenUri())).thenReturn(requestBodySpec);
            when(requestBodySpec.contentType(MediaType.APPLICATION_FORM_URLENCODED)).thenReturn(requestBodySpec);
            when(requestBodySpec.body(eq(expectedTokenForm))).thenReturn(requestBodySpec);
            when(requestBodySpec.retrieve()).thenReturn(responseSpec);
            when(responseSpec.toEntity(any(ParameterizedTypeReference.class))).thenReturn(emptyTokenEntity);

            assertThatThrownBy(() -> smartServicesInvocationService.invokeSmartService(
                    TEST_SERVICE_ID, TEST_MODULE_ID, sampleInputData, ModaptoHeader.SYNC))
                    .isInstanceOf(SmartServiceInvocationException.class)
                    .hasMessage("Failed to retrieve JWT token for DTM authentication");
        }
    }

    @Nested
    @DisplayName("Smart Service Invocation - Synchronous (Threshold) Maintenance")
    class SynchronousMaintenanceInvocation {

        @Test
        @DisplayName("Invoke threshold maintenance service : Success")
        void givenValidInput_whenInvokeThresholdMaintenance_thenReturnsSuccess() {
            stubSuccessfulTokenRetrieval();

            when(modaptoModuleService.retrieveSmartServiceUrl(TEST_MODULE_ID, TEST_SERVICE_ID))
                    .thenReturn(TEST_DTM_URL + "/api/services/threshold");
            when(requestBodyUriSpec.uri("/api/services/threshold/invoke/$value")).thenReturn(requestBodySpec);
            when(requestBodySpec.header("Authorization", "Bearer " + TEST_TOKEN)).thenReturn(requestBodySpec);
            when(requestBodySpec.header("X-MODAPTO-Invocation-Id", "sync")).thenReturn(requestBodySpec);
            when(requestBodySpec.body(eq(sampleInputData))).thenReturn(requestBodySpec);
            when(responseSpec.onStatus(any(), any())).thenReturn(responseSpec);
            when(responseSpec.toEntity(DtResponseDto.class)).thenReturn(sampleResponse);

            ResponseEntity<DtResponseDto> result = smartServicesInvocationService.invokeSmartService(
                    TEST_SERVICE_ID, TEST_MODULE_ID, sampleInputData, ModaptoHeader.SYNC);

            assertThat(result).isNotNull();
            assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(result.getBody()).isNotNull();
            assertThat(result.getBody().isSuccess()).isTrue();
            verify(modaptoModuleService).retrieveSmartServiceUrl(TEST_MODULE_ID, TEST_SERVICE_ID);
        }

        @Test
        @DisplayName("Invoke service : DTM returns 4xx Client Error")
        void givenDtmReturns4xxError_whenInvoke_thenThrowsDtmClientErrorException() {
            stubSuccessfulTokenRetrieval();

            when(modaptoModuleService.retrieveSmartServiceUrl(TEST_MODULE_ID, TEST_SERVICE_ID))
                    .thenReturn(TEST_DTM_URL + "/api/services/threshold");

            when(requestBodyUriSpec.uri("/api/services/threshold/invoke/$value")).thenReturn(requestBodySpec);
            when(requestBodySpec.header(any(), any())).thenReturn(requestBodySpec);
            when(requestBodySpec.body(eq(sampleInputData))).thenReturn(requestBodySpec);
            when(responseSpec.onStatus(any(), any())).thenReturn(responseSpec);
            when(responseSpec.toEntity(DtResponseDto.class))
                    .thenThrow(new DtmClientErrorException("Client error invoking smart service: " + TEST_SERVICE_ID));

            assertThatThrownBy(() -> smartServicesInvocationService.invokeSmartService(
                    TEST_SERVICE_ID, TEST_MODULE_ID, sampleInputData, ModaptoHeader.SYNC))
                    .isInstanceOf(SmartServiceInvocationException.class)
                    .hasMessageContaining("Client error invoking smart service");
        }

        // NEW TEST: 2
        @Test
        @DisplayName("Invoke service : DTM returns 5xx Server Error")
        void givenDtmReturns5xxError_whenInvoke_thenThrowsDtmServerErrorException() {
            stubSuccessfulTokenRetrieval();

            when(modaptoModuleService.retrieveSmartServiceUrl(TEST_MODULE_ID, TEST_SERVICE_ID))
                    .thenReturn(TEST_DTM_URL + "/api/services/threshold");

            when(requestBodyUriSpec.uri("/api/services/threshold/invoke/$value")).thenReturn(requestBodySpec);
            when(requestBodySpec.header(any(), any())).thenReturn(requestBodySpec);
            when(requestBodySpec.body(eq(sampleInputData))).thenReturn(requestBodySpec);

            // Simulate the 5xx error
            when(responseSpec.onStatus(any(), any())).thenReturn(responseSpec);
            when(responseSpec.toEntity(DtResponseDto.class))
                    .thenThrow(new DtmServerErrorException("Server error invoking smart service: " + TEST_SERVICE_ID));

            assertThatThrownBy(() -> smartServicesInvocationService.invokeSmartService(
                    TEST_SERVICE_ID, TEST_MODULE_ID, sampleInputData, ModaptoHeader.SYNC))
                    .isInstanceOf(SmartServiceInvocationException.class)
                    .hasMessageContaining("Server error invoking smart service");
        }
    }

    @Nested
    @DisplayName("Smart Service Invocation - Asynchronous (Grouping) Maintenance")
    class AsynchronousMaintenanceInvocation {

        @Test
        @DisplayName("Invoke grouping maintenance service : Success")
        void givenValidInput_whenInvokeGroupingMaintenance_thenReturnsSuccess() {
            stubSuccessfulTokenRetrieval();

            String groupingModuleId = "GROUPING_MODULE";
            String groupingServiceId = "GROUPING_SERVICE";
            when(modaptoModuleService.retrieveSmartServiceUrl(groupingModuleId, groupingServiceId))
                    .thenReturn(TEST_DTM_URL + "/api/services/grouping");
            when(requestBodyUriSpec.uri("/api/services/grouping/invoke/$value")).thenReturn(requestBodySpec);
            when(requestBodySpec.header("Authorization", "Bearer " + TEST_TOKEN)).thenReturn(requestBodySpec);
            when(requestBodySpec.header("X-MODAPTO-Invocation-Id", "async")).thenReturn(requestBodySpec);
            when(requestBodySpec.body(eq(sampleInputData))).thenReturn(requestBodySpec);
            when(responseSpec.onStatus(any(), any())).thenReturn(responseSpec);
            when(responseSpec.toEntity(DtResponseDto.class)).thenReturn(sampleResponse);

            ResponseEntity<DtResponseDto> result = smartServicesInvocationService.invokeSmartService(
                    groupingServiceId, groupingModuleId, sampleInputData, ModaptoHeader.ASYNC);

            assertThat(result).isNotNull();
            assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(result.getBody()).isNotNull();
            assertThat(result.getBody().isSuccess()).isTrue();
            verify(modaptoModuleService).retrieveSmartServiceUrl(groupingModuleId, groupingServiceId);
        }
    }

    @Nested
    @DisplayName("Input Validation")
    class InputValidation {

        @Test
        @DisplayName("Invoke service : Null smart service ID")
        void givenNullSmartServiceId_whenInvoke_thenThrowsSmartServiceInvocationException() {
            assertThatThrownBy(() -> smartServicesInvocationService.invokeSmartService(
                    null, TEST_MODULE_ID, sampleInputData, ModaptoHeader.SYNC))
                    .isInstanceOf(SmartServiceInvocationException.class)
                    .hasMessage("Smart service ID cannot be null or empty");
        }

        @Test
        @DisplayName("Invoke service : Empty smart service ID")
        void givenEmptySmartServiceId_whenInvoke_thenThrowsSmartServiceInvocationException() {
            assertThatThrownBy(() -> smartServicesInvocationService.invokeSmartService(
                    "  ", TEST_MODULE_ID, sampleInputData, ModaptoHeader.SYNC))
                    .isInstanceOf(SmartServiceInvocationException.class)
                    .hasMessage("Smart service ID cannot be null or empty");
        }

        @Test
        @DisplayName("Invoke service : Null module ID")
        void givenNullModuleId_whenInvoke_thenThrowsSmartServiceInvocationException() {
            assertThatThrownBy(() -> smartServicesInvocationService.invokeSmartService(
                    TEST_SERVICE_ID, null, sampleInputData, ModaptoHeader.SYNC))
                    .isInstanceOf(SmartServiceInvocationException.class)
                    .hasMessage("Module ID cannot be null or empty");
        }

        @Test
        @DisplayName("Invoke service : Empty module ID")
        void givenEmptyModuleId_whenInvoke_thenThrowsSmartServiceInvocationException() {
            assertThatThrownBy(() -> smartServicesInvocationService.invokeSmartService(
                    TEST_SERVICE_ID, "  ", sampleInputData, ModaptoHeader.SYNC))
                    .isInstanceOf(SmartServiceInvocationException.class)
                    .hasMessage("Module ID cannot be null or empty");
        }

        @Test
        @DisplayName("Invoke service : Null invocation data")
        void givenNullInvocationData_whenInvoke_thenThrowsSmartServiceInvocationException() {
            assertThatThrownBy(() -> smartServicesInvocationService.invokeSmartService(
                    TEST_SERVICE_ID, TEST_MODULE_ID, null, ModaptoHeader.SYNC))
                    .isInstanceOf(SmartServiceInvocationException.class)
                    .hasMessage("Invocation data cannot be null");
        }

        @Test
        @DisplayName("Invoke service : Null MODAPTO header")
        void givenNullModaptoHeader_whenInvoke_thenThrowsSmartServiceInvocationException() {
            assertThatThrownBy(() -> smartServicesInvocationService.invokeSmartService(
                    TEST_SERVICE_ID, TEST_MODULE_ID, sampleInputData, null))
                    .isInstanceOf(SmartServiceInvocationException.class)
                    .hasMessage("MODAPTO header cannot be null");
        }
    }

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCases {

        @Test
        @DisplayName("Invoke smart service : Invalid smart service URL")
        void givenInvalidSmartServiceUrl_whenInvokeSmartService_thenThrowsException() {
            stubSuccessfulTokenRetrieval();

            when(modaptoModuleService.retrieveSmartServiceUrl("INVALID_MODULE", "INVALID_SERVICE"))
                    .thenReturn("https://different-host.com/api/services/invalid");

            assertThatThrownBy(() -> smartServicesInvocationService.invokeSmartService(
                    "INVALID_SERVICE", "INVALID_MODULE", sampleInputData, ModaptoHeader.SYNC))
                    .isInstanceOf(SmartServiceInvocationException.class)
                    .hasMessageContaining("Invalid smart service URL");

            verify(modaptoModuleService).retrieveSmartServiceUrl("INVALID_MODULE", "INVALID_SERVICE");
        }
    }

    // The integration is tested through the other service tests that call this method

    @Nested
    @DisplayName("Edge Cases Continued")
    class EdgeCasesContinued {

        @Test
        @DisplayName("Invoke service : Module service fails to retrieve URL")
        void givenModuleServiceFails_whenInvoke_thenThrowsDtmClientErrorException() {
            stubSuccessfulTokenRetrieval();

            // Throw an exception
            String errorMsg = "Underlying database is down";
            when(modaptoModuleService.retrieveSmartServiceUrl(TEST_MODULE_ID, TEST_SERVICE_ID))
                    .thenThrow(new RuntimeException(errorMsg));

            assertThatThrownBy(() -> smartServicesInvocationService.invokeSmartService(
                    TEST_SERVICE_ID, TEST_MODULE_ID, sampleInputData, ModaptoHeader.SYNC))
                    .isInstanceOf(DtmClientErrorException.class)
                    .hasMessage("Failed to retrieve smart service URL: " + errorMsg);
        }

        @Test
        @DisplayName("Invoke service : ResourceNotFoundException from module service")
        void givenResourceNotFoundException_whenInvoke_thenThrowsDtmClientErrorException() {
            stubSuccessfulTokenRetrieval();

            ResourceNotFoundException resourceNotFoundException = new ResourceNotFoundException("Module not found");
            when(modaptoModuleService.retrieveSmartServiceUrl(TEST_MODULE_ID, TEST_SERVICE_ID))
                    .thenThrow(resourceNotFoundException);

            assertThatThrownBy(() -> smartServicesInvocationService.invokeSmartService(
                    TEST_SERVICE_ID, TEST_MODULE_ID, sampleInputData, ModaptoHeader.SYNC))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage("Module not found");
        }

        @Test
        @DisplayName("Invoke service : Retrieved URL is null")
        void givenNullRetrievedUrl_whenInvoke_thenThrowsDtmClientErrorException() {
            stubSuccessfulTokenRetrieval();

            when(modaptoModuleService.retrieveSmartServiceUrl(TEST_MODULE_ID, TEST_SERVICE_ID))
                    .thenReturn(null);

            assertThatThrownBy(() -> smartServicesInvocationService.invokeSmartService(
                    TEST_SERVICE_ID, TEST_MODULE_ID, sampleInputData, ModaptoHeader.SYNC))
                    .isInstanceOf(DtmClientErrorException.class)
                    .hasMessage("Failed to retrieve smart service URL: Retrieved smart service URL is null or empty for service: " + TEST_SERVICE_ID + " and module: " + TEST_MODULE_ID);
        }

        @Test
        @DisplayName("Invoke service : Retrieved URL is empty")
        void givenEmptyRetrievedUrl_whenInvoke_thenThrowsDtmClientErrorException() {
            stubSuccessfulTokenRetrieval();

            when(modaptoModuleService.retrieveSmartServiceUrl(TEST_MODULE_ID, TEST_SERVICE_ID))
                    .thenReturn("  ");

            assertThatThrownBy(() -> smartServicesInvocationService.invokeSmartService(
                    TEST_SERVICE_ID, TEST_MODULE_ID, sampleInputData, ModaptoHeader.SYNC))
                    .isInstanceOf(DtmClientErrorException.class)
                    .hasMessage("Failed to retrieve smart service URL: Retrieved smart service URL is null or empty for service: " + TEST_SERVICE_ID + " and module: " + TEST_MODULE_ID);
        }
    }
}