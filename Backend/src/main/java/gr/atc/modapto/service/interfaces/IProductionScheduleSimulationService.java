package gr.atc.modapto.service.interfaces;

import gr.atc.modapto.dto.serviceInvocations.SewSimulationInputDto;
import gr.atc.modapto.dto.serviceResults.sew.SewSimulationResultsDto;

public interface IProductionScheduleSimulationService extends ISimulationService<SewSimulationResultsDto> {

    void invokeSimulationOfProductionSchedules(SewSimulationInputDto invocationData);
}