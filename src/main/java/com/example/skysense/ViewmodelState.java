package com.example.skysense;

import net.minecraft.world.item.ItemStack;

/** Tiny hand-off between the viewmodel mixins (mixin classes can't hold public statics themselves). */
public final class ViewmodelState {
    /** The item stack of the hand that is about to be drawn (set just before the pose is transformed). */
    public static ItemStack currentStack = null;
    /** True while a transform has been pushed for the hand currently being drawn. */
    public static boolean applied = false;

    private ViewmodelState() {}
}
