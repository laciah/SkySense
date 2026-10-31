package com.example.skysense.mixin;

import com.example.skysense.SkySenseConfig;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
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

    /** Swing Speed: >100% = faster arm swing, <100% = slower. (Same idea as Haste / Mining Fatigue.) */
    @Inject(method = "getCurrentSwingDuration()I", at = @At("RETURN"), cancellable = true, require = 0)
    private void skysense$swingDuration(CallbackInfoReturnable<Integer> cir) {
        if (!SkySenseConfig.vmEnabled) return;
        int speed = SkySenseConfig.vm[7];
        if (speed == 100) return;
        if (((Object) this) instanceof LocalPlayer) {
            cir.setReturnValue(Math.max(1, Math.round(cir.getReturnValueI() * 100.0F / speed)));
        }
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
