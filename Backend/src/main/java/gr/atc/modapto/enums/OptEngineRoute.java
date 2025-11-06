package gr.atc.modapto.enums;

public enum OptEngineRoute {
    PRODUCTION_SCHEDULE_OPTIMIZATION("hffs"),
    PRODUCTION_SCHEDULE_SIMULATION("hffs-sim"),
    ROBOT_PICKING_SEQUENCE("robot-picking-seq"),
    ROBOT_CONFIGURATION("robot-movement");

    private final String route;

    OptEngineRoute(final String route) {
        this.route = route;
    }

    @Override
    public String toString() {
        return route;
    }

}