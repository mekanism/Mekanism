package mekanism.api;

import net.minecraft.core.Direction;
import org.jetbrains.annotations.Nullable;

public enum AutomationType {
    /**
     * External interaction (third party interacting with a machine)
     */
    EXTERNAL,
    /**
     * Internal interaction (machine interacting with its own contents)
     */
    INTERNAL,
    /**
     * Manual interaction (player interacting manually, such as in a GUI)
     */
    MANUAL;

    /**
     * Helper method to convert a null side into an internal automation type, and anything else into an external automation type.
     *
     * @since 10.5.13
     */
    public static AutomationType handler(@Nullable Direction side) {
        return side == null ? INTERNAL : EXTERNAL;
    }
}