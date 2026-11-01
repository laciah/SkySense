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

    // ---- Viewmodel (Visual tab) ----
    public static volatile boolean vmEnabled = false;
    public static volatile boolean vmApplyHand = false;
    public static volatile boolean vmNoEquip = false;
    public static volatile boolean vmNoBowSwing = false;
    public static volatile boolean vmNoSwingReset = false;
    public static volatile boolean vmNoHaste = false;

    /**
     * Slider values, in this order:
     * 0-2 Position X/Y/Z, 3-5 Rotation X/Y/Z, 6-8 Scale X/Y/Z, 9-11 Swing X/Y/Z, 12 Swing Speed.
     */
    public static final int VM_COUNT = 13;
    public static final String[] VM_KEY = {"posX", "posY", "posZ", "rotX", "rotY", "rotZ",
            "scaleX", "scaleY", "scaleZ", "swingX", "swingY", "swingZ", "swingSpeed"};
    public static final int[] VM_MIN     = {-100, -100, -100, -180, -180, -180,  10,  10,  10,   0,   0,   0,  25};
    public static final int[] VM_MAX     = { 100,  100,  100,  180,  180,  180, 300, 300, 300, 300, 300, 300, 400};
    public static final int[] VM_DEFAULT = {   0,    0,    0,    0,    0,    0, 100, 100, 100, 100, 100, 100, 100};
    public static final int[] vm = VM_DEFAULT.clone();

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
            vmEnabled = Boolean.parseBoolean(props.getProperty("vm.enabled", "false"));
            vmApplyHand = Boolean.parseBoolean(props.getProperty("vm.applyHand", "false"));
            vmNoEquip = Boolean.parseBoolean(props.getProperty("vm.noEquip", "false"));
            vmNoBowSwing = Boolean.parseBoolean(props.getProperty("vm.noBowSwing", "false"));
            vmNoHaste = Boolean.parseBoolean(props.getProperty("vm.noHaste", "false"));
            vmNoSwingReset = Boolean.parseBoolean(props.getProperty("vm.noSwingReset", "false"));
            for (int i = 0; i < VM_COUNT; i++) {
                int value = VM_DEFAULT[i];
                try {
                    value = Integer.parseInt(props.getProperty("vm." + VM_KEY[i], Integer.toString(VM_DEFAULT[i])).trim());
                } catch (NumberFormatException ignored) {
                    // keep the default
                }
                vm[i] = Math.max(VM_MIN[i], Math.min(VM_MAX[i], value));
            }
        } catch (IOException e) {
            LOGGER.warn("Could not read {}", path, e);
        }
    }

    public static void save() {
        Path path = file();
        try (OutputStream out = Files.newOutputStream(path)) {
            Properties props = new Properties();
            props.setProperty("hideAbsorptionHearts", Boolean.toString(hideAbsorptionHearts));
            props.setProperty("vm.enabled", Boolean.toString(vmEnabled));
            props.setProperty("vm.applyHand", Boolean.toString(vmApplyHand));
            props.setProperty("vm.noEquip", Boolean.toString(vmNoEquip));
            props.setProperty("vm.noBowSwing", Boolean.toString(vmNoBowSwing));
            props.setProperty("vm.noHaste", Boolean.toString(vmNoHaste));
            props.setProperty("vm.noSwingReset", Boolean.toString(vmNoSwingReset));
            for (int i = 0; i < VM_COUNT; i++) {
                props.setProperty("vm." + VM_KEY[i], Integer.toString(vm[i]));
            }
            props.store(out, "SkySense settings");
        } catch (IOException e) {
            LOGGER.warn("Could not write {}", path, e);
        }
    }
}
