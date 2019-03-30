package mekanism.common.capabilities;

import javax.annotation.Nonnull;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;

public interface IToggleableCapability {

    default boolean isCapabilityDisabled(@Nonnull Capability<?> capability, EnumFacing side) {
        return false;
    }
}