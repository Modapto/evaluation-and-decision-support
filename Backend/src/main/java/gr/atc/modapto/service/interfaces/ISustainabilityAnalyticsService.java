package gr.atc.modapto.service.interfaces;

import gr.atc.modapto.dto.serviceInvocations.FftSustainabilityAnalyticsInputDto;
import gr.atc.modapto.dto.serviceInvocations.GlobalRequestDto;
import gr.atc.modapto.dto.serviceResults.fft.FftSustainabilityAnalyticsResultsDto;

public interface ISustainabilityAnalyticsService {

    public FftSustainabilityAnalyticsResultsDto extractFftSustainabilityAnalytics(GlobalRequestDto<FftSustainabilityAnalyticsInputDto> invocationData);
}
