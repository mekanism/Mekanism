package mekanism.common.capabilities.fluid;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.IntSupplier;
import java.util.function.Predicate;
import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.api.Action;
import mekanism.api.annotations.NonNull;
import mekanism.api.fluid.IExtendedFluidTank;
import mekanism.api.fluid.IMekanismFluidHandler;
import mekanism.api.inventory.AutomationType;
import mekanism.common.tier.FluidTankTier;
import net.minecraftforge.fluids.FluidStack;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class RateLimitFluidHandler extends ItemStackMekanismFluidHandler {

    public static RateLimitFluidHandler create(IntSupplier rate, IntSupplier capacity) {
        //TODO: Validate capacity and rate are positive?
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
        //TODO: FluidHandler - "Fix" Rate limit of fluid tank/bypass it so that we can use bucket mode for low rate tanks
        //TODO: FluidHandler - Rate limit the filling from gas tank slot and fluid tanks lot
    }

    @Override
    protected List<IExtendedFluidTank> getInitialTanks() {
        return Collections.singletonList(tank);
    }

    private static class RateLimitFluidTank extends VariableCapacityFluidTank {

        private final IntSupplier rate;

        private RateLimitFluidTank(IntSupplier rate, IntSupplier capacity, BiPredicate<@NonNull FluidStack, @NonNull AutomationType> canExtract,
              BiPredicate<@NonNull FluidStack, @NonNull AutomationType> canInsert, Predicate<@NonNull FluidStack> isValid, IMekanismFluidHandler fluidHandler) {
            super(capacity, canExtract, canInsert, isValid, fluidHandler);
            this.rate = rate;
        }

        @Override
        protected int getRate() {
            return rate.getAsInt();
        }
    }

    private static class FluidTankRateLimitFluidTank extends RateLimitFluidTank {

        private boolean isCreative;

        private FluidTankRateLimitFluidTank(FluidTankTier tier, IMekanismFluidHandler fluidHandler) {
            super(tier::getOutput, tier::getStorage, BasicFluidTank.alwaysTrueBi, BasicFluidTank.alwaysTrueBi, BasicFluidTank.alwaysTrue, fluidHandler);
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
         * Note: We are only patching {@link #setStackSize(int, Action)}, as both {@link #growStack(int, Action)} and {@link #shrinkStack(int, Action)} are wrapped
         * through this method.
         */
        @Override
        public int setStackSize(int amount, Action action) {
            return super.setStackSize(amount, action.combine(!isCreative));
        }
    }
}