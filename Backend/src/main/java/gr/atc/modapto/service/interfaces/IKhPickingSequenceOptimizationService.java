package gr.atc.modapto.service.interfaces;

import gr.atc.modapto.dto.crf.CrfOptimizationKittingConfigDto;
import gr.atc.modapto.dto.serviceInvocations.CrfInvocationInputDto;
import gr.atc.modapto.dto.serviceResults.crf.CrfOptimizationResultsDto;

public interface IKhPickingSequenceOptimizationService extends IOptimizationService<CrfOptimizationResultsDto> {

    void invokeOptimizationOfKhPickingSequence(CrfInvocationInputDto invocationData);

    CrfOptimizationKittingConfigDto retrieveOptimizationKittingConfig();
}
