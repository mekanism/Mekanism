package mekanism.common.capabilities.energy.item;

import java.util.Objects;
import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.IContentsListener;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.math.FloatingLong;
import mekanism.common.capabilities.energy.BasicEnergyContainer;
import mekanism.common.tier.EnergyCubeTier;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public class EnergyCubeRateLimitEnergyContainer extends RateLimitEnergyContainer {

    public static EnergyCubeRateLimitEnergyContainer create(EnergyCubeTier tier) {
        Objects.requireNonNull(tier, "Energy cube tier cannot be null");
        return new EnergyCubeRateLimitEnergyContainer(tier, null);
    }

    private final boolean isCreative;

    private EnergyCubeRateLimitEnergyContainer(EnergyCubeTier tier, @Nullable IContentsListener listener) {
        super(tier::getOutput, tier::getMaxEnergy, BasicEnergyContainer.alwaysTrue, BasicEnergyContainer.alwaysTrue, listener);
        isCreative = tier == EnergyCubeTier.CREATIVE;
    }

    @Override
    public FloatingLong insert(FloatingLong amount, Action action, AutomationType automationType) {
        return super.insert(amount, action.combine(!isCreative), automationType);
    }

    @Override
    public FloatingLong extract(FloatingLong amount, Action action, AutomationType automationType) {
        return super.extract(amount, action.combine(!isCreative), automationType);
    }
}