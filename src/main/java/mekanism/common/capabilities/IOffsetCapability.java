package mekanism.common.capabilities;

import javax.annotation.Nonnull;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.Vec3i;
import net.minecraftforge.common.capabilities.Capability;

public interface IOffsetCapability {

    boolean hasOffsetCapability(@Nonnull Capability<?> capability, EnumFacing side, Vec3i offset);

    <T> T getOffsetCapability(@Nonnull Capability<T> capability, EnumFacing side, Vec3i offset);

    default boolean isOffsetCapabilityDisabled(@Nonnull Capability<?> capability, EnumFacing side, Vec3i offset) {
        return false;
    }
}