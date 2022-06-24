package mekanism.common.capabilities.fluid.item;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.BiPredicate;
import java.util.function.IntSupplier;
import java.util.function.Predicate;
import mekanism.api.AutomationType;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.fluid.IExtendedFluidTank;
import mekanism.common.capabilities.fluid.BasicFluidTank;
import mekanism.common.capabilities.fluid.item.RateLimitFluidHandler.RateLimitFluidTank;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.util.TriPredicate;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;

@NothingNullByDefault
public class RateLimitMultiTankFluidHandler extends ItemStackMekanismFluidHandler {

    public static RateLimitMultiTankFluidHandler create(@NotNull Collection<FluidTankSpec> fluidTanks) {
        return new RateLimitMultiTankFluidHandler(fluidTanks);
    }

    private final List<IExtendedFluidTank> tanks;

    private RateLimitMultiTankFluidHandler(@NotNull Collection<FluidTankSpec> fluidTanks) {
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

        private static final TriPredicate<@NotNull FluidStack, @NotNull AutomationType, @NotNull ItemStack> alwaysTrue = (gas, automationType, stack) -> true;

        final IntSupplier rate;
        final IntSupplier capacity;
        final Predicate<@NotNull FluidStack> isValid;
        final BiPredicate<@NotNull FluidStack, @NotNull AutomationType> canExtract;
        final TriPredicate<@NotNull FluidStack, @NotNull AutomationType, @NotNull ItemStack> canInsert;

        public FluidTankSpec(IntSupplier rate, IntSupplier capacity, BiPredicate<@NotNull FluidStack, @NotNull AutomationType> canExtract,
              TriPredicate<@NotNull FluidStack, @NotNull AutomationType, @NotNull ItemStack> canInsert, Predicate<@NotNull FluidStack> isValid) {
            this.rate = rate;
            this.capacity = capacity;
            this.isValid = isValid;
            this.canExtract = canExtract;
            this.canInsert = canInsert;
        }

        public static FluidTankSpec create(IntSupplier rate, IntSupplier capacity) {
            return new FluidTankSpec(rate, capacity, BasicFluidTank.alwaysTrueBi, alwaysTrue, BasicFluidTank.alwaysTrue);
        }

        public static FluidTankSpec createFillOnly(IntSupplier rate, IntSupplier capacity, Predicate<@NotNull FluidStack> isValid) {
            return createFillOnly(rate, capacity, alwaysTrue, isValid);
        }

        public static FluidTankSpec createFillOnly(IntSupplier rate, IntSupplier capacity,
              TriPredicate<@NotNull FluidStack, @NotNull AutomationType, @NotNull ItemStack> canInsert, Predicate<@NotNull FluidStack> isValid) {
            return new FluidTankSpec(rate, capacity, BasicFluidTank.notExternal, canInsert, isValid);
        }

        public boolean isValid(@NotNull FluidStack gas) {
            return isValid.test(gas);
        }
    }
}
