package mekanism.common.capabilities.radiation.item;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.ToDoubleFunction;
import mekanism.api.radiation.capability.IRadiationShielding;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.capabilities.ItemCapabilityWrapper.ItemCapability;
import mekanism.common.capabilities.resolver.BasicCapabilityResolver;
import mekanism.common.capabilities.resolver.ICapabilityResolver;
import net.minecraft.world.item.ItemStack;

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
    protected void gatherCapabilityResolvers(Consumer<ICapabilityResolver> consumer) {
        consumer.accept(BasicCapabilityResolver.constant(Capabilities.RADIATION_SHIELDING, this));
    }
}