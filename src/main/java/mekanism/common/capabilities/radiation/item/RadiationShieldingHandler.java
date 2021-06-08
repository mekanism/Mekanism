package mekanism.common.capabilities.radiation.item;

import java.util.Objects;
import java.util.function.ToDoubleFunction;
import mekanism.api.radiation.capability.IRadiationShielding;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.capabilities.CapabilityCache;
import mekanism.common.capabilities.ItemCapabilityWrapper.ItemCapability;
import mekanism.common.capabilities.resolver.BasicCapabilityResolver;
import net.minecraft.item.ItemStack;

public class RadiationShieldingHandler extends ItemCapability implements IRadiationShielding {

    public static RadiationShieldingHandler create(ToDoubleFunction<ItemStack> shieldingFunction) {
        Objects.requireNonNull(shieldingFunction, "Shielding function cannot be null");
        return new RadiationShieldingHandler(shieldingFunction);
    }

    private final ToDoubleFunction<ItemStack> shieldingFunction;

    private RadiationShieldingHandler(ToDoubleFunction<ItemStack> shieldingFunction) {
        this.shieldingFunction = shieldingFunction;
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