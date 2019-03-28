package mekanism.common.util;

import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;

public final class CapabilityUtils {

    public static boolean hasCapability(ICapabilityProvider provider, Capability<?> cap, EnumFacing side) {
        if (provider == null || cap == null) {
            return false;
        }

        return provider.hasCapability(cap, side);
    }

    public static <T> T getCapability(ICapabilityProvider provider, Capability<T> cap, EnumFacing side) {
        if (provider == null || cap == null) {
            return null;
        }

        return provider.getCapability(cap, side);
    }
}
