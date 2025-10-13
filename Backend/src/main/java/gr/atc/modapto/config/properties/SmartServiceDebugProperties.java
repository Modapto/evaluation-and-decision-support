package gr.atc.modapto.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for Smart Service debugging features.
 * Enables storing request bodies as JSON files for inspection and debugging.
 */
@ConfigurationProperties(prefix = "smart-service.debug")
public record SmartServiceDebugProperties(

    /**
     * Enable or disable storing request JSON to local files.
     * Default: false
     */
    boolean storeRequestJson,

    /**
     * Directory path where JSON request files will be stored.
     * Can be relative or absolute path.
     * Default: ./smart-service-requests
     */
    String jsonOutputDirectory
) {

    /**
     * Default constructor with fallback values
     */
    public SmartServiceDebugProperties {
        if (jsonOutputDirectory == null || jsonOutputDirectory.trim().isEmpty()) {
            jsonOutputDirectory = "./smart-service-requests";
        }
    }
}
