package gr.atc.modapto.service.interfaces;

public interface ISimulationService<T> {
    T retrieveLatestSimulationResults();

    T retrieveLatestSimulationResultsByProductionModule(String productionModule);
}