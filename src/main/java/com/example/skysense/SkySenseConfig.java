package com.example.skysense;

import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/** Feature toggles that are saved to .minecraft/config/skysense.properties. */
public final class SkySenseConfig {
    private static final Logger LOGGER = LoggerFactory.getLogger("skysense");

    /** "No Render" > "Hide Absorption Hearts". Read by the HUD mixin. */
    public static volatile boolean hideAbsorptionHearts = false;

    private SkySenseConfig() {}

    private static Path file() {
        return FabricLoader.getInstance().getConfigDir().resolve("skysense.properties");
    }

    public static void load() {
        Path path = file();
        if (!Files.exists(path)) return;
        try (InputStream in = Files.newInputStream(path)) {
            Properties props = new Properties();
            props.load(in);
            hideAbsorptionHearts = Boolean.parseBoolean(props.getProperty("hideAbsorptionHearts", "false"));
        } catch (IOException e) {
            LOGGER.warn("Could not read {}", path, e);
        }
    }

    public static void save() {
        Path path = file();
        try (OutputStream out = Files.newOutputStream(path)) {
            Properties props = new Properties();
            props.setProperty("hideAbsorptionHearts", Boolean.toString(hideAbsorptionHearts));
            props.store(out, "SkySense settings");
        } catch (IOException e) {
            LOGGER.warn("Could not write {}", path, e);
        }
    }
}
