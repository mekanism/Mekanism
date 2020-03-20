package mekanism.common.capabilities.energy;

import java.util.Objects;
import java.util.function.DoubleSupplier;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.api.Action;
import mekanism.api.energy.IMekanismStrictEnergyHandler;
import mekanism.api.inventory.AutomationType;
import mekanism.common.tier.EnergyCubeTier;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class EnergyCubeEnergyContainer extends BasicEnergyContainer {

    public static EnergyCubeEnergyContainer create(EnergyCubeTier tier, @Nullable IMekanismStrictEnergyHandler energyHandler) {
        Objects.requireNonNull(tier, "Energy cube tier cannot be null");
        return new EnergyCubeEnergyContainer(tier, energyHandler);
    }

    private final boolean isCreative;
    private final DoubleSupplier rate;

    private EnergyCubeEnergyContainer(EnergyCubeTier tier, @Nullable IMekanismStrictEnergyHandler energyHandler) {
        super(tier.getMaxEnergy(), alwaysTrue, alwaysTrue, energyHandler);
        isCreative = tier == EnergyCubeTier.CREATIVE;
        rate = tier::getOutput;
    }

    @Override
    protected double getRate(@Nullable AutomationType automationType) {
        //Only limit the internal rate so as to change the speed at which this can be filled from an item
        return automationType == AutomationType.INTERNAL ? rate.getAsDouble() : super.getRate(automationType);
    }

    @Override
    public double insert(double amount, Action action, AutomationType automationType) {
        //Note: Unlike other creative items, the creative energy cube does not allow changing it to always full
        return super.insert(amount, action.combine(!isCreative), automationType);
    }

    @Override
    public double extract(double amount, Action action, AutomationType automationType) {
        return super.extract(amount, action.combine(!isCreative), automationType);
    }
}