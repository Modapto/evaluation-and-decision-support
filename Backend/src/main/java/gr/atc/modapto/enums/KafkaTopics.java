package gr.atc.modapto.enums;

/**
 * Enum representing Kafka topics with corresponding algorithm
 */
public enum KafkaTopics {
    CRF_SIMULATION_RESULTS("kh-picking-sequence-simulation"),
    CRF_OPTIMIZATION_RESULTS("kh-picking-sequence-optimization"),
    SEW_SIMULATION_RESULTS("production-schedule-simulation"),
    SEW_OPTIMIZATION_RESULTS("production-schedule-optimization"),
    SEW_GROUPING_PREDICTIVE_MAINTENANCE("grouping-predictive-maintenance"),
    SEW_THRESHOLD_PREDICTIVE_MAINTENANCE("threshold-predictive-maintenance"),
    SEW_SELF_AWARENESS_MONITORING_KPIS("self-awareness-monitoring-kpis"),
    SEW_SELF_AWARENESS_REAL_TIME_MONITORING("self-awareness-real-time-monitoring"),
    SEW_PROCESS_DRIFT("process-drift"),
    CRF_SELF_AWARENESS_WEAR_DETECTION("self-awareness-wear-detection");

    private final String topic;

    KafkaTopics(final String topic) {
        this.topic = topic;
    }

    @Override
    public String toString() {
        return topic;
    }
}