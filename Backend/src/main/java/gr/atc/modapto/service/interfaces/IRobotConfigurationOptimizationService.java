package gr.atc.modapto.service.interfaces;

import gr.atc.modapto.dto.serviceInvocations.FftOptimizationInputDto;
import gr.atc.modapto.dto.serviceResults.fft.FftOptimizationResultsDto;

public interface IRobotConfigurationOptimizationService extends IOptimizationService<FftOptimizationResultsDto>{
    void invokeOptimizationOfRobotConfiguration(FftOptimizationInputDto invocationData);
}
