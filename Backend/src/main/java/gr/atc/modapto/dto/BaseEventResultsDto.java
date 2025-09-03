package gr.atc.modapto.dto;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import gr.atc.modapto.dto.serviceResults.crf.CrfOptimizationResultsDto;
import gr.atc.modapto.dto.serviceResults.crf.CrfSimulationResultsDto;
import gr.atc.modapto.dto.serviceResults.sew.SewGroupingPredictiveMaintenanceOutputDto;
import gr.atc.modapto.dto.serviceResults.sew.SewOptimizationResultsDto;
import gr.atc.modapto.dto.serviceResults.sew.SewSelfAwarenessMonitoringKpisResultsDto;
import gr.atc.modapto.dto.serviceResults.sew.SewSimulationResultsDto;

@JsonTypeInfo(use = JsonTypeInfo.Id.DEDUCTION)
@JsonSubTypes({
        @JsonSubTypes.Type(value = CrfSimulationResultsDto.class, name = "crf-simulation"),
        @JsonSubTypes.Type(value = CrfOptimizationResultsDto.class, name = "crf-optimization"),
        @JsonSubTypes.Type(value = SewSimulationResultsDto.class, name = "sew-optimization"),
        @JsonSubTypes.Type(value = SewOptimizationResultsDto.class, name = "sew-simulation"),
        @JsonSubTypes.Type(value = SewGroupingPredictiveMaintenanceOutputDto.class, name = "sew-grouping-pdm"),
        @JsonSubTypes.Type(value = SewSelfAwarenessMonitoringKpisResultsDto.class, name = "sew-self-awareness-monitoring-kpis")
})
public abstract class BaseEventResultsDto {}

