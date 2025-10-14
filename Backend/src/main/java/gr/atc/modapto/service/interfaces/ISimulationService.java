package gr.atc.modapto.service.interfaces;

public interface ISimulationService<T> {
    T retrieveLatestSimulationResults();

    T retrieveLatestSimulationResultsByModule(String productionModule);
}