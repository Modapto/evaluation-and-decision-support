package gr.atc.modapto.enums;

/*
 * Enum case for WebSocket Topics
 */
public enum WebSocketTopics {
    CRF_SIMULATION_RESULTS("crf-simulation-results"),
    CRF_OPTIMIZATION_RESULTS("crf-optimization-results"),
    SEW_SIMULATION_RESULTS("sew-simulation-results"),
    SEW_OPTIMIZATION_RESULTS("sew-optimization-results"),
    SEW_THRESHOLD_BASED_PREDICTIVE_MAINTENANCE("sew-threshold-based-predictive-maintenance"),
    SEW_GROUPING_PREDICTIVE_MAINTENANCE("sew-grouping-predictive-maintenance"),
    SEW_SELF_AWARENESS("sew-self-awareness"),
    CRF_SELF_AWARENESS("crf-self-awareness");

    private final String topic;

    WebSocketTopics(String topic) {
        this.topic = topic;
    }

    public String toString() {
        return topic;
    }
}
