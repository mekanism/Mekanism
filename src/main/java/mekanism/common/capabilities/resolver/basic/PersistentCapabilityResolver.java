package mekanism.common.capabilities.resolver.basic;

import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.api.IConfigCardAccess;
import mekanism.api.IConfigCardAccess.ISpecialConfigData;
import mekanism.api.IConfigurable;
import mekanism.api.chemical.gas.IGasHandler;
import mekanism.api.chemical.infuse.IInfusionHandler;
import mekanism.api.heat.IHeatHandler;
import mekanism.api.lasers.ILaserReceptor;
import mekanism.common.capabilities.Capabilities;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.NonNullLazy;
import net.minecraftforge.common.util.NonNullSupplier;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;

//TODO: JavaDocs, calculated result persists through invalidation
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class PersistentCapabilityResolver extends BasicCapabilityResolver {

    public static PersistentCapabilityResolver configCard(NonNullSupplier<IConfigCardAccess> supplier) {
        return create(Capabilities.CONFIG_CARD_CAPABILITY, supplier);
    }

    public static PersistentCapabilityResolver specialConfigData(NonNullSupplier<ISpecialConfigData> supplier) {
        return create(Capabilities.SPECIAL_CONFIG_DATA_CAPABILITY, supplier);
    }

    public static PersistentCapabilityResolver configurable(NonNullSupplier<IConfigurable> supplier) {
        return create(Capabilities.CONFIGURABLE_CAPABILITY, supplier);
    }

    public static PersistentCapabilityResolver laserReceptor(NonNullSupplier<ILaserReceptor> supplier) {
        return create(Capabilities.LASER_RECEPTOR_CAPABILITY, supplier);
    }

    public static PersistentCapabilityResolver heatHandler(NonNullSupplier<IHeatHandler> supplier) {
        return create(Capabilities.HEAT_HANDLER_CAPABILITY, supplier);
    }

    public static PersistentCapabilityResolver fluidHandlerItem(NonNullSupplier<IFluidHandlerItem> supplier) {
        return create(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, supplier);
    }

    public static PersistentCapabilityResolver gasHandler(NonNullSupplier<IGasHandler> supplier) {
        return create(Capabilities.GAS_HANDLER_CAPABILITY, supplier);
    }

    public static PersistentCapabilityResolver infusionHandler(NonNullSupplier<IInfusionHandler> supplier) {
        return create(Capabilities.INFUSION_HANDLER_CAPABILITY, supplier);
    }

    public static <T> PersistentCapabilityResolver create(Capability<T> supportedCapability, NonNullSupplier<T> supplier) {
        return new PersistentCapabilityResolver(supportedCapability, supplier);
    }

    protected <T> PersistentCapabilityResolver(Capability<T> supportedCapability, NonNullSupplier<T> supplier) {
        super(supportedCapability, NonNullLazy.of(supplier));
    }
}