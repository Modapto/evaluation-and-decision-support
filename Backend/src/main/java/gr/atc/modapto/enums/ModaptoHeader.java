package gr.atc.modapto.enums;

public enum ModaptoHeader {
    ASYNC("async"),
    SYNC("sync");

    private final String header;

    ModaptoHeader(final String header) {
        this.header = header;
    }

    @Override
    public String toString() {
        return header;
    }

}
