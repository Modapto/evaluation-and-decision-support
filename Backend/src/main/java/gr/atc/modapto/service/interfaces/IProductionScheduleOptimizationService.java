package gr.atc.modapto.service.interfaces;

import gr.atc.modapto.dto.serviceInvocations.SewOptimizationInputDto;
import gr.atc.modapto.dto.serviceInvocations.SewProductionScheduleDto;
import gr.atc.modapto.dto.serviceResults.sew.SewOptimizationResultsDto;

public interface IProductionScheduleOptimizationService extends IOptimizationService<SewOptimizationResultsDto>{

    void uploadProductionSchedule(SewProductionScheduleDto schedule);

    SewProductionScheduleDto retrieveLatestProductionSchedule();

    void invokeOptimizationOfProductionSchedules(SewOptimizationInputDto invocationData);
}
