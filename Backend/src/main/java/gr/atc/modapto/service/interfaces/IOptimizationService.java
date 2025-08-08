package gr.atc.modapto.service.interfaces;

import gr.atc.modapto.dto.serviceInvocations.SewProductionScheduleDto;

public interface IOptimizationService<T> {
    T retrieveLatestOptimizationResults();

    T retrieveLatestOptimizationResultsByProductionModule(String productionModule);

    void uploadProductionSchedule(SewProductionScheduleDto schedule);
}