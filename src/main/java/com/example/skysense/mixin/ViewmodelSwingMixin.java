package com.example.skysense.mixin;

import com.example.skysense.SkySenseConfig;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Viewmodel swing options. Everything here only applies to YOUR OWN player and only changes how your own arm
 * animation looks. The attack / interact packets are sent by LocalPlayer.swing and are not touched.
 *
 * Every injection uses an exact method signature with require = 0, so if a signature has changed it is skipped
 * instead of breaking the game.
 */
@Mixin(LivingEntity.class)
public abstract class ViewmodelSwingMixin {

    private static boolean skysense$hasHasteOrFatigue(LivingEntity entity) {
        for (MobEffectInstance effect : entity.getActiveEffects()) {
            String id = effect.getDescriptionId();
            if ("effect.minecraft.haste".equals(id) || "effect.minecraft.mining_fatigue".equals(id)) {
                return true;
            }
        }
        return false;
    }

    /** No Haste (ignore Haste / Mining Fatigue) and Swing Speed (>100% = faster swing, <100% = slower). */
    @Inject(method = "getCurrentSwingDuration()I", at = @At("RETURN"), cancellable = true, require = 0)
    private void skysense$swingDuration(CallbackInfoReturnable<Integer> cir) {
        if (!SkySenseConfig.vmEnabled) return;
        LivingEntity self = (LivingEntity) (Object) this;
        if (!(self instanceof LocalPlayer)) return;

        int duration = cir.getReturnValueI();
        if (SkySenseConfig.vmNoHaste && skysense$hasHasteOrFatigue(self)) {
            duration = 6; // the normal, effect-free swing length
        }
        int speed = SkySenseConfig.vm[12];
        if (speed != 100) {
            duration = Math.max(1, Math.round(duration * 100.0F / speed));
        }
        cir.setReturnValue(duration);
    }

    /** No Bow Swing: no arm swing animation while you hold a bow. */
    @Inject(method = "getAttackAnim(F)F", at = @At("RETURN"), cancellable = true, require = 0)
    private void skysense$attackAnim(float partialTick, CallbackInfoReturnable<Float> cir) {
        if (!SkySenseConfig.vmEnabled || !SkySenseConfig.vmNoBowSwing) return;
        if (((Object) this) instanceof LocalPlayer player && player.getMainHandItem().is(Items.BOW)) {
            cir.setReturnValue(0.0F);
        }
    }

    /** No Swing Reset: a swing that is already playing finishes instead of restarting on the next click. */
    @Inject(method = "swing(Lnet/minecraft/world/InteractionHand;Z)V", at = @At("HEAD"), cancellable = true, require = 0)
    private void skysense$swing(InteractionHand hand, boolean updateSelf, CallbackInfo ci) {
        if (!SkySenseConfig.vmEnabled || !SkySenseConfig.vmNoSwingReset) return;
        if (((Object) this) instanceof LocalPlayer player && player.swinging) {
            ci.cancel();
        }
    }
}
