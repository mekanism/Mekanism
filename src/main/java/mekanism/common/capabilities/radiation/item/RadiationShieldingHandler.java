package mekanism.common.capabilities.radiation.item;

import java.util.function.ToDoubleFunction;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.capabilities.CapabilityCache;
import mekanism.common.capabilities.ItemCapabilityWrapper.ItemCapability;
import mekanism.common.capabilities.resolver.basic.BasicCapabilityResolver;
import mekanism.common.lib.radiation.capability.IRadiationShielding;
import net.minecraft.item.ItemStack;

public class RadiationShieldingHandler extends ItemCapability implements IRadiationShielding {

    private ToDoubleFunction<ItemStack> shieldingFunction;

    public static RadiationShieldingHandler create(ToDoubleFunction<ItemStack> shieldingFunction) {
        RadiationShieldingHandler handler = new RadiationShieldingHandler();
        handler.shieldingFunction = shieldingFunction;
        return handler;
    }

    @Override
    public double getRadiationShielding() {
        return shieldingFunction.applyAsDouble(getStack());
    }

    @Override
    protected void addCapabilityResolvers(CapabilityCache capabilityCache) {
        capabilityCache.addCapabilityResolver(BasicCapabilityResolver.constant(Capabilities.RADIATION_SHIELDING_CAPABILITY, this));
    }
}
