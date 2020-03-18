package mekanism.common.util;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;

public final class CapabilityUtils {

    //TODO: Probably remove this method, and just properly handle null cap/provider in places that call this
    //TODO: Add contract param back?
    //@Contract("null, _, _ -> null; _, null, _ -> null")
    @Nonnull
    public static <T> LazyOptional<T> getCapability(@Nullable ICapabilityProvider provider, @Nullable Capability<T> cap, @Nullable Direction side) {
        if (provider == null || cap == null) {
            return LazyOptional.empty();
        }
        return provider.getCapability(cap, side);
    }
}