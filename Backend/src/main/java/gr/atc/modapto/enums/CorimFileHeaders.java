package gr.atc.modapto.enums;

import lombok.Getter;
import lombok.Setter;

/*
 * Enum Corim Data Headers to ensure that the column names inside the file are correct
 */
@Getter
public enum CorimFileHeaders {
    STAGE("Stage"),
    CELL("Cell"),
    FAILURE_ELEMENT_ID("ID"),
    MODULE("Module description"),
    MODULE_ID("Module ID"),
    COMPONENT("Component"),
    COMPONENT_ID("Component ID"),
    FAILURE_TYPE("Failure Type"),
    FAILURE_DESCRIPTION("Failure description"),
    MAINTENANCE_ACTION_PERFORMED("Maintenance Action performed"),
    COMPONENT_REPLACEMENT("component replacement (yes/no)"),
    WORKER_NAME("Name"),
    TS_REQUEST_CREATION("TS request creation"),
    TS_INTERVENTION_STARTED("TS Intervention started"),
    TS_INTERVENTION_FINISHED("TS intervention finished");

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