package mekanism.common.util;

import javax.annotation.Nonnull;
import mekanism.common.base.LazyOptionalHelper;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;

public final class CapabilityUtils {

    //TODO: Ones that only use a simple ifPresent don't need to be done through the helper
    //TODO: Add contract param back?
    //@Contract("null, _, _ -> null; _, null, _ -> null")
    @Nonnull
    public static <T> LazyOptionalHelper<T> getCapabilityHelper(ICapabilityProvider provider, Capability<T> cap, Direction side) {
        if (provider == null || cap == null) {
            return new LazyOptionalHelper<>(LazyOptional.empty());
        }
        return new LazyOptionalHelper<>(provider.getCapability(cap, side));
    }
}