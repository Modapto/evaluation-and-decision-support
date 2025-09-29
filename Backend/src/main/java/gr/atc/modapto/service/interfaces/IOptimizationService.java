package gr.atc.modapto.service.interfaces;

public interface IOptimizationService<T> {
    T retrieveLatestOptimizationResults();

    T retrieveLatestOptimizationResultsByModuleId(String moduleId);
}