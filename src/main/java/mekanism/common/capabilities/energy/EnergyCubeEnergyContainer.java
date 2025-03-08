package mekanism.common.capabilities.energy;

import java.util.Objects;
import java.util.function.LongSupplier;
import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.IContentsListener;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.functions.ConstantPredicates;
import mekanism.common.tier.EnergyCubeTier;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public class EnergyCubeEnergyContainer extends BasicEnergyContainer {

    public static EnergyCubeEnergyContainer create(EnergyCubeTier tier, @Nullable IContentsListener listener) {
        Objects.requireNonNull(tier, "Energy cube tier cannot be null");
        return new EnergyCubeEnergyContainer(tier, listener);
    }

    private final boolean isCreative;
    private final LongSupplier rate;

    private EnergyCubeEnergyContainer(EnergyCubeTier tier, @Nullable IContentsListener listener) {
        super(tier.getMaxEnergy(), ConstantPredicates.alwaysTrue(), ConstantPredicates.alwaysTrue(), listener);
        isCreative = tier == EnergyCubeTier.CREATIVE;
        rate = tier::getOutput;
    }

    @Override
    protected long getInsertRate(@Nullable AutomationType automationType) {
        //Only limit the internal rate to change the speed at which this can be filled from an item
        return automationType == AutomationType.INTERNAL ? rate.getAsLong() : super.getInsertRate(automationType);
    }

    @Override
    protected long getExtractRate(@Nullable AutomationType automationType) {
        //Only limit the internal rate to change the speed at which this can be filled from an item
        return automationType == AutomationType.INTERNAL ? rate.getAsLong() : super.getExtractRate(automationType);
    }

    @Override
    public long insert(long amount, Action action, AutomationType automationType) {
        //Note: Unlike other creative items, the creative energy cube does not allow changing it to always full
        return super.insert(amount, action.combine(!isCreative), automationType);
    }

    @Override
    public long extract(long amount, Action action, AutomationType automationType) {
        return super.extract(amount, action.combine(!isCreative), automationType);
    }
}