package com.example.skysense.mixin;

import com.example.skysense.SkySenseConfig;
import com.example.skysense.ViewmodelState;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Viewmodel > "No Equip Animation": the item stays fully raised instead of dipping and coming back up when you
 * switch items or the item changes. Visual only.
 */
@Mixin(ItemInHandRenderer.class)
public abstract class ViewmodelStackMixin {
    @Shadow private float mainHandHeight;
    @Shadow private float offHandHeight;
    @Shadow private float oMainHandHeight;
    @Shadow private float oOffHandHeight;

    @Inject(method = "shouldInstantlyReplaceVisibleItem", at = @At("HEAD"), cancellable = true, require = 0)
    private void skysense$noReplaceDip(ItemStack visible, ItemStack expected, CallbackInfoReturnable<Boolean> cir) {
        if (ViewmodelState.active() && SkySenseConfig.vmNoEquip) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "tick", at = @At("TAIL"), require = 0)
    private void skysense$holdItemUp(CallbackInfo ci) {
        if (ViewmodelState.active() && SkySenseConfig.vmNoEquip) {
            this.mainHandHeight = 1.0F;
            this.offHandHeight = 1.0F;
            this.oMainHandHeight = 1.0F;
            this.oOffHandHeight = 1.0F;
        }
    }
}
