package gr.atc.modapto.service.interfaces;

import gr.atc.modapto.dto.serviceInvocations.SewSelfAwarenessMonitoringKpisInputDto;
import gr.atc.modapto.dto.serviceInvocations.SewSelfAwarenessRealTimeMonitoringInputDto;
import gr.atc.modapto.dto.serviceResults.sew.SewSelfAwarenessMonitoringKpisResultsDto;
import gr.atc.modapto.dto.serviceResults.sew.SewSelfAwarenessRealTimeMonitoringResultsDto;
import gr.atc.modapto.dto.sew.SewMonitorKpisComponentsDto;
import jakarta.validation.Valid;

import java.util.List;

public interface ISelfAwarenessService {

    void invokeSelfAwarenessMonitoringKpisAlgorithm(SewSelfAwarenessMonitoringKpisInputDto invocationData);

    SewSelfAwarenessMonitoringKpisResultsDto retrieveLatestSelfAwarenessMonitoringKpisResults();

    SewSelfAwarenessMonitoringKpisResultsDto retrieveLatestSelfAwarenessMonitoringKpisResultsByModuleId(String moduleId);

    List<SewSelfAwarenessMonitoringKpisResultsDto> retrieveAllSelfAwarenessMonitoringKpisResults();

    List<SewSelfAwarenessMonitoringKpisResultsDto> retrieveAllSelfAwarenessMonitoringKpisResultsByModuleId(String moduleId);

    void invokeSelfAwarenessRealTimeMonitoringAlgorithm(SewSelfAwarenessRealTimeMonitoringInputDto invocationData);

    List<SewSelfAwarenessRealTimeMonitoringResultsDto> retrieveAllSelfAwarenessRealTimeMonitoringResults();

    List<SewSelfAwarenessRealTimeMonitoringResultsDto> retrieveAllSelfAwarenessRealTimeMonitoringResultsByModuleId(String moduleId);

    void uploadModuleComponentsList(SewMonitorKpisComponentsDto componentsData);

    SewMonitorKpisComponentsDto retrieveSelfAwarenessComponentListByModuleId(String moduleId);

    void deleteSelfAwarenessComponentListByModuleId(String moduleId);
}
