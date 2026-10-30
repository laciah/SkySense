package com.example.skysense;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;

public class SkySenseClient implements ClientModInitializer {
    public static final String MOD_ID = "skysense";

    // Shows up in Options > Controls > Key Binds under "SkySense".
    // That is where you rebind the open key (default: INSERT).
    private static final KeyMapping.Category CATEGORY =
            KeyMapping.Category.register(Identifier.fromNamespaceAndPath(MOD_ID, "main"));

    public static final KeyMapping OPEN_KEY = KeyMappingHelper.registerKeyMapping(
            new KeyMapping("key.skysense.open", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_INSERT, CATEGORY));

    @Override
    public void onInitializeClient() {
        SkySenseConfig.load();
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (OPEN_KEY.consumeClick()) {
                if (client.gui.screen() == null) {
                    client.gui.setScreen(new SkySenseScreen());
                }
            }
        });
    }
}
