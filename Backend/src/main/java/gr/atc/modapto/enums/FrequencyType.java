package gr.atc.modapto.enums;

public enum FrequencyType {
    MINUTES("MINUTES"),
    HOURS("HOURS"),
    DAYS("DAYS");

    private final String type;

    FrequencyType(final String type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return type;
    }
}