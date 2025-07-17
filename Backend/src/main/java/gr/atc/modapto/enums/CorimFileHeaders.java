package gr.atc.modapto.enums;

import lombok.Getter;
import lombok.Setter;

/*
 * Enum Corim Data Headers to ensure that the column names inside the file are correct
 */
@Getter
public enum CorimFileHeaders {
    REQUEST_ID("Request ID"),
    INTERVENTION_ID("Intervention ID"),
    EQUIPMENT_ID("Equipment ID"),
    RECIPIENT("Recipient"),
    STAGE("Stage"),
    CELL("Cell"),
    MODULE("Module"),
    COMPONENT("Component"),
    FAILURE_TYPE("Failure Type"),
    FAILURE_DESCRIPTION("Failure description"),
    MAINTENANCE_ACTION_PERFORMED("Maintenance Action performed"),
    COMPONENT_REPLACEMENT("component replacement (yes/no)"),
    COMPONENT_NAME("Name"),
    TS_REQUEST_CREATION("TS request creation"),
    TS_REQUEST_ACKNOWLEDGED("TS request acknowledged"),
    TS_INTERVENTION_STARTED("TS Intervention started"),
    TS_INTERVENTION_FINISHED("TS intervention finished"),
    INTERVENTION_STATUS("Intervention status"),
    MTBF("MTBF"),
    MTBF_STAGE_LEVEL("MTBF stage level"),
    DURATION_CREATION_TO_ACKNOWLEDGED("Duration creation - acknowledged"),
    DURATION_CREATION_TO_INTERVENTION_START("Duration creation -  intervention start"),
    DURATION_INTERVENTION_STARTED_TO_FINISHED("Duration intervention started- finished"),
    TOTAL_DURATION_CREATION_TO_FINISHED("Total duration creation-finished"),
    TOTAL_MAINTENANCE_TIME_ALLOCATED("Total maintenance time allocated");

    private final String header;

    // Column number positioning
    @Setter
    private int columnPosition = -1;

    CorimFileHeaders(String header) {
        this.header = header;
    }

    public static CorimFileHeaders fromHeader(String header) {
        for (CorimFileHeaders value : values()) {
            if (value.getHeader().equalsIgnoreCase(header)) {
                return value;
            }
        }
        return null;
    }

    public boolean hasValidPosition() {
        return columnPosition >= 0;
    }

    // Reset all positions
    public static void resetAllPositions() {
        for (CorimFileHeaders header : values()) {
            header.columnPosition = -1;
        }
    }
}