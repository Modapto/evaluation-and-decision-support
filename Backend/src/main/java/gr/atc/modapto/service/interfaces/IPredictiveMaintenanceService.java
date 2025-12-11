package gr.atc.modapto.service.interfaces;

import gr.atc.modapto.dto.serviceInvocations.SewThresholdBasedMaintenanceInputDataDto;
import gr.atc.modapto.dto.serviceResults.sew.SewGroupingPredictiveMaintenanceOutputDto;
import gr.atc.modapto.dto.serviceResults.sew.SewThresholdBasedPredictiveMaintenanceOutputDto;
import gr.atc.modapto.dto.sew.MaintenanceDataDto;
import gr.atc.modapto.dto.sew.SewComponentInfoDto;
import gr.atc.modapto.dto.serviceInvocations.SewGroupingPredictiveMaintenanceInputDataDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

public interface IPredictiveMaintenanceService {
    void storeCorimData(MultipartFile file);

    void storeComponentsListData(List<SewComponentInfoDto> componentInfoList);

    Page<MaintenanceDataDto> retrieveMaintenanceDataPaginated(Pageable pageable);

    List<SewComponentInfoDto> retrieveComponentListGivenFilterAttributes(String stage, String cell, String module, String moduleId);

    void locateLastMaintenanceActionForStoredComponents();

    void invokeGroupingPredictiveMaintenance(SewGroupingPredictiveMaintenanceInputDataDto invocationData);

    SewThresholdBasedPredictiveMaintenanceOutputDto invokeAndRegisterThresholdBasedPredictiveMaintenance(SewThresholdBasedMaintenanceInputDataDto invocationData);

    SewThresholdBasedPredictiveMaintenanceOutputDto invokeThresholdBasedPredictiveMaintenance(SewThresholdBasedMaintenanceInputDataDto invocationData);

    SewThresholdBasedPredictiveMaintenanceOutputDto retrieveLatestThresholdBasedMaintenanceResults(String moduleId);

    SewGroupingPredictiveMaintenanceOutputDto retrieveLatestGroupingMaintenanceResults(String moduleId);

    String declareProcessDrift(MaintenanceDataDto processDriftData);

    MaintenanceDataDto retrieveProcessDriftById(String processDriftId);

    Page<MaintenanceDataDto> retrievePaginatedUncompletedProcessDrifts(Pageable pageable);

    void completeProcessDrift(String processDriftId, LocalDateTime endDatetime);

    void deleteAllMaintenanceData();
}
