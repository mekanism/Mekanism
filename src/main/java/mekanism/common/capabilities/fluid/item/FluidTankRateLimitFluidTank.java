package mekanism.common.capabilities.fluid.item;

import java.util.Objects;
import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.IContentsListener;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.common.capabilities.fluid.BasicFluidTank;
import mekanism.common.tier.FluidTankTier;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public class FluidTankRateLimitFluidTank extends RateLimitFluidTank {

    public static FluidTankRateLimitFluidTank create(FluidTankTier tier) {
        Objects.requireNonNull(tier, "Fluid tank tier cannot be null");
        return new FluidTankRateLimitFluidTank(tier, null);
    }

    private final boolean isCreative;

    private FluidTankRateLimitFluidTank(FluidTankTier tier, @Nullable IContentsListener listener) {
        super(tier::getOutput, tier::getStorage, BasicFluidTank.alwaysTrueBi, BasicFluidTank.alwaysTrueBi, BasicFluidTank.alwaysTrue, listener);
        isCreative = tier == FluidTankTier.CREATIVE;
    }

    @Override
    public FluidStack insert(FluidStack stack, Action action, AutomationType automationType) {
        return super.insert(stack, action.combine(!isCreative), automationType);
    }

    @Override
    public FluidStack extract(int amount, Action action, AutomationType automationType) {
        return super.extract(amount, action.combine(!isCreative), automationType);
    }

    /**
     * {@inheritDoc}
     *
     * Note: We are only patching {@link #setStackSize(int, Action)}, as both {@link #growStack(int, Action)} and {@link #shrinkStack(int, Action)} are wrapped through
     * this method.
     */
    @Override
    public int setStackSize(int amount, Action action) {
        return super.setStackSize(amount, action.combine(!isCreative));
    }
}