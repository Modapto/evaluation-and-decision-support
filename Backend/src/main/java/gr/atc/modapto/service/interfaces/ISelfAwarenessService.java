package gr.atc.modapto.service.interfaces;

import gr.atc.modapto.dto.serviceInvocations.SewSelfAwarenessMonitoringKpisInputDto;
import gr.atc.modapto.dto.serviceResults.sew.SewSelfAwarenessMonitoringKpisResultsDto;

import java.util.List;

public interface ISelfAwarenessService {

    void invokeSelfAwarenessMonitoringKpisAlgorithm(SewSelfAwarenessMonitoringKpisInputDto invocationData);

    SewSelfAwarenessMonitoringKpisResultsDto retrieveLatestSelfAwarenessMonitoringKpisResults();

    SewSelfAwarenessMonitoringKpisResultsDto retrieveLatestSelfAwarenessMonitoringKpisResultsByModuleId(String moduleId);

    List<SewSelfAwarenessMonitoringKpisResultsDto> retrieveAllSelfAwarenessMonitoringKpisResults();

    List<SewSelfAwarenessMonitoringKpisResultsDto> retrieveAllSelfAwarenessMonitoringKpisResultsByModuleId(String moduleId);
}
