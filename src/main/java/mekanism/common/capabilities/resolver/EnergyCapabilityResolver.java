package mekanism.common.capabilities.resolver;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import mekanism.api.energy.ISidedStrictEnergyHandler;
import mekanism.api.energy.IStrictEnergyHandler;
import mekanism.common.integration.energy.EnergyCompatUtils;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class EnergyCapabilityResolver implements ICapabilityResolver {

    private final Map<Capability<?>, LazyOptional<?>> cachedCapabilities = new HashMap<>();
    private final IStrictEnergyHandler handler;

    public EnergyCapabilityResolver(ISidedStrictEnergyHandler handler) {
        this.handler = handler;
    }

    @Override
    public List<Capability<?>> getSupportedCapabilities() {
        return EnergyCompatUtils.getEnabledEnergyCapabilities();
    }

    @Override
    public <T> LazyOptional<T> resolve(Capability<T> capability, @Nullable Direction side) {
        return getCachedOrResolve(capability, cachedCapabilities, handler);
    }

    @Override
    public void invalidate(Capability<?> capability, @Nullable Direction side) {
        invalidate(cachedCapabilities.get(capability));
    }

    @Override
    public void invalidateAll() {
        for (LazyOptional<?> lazyOptional : new ArrayList<>(cachedCapabilities.values())) {
            invalidate(lazyOptional);
        }
    }

    protected void invalidate(@Nullable LazyOptional<?> cachedCapability) {
        if (cachedCapability != null && cachedCapability.isPresent()) {
            cachedCapability.invalidate();
        }
    }

    public static <T> LazyOptional<T> getCachedOrResolve(Capability<T> capability, Map<Capability<?>, LazyOptional<?>> cachedCapabilities, IStrictEnergyHandler handler) {
        if (cachedCapabilities.containsKey(capability)) {
            //If we already contain a cached object for this lazy optional then get it and use it
            LazyOptional<?> cachedCapability = cachedCapabilities.get(capability);
            if (cachedCapability.isPresent()) {
                //If the capability is still present (valid), just return the cached object
                return cachedCapability.cast();
            }
        }
        LazyOptional<T> uncachedCapability = EnergyCompatUtils.getEnergyCapability(capability, handler);
        cachedCapabilities.put(capability, uncachedCapability);
        return uncachedCapability;
    }
}