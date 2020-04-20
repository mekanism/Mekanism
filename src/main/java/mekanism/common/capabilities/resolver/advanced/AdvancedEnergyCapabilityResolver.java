package mekanism.common.capabilities.resolver.advanced;

import java.util.List;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.api.energy.ISidedStrictEnergyHandler;
import mekanism.api.energy.IStrictEnergyHandler;
import mekanism.common.capabilities.proxy.ProxyStrictEnergyHandler;
import mekanism.common.capabilities.resolver.ICapabilityResolver;
import mekanism.common.integration.energy.EnergyCompatUtils;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class AdvancedEnergyCapabilityResolver implements ICapabilityResolver {

    private final IStrictEnergyHandler handler;
    private final IStrictEnergyHandler readOnlyHandler;

    public AdvancedEnergyCapabilityResolver(ISidedStrictEnergyHandler handler) {
        this.handler = handler;
        this.readOnlyHandler = new ProxyStrictEnergyHandler(handler, null, null);
    }

    @Override
    public List<Capability<?>> getSupportedCapabilities() {
        return EnergyCompatUtils.getEnabledEnergyCapabilities();
    }

    @Override
    public <T> LazyOptional<T> resolve(Capability<T> capability, @Nullable Direction side) {
        return EnergyCompatUtils.getEnergyCapability(capability, side == null ? readOnlyHandler : handler);
    }
}