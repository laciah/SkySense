package com.example.skysense.mixin;

import com.example.skysense.SkySenseConfig;
import com.example.skysense.ViewmodelState;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * Viewmodel: moves / rotates / scales the first-person held item (and the empty hand if "Apply To Hand" is on).
 * Purely visual. It only changes how your own screen draws your own hand.
 *
 * Works by pushing a transform onto the pose stack when the first-person renderer starts drawing a hand, and popping
 * it when that call returns. require = 0 means "do nothing" if the renderer's method can't be found.
 */
@Pseudo
@Mixin(targets = "net.minecraft.client.renderer.ItemInHandRenderer")
public abstract class ViewmodelPoseMixin {

    @ModifyVariable(method = "/^(render|submit|extract)ArmWithItem$/", at = @At("HEAD"), argsOnly = true, require = 0)
    private PoseStack skysense$begin(PoseStack stack) {
        ViewmodelState.applied = false;
        ItemStack held = ViewmodelState.currentStack;
        ViewmodelState.currentStack = null;

        if (!SkySenseConfig.vmEnabled) return stack;
        if (held != null && held.isEmpty() && !SkySenseConfig.vmApplyHand) return stack;

        int[] v = SkySenseConfig.vm;
        stack.pushPose();
        stack.translate(v[0] / 100.0F, v[1] / 100.0F, v[2] / 100.0F);
        stack.mulPose(Axis.XP.rotationDegrees((float) v[3]));
        stack.mulPose(Axis.YP.rotationDegrees((float) v[4]));
        stack.mulPose(Axis.ZP.rotationDegrees((float) v[5]));
        float scale = v[6] / 100.0F;
        stack.scale(scale, scale, scale);
        ViewmodelState.applied = true;
        return stack;
    }

    @ModifyVariable(method = "/^(render|submit|extract)ArmWithItem$/", at = @At("RETURN"), argsOnly = true, require = 0)
    private PoseStack skysense$end(PoseStack stack) {
        if (ViewmodelState.applied) {
            ViewmodelState.applied = false;
            stack.popPose();
        }
        return stack;
    }
}
