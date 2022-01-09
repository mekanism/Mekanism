package mekanism.common.capabilities.fluid.item;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.BiPredicate;
import java.util.function.IntSupplier;
import java.util.function.Predicate;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import mekanism.api.AutomationType;
import mekanism.api.annotations.NonNull;
import mekanism.api.fluid.IExtendedFluidTank;
import mekanism.common.capabilities.fluid.BasicFluidTank;
import mekanism.common.capabilities.fluid.item.RateLimitFluidHandler.RateLimitFluidTank;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.util.TriPredicate;
import net.minecraftforge.fluids.FluidStack;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class RateLimitMultiTankFluidHandler extends ItemStackMekanismFluidHandler {

    public static RateLimitMultiTankFluidHandler create(@Nonnull Collection<FluidTankSpec> fluidTanks) {
        return new RateLimitMultiTankFluidHandler(fluidTanks);
    }

    private final List<IExtendedFluidTank> tanks;

    private RateLimitMultiTankFluidHandler(@Nonnull Collection<FluidTankSpec> fluidTanks) {
        List<IExtendedFluidTank> tankProviders = new ArrayList<>();
        for (FluidTankSpec spec : fluidTanks) {
            tankProviders.add(new RateLimitFluidTank(spec.rate, spec.capacity, spec.canExtract,
                  (fluid, automationType) -> spec.canInsert.test(fluid, automationType, getStack()), spec.isValid, this));
        }
        tanks = Collections.unmodifiableList(tankProviders);
    }

    @Override
    protected List<IExtendedFluidTank> getInitialTanks() {
        return tanks;
    }

    public static class FluidTankSpec {

        private static final TriPredicate<@NonNull FluidStack, @NonNull AutomationType, @NonNull ItemStack> alwaysTrue = (gas, automationType, stack) -> true;

        final IntSupplier rate;
        final IntSupplier capacity;
        final Predicate<@NonNull FluidStack> isValid;
        final BiPredicate<@NonNull FluidStack, @NonNull AutomationType> canExtract;
        final TriPredicate<@NonNull FluidStack, @NonNull AutomationType, @NonNull ItemStack> canInsert;

        public FluidTankSpec(IntSupplier rate, IntSupplier capacity, BiPredicate<@NonNull FluidStack, @NonNull AutomationType> canExtract,
              TriPredicate<@NonNull FluidStack, @NonNull AutomationType, @NonNull ItemStack> canInsert, Predicate<@NonNull FluidStack> isValid) {
            this.rate = rate;
            this.capacity = capacity;
            this.isValid = isValid;
            this.canExtract = canExtract;
            this.canInsert = canInsert;
        }

        public static FluidTankSpec create(IntSupplier rate, IntSupplier capacity) {
            return new FluidTankSpec(rate, capacity, BasicFluidTank.alwaysTrueBi, alwaysTrue, BasicFluidTank.alwaysTrue);
        }

        public static FluidTankSpec createFillOnly(IntSupplier rate, IntSupplier capacity, Predicate<@NonNull FluidStack> isValid) {
            return createFillOnly(rate, capacity, alwaysTrue, isValid);
        }

        public static FluidTankSpec createFillOnly(IntSupplier rate, IntSupplier capacity,
              TriPredicate<@NonNull FluidStack, @NonNull AutomationType, @NonNull ItemStack> canInsert, Predicate<@NonNull FluidStack> isValid) {
            return new FluidTankSpec(rate, capacity, BasicFluidTank.notExternal, canInsert, isValid);
        }

        public boolean isValid(@NonNull FluidStack gas) {
            return isValid.test(gas);
        }
    }
}
