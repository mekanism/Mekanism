package mekanism.common.capabilities.resolver.advanced;

import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.api.chemical.gas.IGasHandler;
import mekanism.api.heat.IHeatHandler;
import mekanism.common.capabilities.Capabilities;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.NonNullLazy;
import net.minecraftforge.common.util.NonNullSupplier;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;

//TODO: JavaDocs, calculated result persists through invalidation
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class AdvancedPersistentCapabilityResolver extends AdvancedCapabilityResolver {

    public static AdvancedPersistentCapabilityResolver heatHandler(NonNullSupplier<IHeatHandler> supplier, NonNullSupplier<IHeatHandler> readOnlySupplier) {
        return create(Capabilities.HEAT_HANDLER_CAPABILITY, supplier, readOnlySupplier);
    }

    public static AdvancedPersistentCapabilityResolver gasHandler(NonNullSupplier<IGasHandler> supplier, NonNullSupplier<IGasHandler> readOnlySupplier) {
        return create(Capabilities.GAS_HANDLER_CAPABILITY, supplier, readOnlySupplier);
    }

    public static AdvancedPersistentCapabilityResolver fluidHandler(NonNullSupplier<IFluidHandler> supplier, NonNullSupplier<IFluidHandler> readOnlySupplier) {
        return create(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, supplier, readOnlySupplier);
    }

    public static <T> AdvancedPersistentCapabilityResolver create(Capability<T> supportedCapability, NonNullSupplier<T> supplier, NonNullSupplier<T> readOnlySupplier) {
        return new AdvancedPersistentCapabilityResolver(supportedCapability, supplier, readOnlySupplier);
    }

    protected <T> AdvancedPersistentCapabilityResolver(Capability<T> supportedCapability, NonNullSupplier<T> supplier, NonNullSupplier<T> readOnlySupplier) {
        super(supportedCapability, NonNullLazy.of(supplier), NonNullLazy.of(readOnlySupplier));
    }
}