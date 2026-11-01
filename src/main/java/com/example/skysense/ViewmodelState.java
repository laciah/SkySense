package com.example.skysense;

/** Small helpers shared by the viewmodel mixins. */
public final class ViewmodelState {
    private ViewmodelState() {}

    /** True when the Viewmodel "Enabled" box is ticked. */
    public static boolean active() {
        return SkySenseConfig.vmEnabled;
    }
}
