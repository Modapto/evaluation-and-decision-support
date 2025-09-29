package gr.atc.modapto.service.interfaces;

import gr.atc.modapto.dto.serviceInvocations.CrfInvocationInputDto;
import gr.atc.modapto.dto.serviceResults.crf.CrfSimulationResultsDto;

public interface IKitHolderSimulationService extends ISimulationService<CrfSimulationResultsDto> {

    void invokeSimulationOfKhPickingSequence(CrfInvocationInputDto invocationData);
}