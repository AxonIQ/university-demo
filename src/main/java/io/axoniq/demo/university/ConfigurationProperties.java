package io.axoniq.demo.university;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ConfigurationProperties {
    private static final Logger logger = Logger.getLogger(ConfigurationProperties.class.getName());

    // Possible values: "in-memory", "axonserver", "postgres"
    private String eventStorageEngine = "axonserver";

    public static ConfigurationProperties defaults() {
        return new ConfigurationProperties();
    }

    public static ConfigurationProperties load() {
        ConfigurationProperties props = new ConfigurationProperties();

        Properties properties = loadPropertiesFile("application.properties");

        if (properties != null) {
            String engine = properties.getProperty("axon.event-storage.engine");
            if (engine != null && !engine.isBlank()) {
                props.eventStorageEngine = engine.trim();
            }
        } else {
            logger.info("No properties file found, using default configuration");
        }

        return props;
    }

    private static Properties loadPropertiesFile(String filename) {
        Properties properties = new Properties();
        try (InputStream input = ConfigurationProperties.class.getClassLoader().getResourceAsStream(filename)) {
            if (input != null) {
                properties.load(input);
                logger.info("Loaded configuration from " + filename);
                return properties;
            }
        } catch (IOException e) {
            logger.log(Level.WARNING, "Error loading properties file " + filename + ": " + e.getMessage());
        }
        return null;
    }

    public String eventStorageEngine() {
        return eventStorageEngine;
    }

    public ConfigurationProperties eventStorageEngine(String eventStorageEngine) {
        this.eventStorageEngine = eventStorageEngine;
        return this;
    }

    public boolean isAxonServerEventStorageEngine() {
        return "axonserver".equalsIgnoreCase(eventStorageEngine);
    }

    public boolean isPostgresEventStorageEngine() {
        return "postgres".equalsIgnoreCase(eventStorageEngine);
    }
}
