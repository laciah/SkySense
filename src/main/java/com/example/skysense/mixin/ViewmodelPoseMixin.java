package com.example.skysense.mixin;

import com.example.skysense.SkySenseConfig;
import com.example.skysense.ViewmodelState;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Viewmodel: position, rotation, scale and swing size of your own first-person hand / held item.
 * Purely visual - it only changes how YOUR screen draws YOUR hand.
 *
 * Position is applied first (before the game positions the arm), rotation and scale last (right before the item or
 * arm is drawn), so rotating and scaling happen around the item itself. Every hook uses require = 0: if the game's
 * code doesn't contain the expected call, that part is skipped instead of crashing.
 */
@Mixin(ItemInHandRenderer.class)
public abstract class ViewmodelPoseMixin {

    /** +1 if the arm is on the right of the screen, -1 if on the left (so X mirrors for the other hand). */
    private static float skysense$sideOf(HumanoidArm arm) {
        return arm == HumanoidArm.RIGHT ? 1.0F : -1.0F;
    }

    private static void skysense$rotateAndScale(PoseStack poses) {
        int[] v = SkySenseConfig.vm;
        poses.mulPose(Axis.XP.rotationDegrees((float) v[3]));
        poses.mulPose(Axis.YP.rotationDegrees((float) v[4]));
        poses.mulPose(Axis.ZP.rotationDegrees((float) v[5]));
        poses.scale(v[6] / 100.0F, v[7] / 100.0F, v[8] / 100.0F);
    }

    // ---- a held item ----

    @Inject(method = "submitArmWithItem",
            at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;pushPose()V", shift = At.Shift.AFTER),
            require = 0)
    private void skysense$moveItem(AbstractClientPlayer player, float partialTick, float xRot, InteractionHand hand,
                                   float attack, ItemStack stack, float equipHeight, PoseStack poses,
                                   SubmitNodeCollector collector, int light, CallbackInfo ci) {
        if (!ViewmodelState.active() || stack.isEmpty()) return;
        HumanoidArm arm = hand == InteractionHand.MAIN_HAND ? player.getMainArm() : player.getMainArm().getOpposite();
        int[] v = SkySenseConfig.vm;
        poses.translate(skysense$sideOf(arm) * v[0] / 100.0F, v[1] / 100.0F, v[2] / 100.0F);
    }

    @Inject(method = "submitArmWithItem",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/ItemInHandRenderer;renderItem(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemDisplayContext;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;I)V"),
            require = 0)
    private void skysense$turnItem(AbstractClientPlayer player, float partialTick, float xRot, InteractionHand hand,
                                   float attack, ItemStack stack, float equipHeight, PoseStack poses,
                                   SubmitNodeCollector collector, int light, CallbackInfo ci) {
        if (!ViewmodelState.active() || stack.isEmpty()) return;
        skysense$rotateAndScale(poses);
    }

    // ---- the empty hand (only when "Apply To Hand" is on) ----

    @Inject(method = "renderPlayerArm", at = @At("HEAD"), require = 0)
    private void skysense$moveHand(PoseStack poses, SubmitNodeCollector collector, int light, float equipHeight,
                                   float attack, HumanoidArm arm, CallbackInfo ci) {
        if (!ViewmodelState.active() || !SkySenseConfig.vmApplyHand) return;
        int[] v = SkySenseConfig.vm;
        poses.translate(skysense$sideOf(arm) * v[0] / 100.0F, v[1] / 100.0F, v[2] / 100.0F);
    }

    @Inject(method = "renderPlayerArm",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/EntityRenderDispatcher;getPlayerRenderer(Lnet/minecraft/client/player/AbstractClientPlayer;)Lnet/minecraft/client/renderer/entity/player/AvatarRenderer;"),
            require = 0)
    private void skysense$turnHand(PoseStack poses, SubmitNodeCollector collector, int light, float equipHeight,
                                   float attack, HumanoidArm arm, CallbackInfo ci) {
        if (!ViewmodelState.active() || !SkySenseConfig.vmApplyHand) return;
        skysense$rotateAndScale(poses);
    }

    // ---- swing size: multiplies the sideways / up / forward travel of the swing ----

    @Redirect(method = "swingArm",
            at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;translate(FFF)V", ordinal = 0),
            require = 0)
    private void skysense$swingTravel(PoseStack poses, float x, float y, float z) {
        if (ViewmodelState.active()) {
            int[] v = SkySenseConfig.vm;
            poses.translate(x * v[9] / 100.0F, y * v[10] / 100.0F, z * v[11] / 100.0F);
        } else {
            poses.translate(x, y, z);
        }
    }
}
