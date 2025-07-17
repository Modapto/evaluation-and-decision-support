package gr.atc.modapto.dto;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import gr.atc.modapto.dto.serviceResults.CrfOptimizationResultsDto;
import gr.atc.modapto.dto.serviceResults.CrfSimulationResultsDto;
import gr.atc.modapto.dto.serviceResults.SewOptimizationResultsDto;
import gr.atc.modapto.dto.serviceResults.SewSimulationResultsDto;

@JsonTypeInfo(use = JsonTypeInfo.Id.DEDUCTION)
@JsonSubTypes({
        @JsonSubTypes.Type(value = CrfSimulationResultsDto.class, name = "crf-simulation"),
        @JsonSubTypes.Type(value = CrfOptimizationResultsDto.class, name = "crf-optimization"),
        @JsonSubTypes.Type(value = SewSimulationResultsDto.class, name = "sew-optimization"),
        @JsonSubTypes.Type(value = SewOptimizationResultsDto.class, name = "sew-simulation")
})
public abstract class BaseEventResultsDto {}

