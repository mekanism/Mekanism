package mekanism.common.capabilities.energy;

import java.util.Objects;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.api.Action;
import mekanism.api.IContentsListener;
import mekanism.api.inventory.AutomationType;
import mekanism.api.math.FloatingLong;
import mekanism.api.math.FloatingLongSupplier;
import mekanism.common.tier.EnergyCubeTier;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class EnergyCubeEnergyContainer extends BasicEnergyContainer {

    public static EnergyCubeEnergyContainer create(EnergyCubeTier tier, @Nullable IContentsListener listener) {
        Objects.requireNonNull(tier, "Energy cube tier cannot be null");
        return new EnergyCubeEnergyContainer(tier, listener);
    }

    private final boolean isCreative;
    private final FloatingLongSupplier rate;

    private EnergyCubeEnergyContainer(EnergyCubeTier tier, @Nullable IContentsListener listener) {
        super(tier.getMaxEnergy(), alwaysTrue, alwaysTrue, listener);
        isCreative = tier == EnergyCubeTier.CREATIVE;
        rate = tier::getOutput;
    }

    @Override
    protected FloatingLong getRate(@Nullable AutomationType automationType) {
        //Only limit the internal rate so as to change the speed at which this can be filled from an item
        return automationType == AutomationType.INTERNAL ? rate.get() : super.getRate(automationType);
    }

    @Override
    public FloatingLong insert(FloatingLong amount, Action action, AutomationType automationType) {
        //Note: Unlike other creative items, the creative energy cube does not allow changing it to always full
        return super.insert(amount, action.combine(!isCreative), automationType);
    }

    @Override
    public FloatingLong extract(FloatingLong amount, Action action, AutomationType automationType) {
        return super.extract(amount, action.combine(!isCreative), automationType);
    }
}