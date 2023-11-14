package mekanism.common.capabilities.laser.item;

import java.util.Objects;
import java.util.function.ToDoubleFunction;
import mekanism.api.lasers.ILaserDissipation;
import net.minecraft.world.item.ItemStack;

public class LaserDissipationHandler implements ILaserDissipation {

    public static LaserDissipationHandler create(ItemStack stack, ToDoubleFunction<ItemStack> dissipationFunction, ToDoubleFunction<ItemStack> refractionFunction) {
        Objects.requireNonNull(dissipationFunction, "Dissipation function cannot be null");
        Objects.requireNonNull(refractionFunction, "Refraction function cannot be null");
        return new LaserDissipationHandler(stack, dissipationFunction, refractionFunction);
    }

    protected final ItemStack stack;
    private final ToDoubleFunction<ItemStack> dissipationFunction;
    private final ToDoubleFunction<ItemStack> refractionFunction;

    private LaserDissipationHandler(ItemStack stack, ToDoubleFunction<ItemStack> dissipationFunction, ToDoubleFunction<ItemStack> refractionFunction) {
        this.stack = stack;
        this.dissipationFunction = dissipationFunction;
        this.refractionFunction = refractionFunction;
    }

    @Override
    public double getDissipationPercent() {
        return dissipationFunction.applyAsDouble(stack);
    }

    @Override
    public double getRefractionPercent() {
        return refractionFunction.applyAsDouble(stack);
    }
}