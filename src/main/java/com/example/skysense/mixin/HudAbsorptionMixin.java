package com.example.skysense.mixin;

import com.example.skysense.SkySenseConfig;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * "No Render" > "Hide Absorption Hearts".
 *
 * The HUD (net.minecraft.client.gui.Hud, extractPlayerHealth and friends) reads the player's absorption amount to
 * decide how many absorption hearts and heart rows to draw. While the option is on, the HUD sees 0 instead, so the
 * absorption hearts are skipped entirely (and the other bars don't move up to make room for them).
 *
 * Two variants because the call may be compiled against Player or LivingEntity. require = 0 means "do nothing instead
 * of crashing" if a call site isn't found.
 */
@Pseudo
@Mixin(targets = "net.minecraft.client.gui.Hud")
public abstract class HudAbsorptionMixin {

    @Redirect(
            method = "/^extract.*/",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;getAbsorptionAmount()F"),
            require = 0)
    private float skysense$playerAbsorption(Player player) {
        return SkySenseConfig.hideAbsorptionHearts ? 0.0F : player.getAbsorptionAmount();
    }

    @Redirect(
            method = "/^extract.*/",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;getAbsorptionAmount()F"),
            require = 0)
    private float skysense$livingAbsorption(LivingEntity entity) {
        return SkySenseConfig.hideAbsorptionHearts ? 0.0F : entity.getAbsorptionAmount();
    }
}
