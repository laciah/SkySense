package com.example.skysense.mixin;

import com.example.skysense.ViewmodelState;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * Remembers which item the first-person renderer is about to draw, so "Apply To Hand" can tell an empty hand from a
 * held item. High priority so it runs before ViewmodelPoseMixin. If it ever runs late, the viewmodel simply treats the
 * hand as holding an item.
 */
@Pseudo
@Mixin(targets = "net.minecraft.client.renderer.ItemInHandRenderer", priority = 2000)
public abstract class ViewmodelStackMixin {

    @ModifyVariable(method = "/^(render|submit|extract)ArmWithItem$/", at = @At("HEAD"), argsOnly = true, require = 0)
    private ItemStack skysense$captureStack(ItemStack stack) {
        ViewmodelState.currentStack = stack;
        return stack;
    }
}
