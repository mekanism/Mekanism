package mekanism.common.capabilities;

import javax.annotation.Nonnull;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;

public interface IToggleableCapability extends ICapabilityProvider {

    default boolean isCapabilityDisabled(@Nonnull Capability<?> capability, EnumFacing side) {
        return false;
    }
}