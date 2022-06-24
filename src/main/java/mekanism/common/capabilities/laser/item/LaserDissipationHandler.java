package mekanism.common.capabilities.laser.item;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.ToDoubleFunction;
import mekanism.api.lasers.ILaserDissipation;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.capabilities.ItemCapabilityWrapper.ItemCapability;
import mekanism.common.capabilities.resolver.BasicCapabilityResolver;
import mekanism.common.capabilities.resolver.ICapabilityResolver;
import net.minecraft.world.item.ItemStack;

public class LaserDissipationHandler extends ItemCapability implements ILaserDissipation {

    public static LaserDissipationHandler create(ToDoubleFunction<ItemStack> dissipationFunction, ToDoubleFunction<ItemStack> refractionFunction) {
        Objects.requireNonNull(dissipationFunction, "Dissipation function cannot be null");
        Objects.requireNonNull(refractionFunction, "Refraction function cannot be null");
        return new LaserDissipationHandler(dissipationFunction, refractionFunction);
    }

    private final ToDoubleFunction<ItemStack> dissipationFunction;
    private final ToDoubleFunction<ItemStack> refractionFunction;

    private LaserDissipationHandler(ToDoubleFunction<ItemStack> dissipationFunction, ToDoubleFunction<ItemStack> refractionFunction) {
        this.dissipationFunction = dissipationFunction;
        this.refractionFunction = refractionFunction;
    }

    @Override
    public double getDissipationPercent() {
        return dissipationFunction.applyAsDouble(getStack());
    }

    @Override
    public double getRefractionPercent() {
        return refractionFunction.applyAsDouble(getStack());
    }

    @Override
    protected void gatherCapabilityResolvers(Consumer<ICapabilityResolver> consumer) {
        consumer.accept(BasicCapabilityResolver.constant(Capabilities.LASER_DISSIPATION, this));
    }
}