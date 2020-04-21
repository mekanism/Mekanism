package mekanism.common.capabilities.resolver.advanced;

import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.api.energy.ISidedStrictEnergyHandler;
import mekanism.api.energy.IStrictEnergyHandler;
import mekanism.common.capabilities.proxy.ProxyStrictEnergyHandler;
import mekanism.common.capabilities.resolver.basic.EnergyCapabilityResolver;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class AdvancedEnergyCapabilityResolver extends EnergyCapabilityResolver {

    private final Map<Capability<?>, LazyOptional<?>> cachedReadOnlyCapabilities = new HashMap<>();
    private final IStrictEnergyHandler readOnlyHandler;

    public AdvancedEnergyCapabilityResolver(ISidedStrictEnergyHandler handler) {
        super(handler);
        this.readOnlyHandler = new ProxyStrictEnergyHandler(handler, null, null);
    }

    @Override
    public <T> LazyOptional<T> resolve(Capability<T> capability, @Nullable Direction side) {
        if (side == null) {
            return getCachedOrResolve(capability, cachedReadOnlyCapabilities, readOnlyHandler);
        }
        return super.resolve(capability, side);
    }

    @Override
    public void invalidate(Capability<?> capability, @Nullable Direction side) {
        if (side == null) {
            invalidate(cachedReadOnlyCapabilities.get(capability));
        } else {
            super.invalidate(capability, side);
        }
    }

    @Override
    public void invalidateAll() {
        super.invalidateAll();
        cachedReadOnlyCapabilities.values().forEach(this::invalidate);
    }
}