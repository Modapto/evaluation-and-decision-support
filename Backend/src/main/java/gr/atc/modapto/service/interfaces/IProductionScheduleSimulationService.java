package gr.atc.modapto.service.interfaces;

import gr.atc.modapto.dto.serviceInvocations.SewSimulationInputDto;
import gr.atc.modapto.dto.serviceResults.sew.SewSimulationResultsDto;
import gr.atc.modapto.dto.sew.SewPlantEnvironmentDto;

public interface IProductionScheduleSimulationService extends ISimulationService<SewSimulationResultsDto> {

    void invokeSimulationOfProductionSchedules(SewSimulationInputDto invocationData);

    SewPlantEnvironmentDto retrieveLatestPlantEnvironment();

    void uploadPlantEnvironment(SewPlantEnvironmentDto environment);
}