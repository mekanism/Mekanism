package mekanism.common.capabilities.fluid.item;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.IntSupplier;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.api.Action;
import mekanism.api.annotations.NonNull;
import mekanism.api.fluid.IExtendedFluidTank;
import mekanism.api.fluid.IMekanismFluidHandler;
import mekanism.api.inventory.AutomationType;
import mekanism.common.capabilities.fluid.BasicFluidTank;
import mekanism.common.capabilities.fluid.VariableCapacityFluidTank;
import mekanism.common.tier.FluidTankTier;
import net.minecraftforge.fluids.FluidStack;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class RateLimitFluidHandler extends ItemStackMekanismFluidHandler {

    public static RateLimitFluidHandler create(int rate, IntSupplier capacity) {
        if (rate <= 0) {
            throw new IllegalArgumentException("Rate must be greater than zero");
        }
        Objects.requireNonNull(capacity, "Capacity supplier cannot be null");
        return new RateLimitFluidHandler(handler -> new RateLimitFluidTank(rate, capacity, BasicFluidTank.alwaysTrueBi, BasicFluidTank.alwaysTrueBi,
              BasicFluidTank.alwaysTrue, handler));
    }

    public static RateLimitFluidHandler create(FluidTankTier tier) {
        Objects.requireNonNull(tier, "Fluid tank tier cannot be null");
        return new RateLimitFluidHandler(handler -> new FluidTankRateLimitFluidTank(tier, handler));
    }

    private IExtendedFluidTank tank;

    private RateLimitFluidHandler(Function<IMekanismFluidHandler, IExtendedFluidTank> tankProvider) {
        tank = tankProvider.apply(this);
    }

    @Override
    protected List<IExtendedFluidTank> getInitialTanks() {
        return Collections.singletonList(tank);
    }

    private static class RateLimitFluidTank extends VariableCapacityFluidTank {

        private final int rate;

        private RateLimitFluidTank(int rate, IntSupplier capacity, BiPredicate<@NonNull FluidStack, @NonNull AutomationType> canExtract,
              BiPredicate<@NonNull FluidStack, @NonNull AutomationType> canInsert, Predicate<@NonNull FluidStack> isValid, IMekanismFluidHandler fluidHandler) {
            super(capacity, canExtract, canInsert, isValid, fluidHandler);
            this.rate = rate;
        }

        @Override
        protected int getRate(@Nullable AutomationType automationType) {
            //Allow unknown or manual interaction to bypass rate limit for the item
            return automationType == null || automationType == AutomationType.MANUAL ? super.getRate(automationType) : rate;
        }
    }

    private static class FluidTankRateLimitFluidTank extends VariableCapacityFluidTank {

        private final IntSupplier rate;
        private final boolean isCreative;

        private FluidTankRateLimitFluidTank(FluidTankTier tier, IMekanismFluidHandler fluidHandler) {
            super(tier::getStorage, BasicFluidTank.alwaysTrueBi, BasicFluidTank.alwaysTrueBi, BasicFluidTank.alwaysTrue, fluidHandler);
            isCreative = tier == FluidTankTier.CREATIVE;
            rate = tier::getOutput;
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
         * Note: We are only patching {@link #setStackSize(int, Action)}, as both {@link #growStack(int, Action)} and {@link #shrinkStack(int, Action)} are wrapped
         * through this method.
         */
        @Override
        public int setStackSize(int amount, Action action) {
            return super.setStackSize(amount, action.combine(!isCreative));
        }

        @Override
        protected int getRate(@Nullable AutomationType automationType) {
            //Allow unknown or manual interaction to bypass rate limit for the item
            return automationType == null || automationType == AutomationType.MANUAL ? super.getRate(automationType) : rate.getAsInt();
        }
    }
}