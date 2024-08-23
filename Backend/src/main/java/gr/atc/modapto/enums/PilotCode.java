package gr.atc.modapto.enums;

/*
 * Enum class for Pilot Code
 */
public enum PilotCode {
    CRF("CRF"),
    ILTAR("ILTAR`"),
    FFT("FFT"),
    SEW("SEW");

    private final String pilot;

    PilotCode(final String pilot) {
        this.pilot = pilot;
    }

    @Override
    public String toString() {
        return pilot.toUpperCase();
    }
}
