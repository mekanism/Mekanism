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
import mekanism.api.functions.ConstantPredicates;
import mekanism.common.capabilities.GenericTankSpec;
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

    public static class FluidTankSpec extends GenericTankSpec<FluidStack> {

        final IntSupplier rate;
        final IntSupplier capacity;

        public FluidTankSpec(IntSupplier rate, IntSupplier capacity, BiPredicate<@NotNull FluidStack, @NotNull AutomationType> canExtract,
              TriPredicate<@NotNull FluidStack, @NotNull AutomationType, @NotNull ItemStack> canInsert, Predicate<@NotNull FluidStack> isValid,
              Predicate<@NotNull ItemStack> supportsStack) {
            super(canExtract, canInsert, isValid, supportsStack);
            this.rate = rate;
            this.capacity = capacity;
        }

        public static FluidTankSpec create(IntSupplier rate, IntSupplier capacity) {
            return new FluidTankSpec(rate, capacity, ConstantPredicates.alwaysTrueBi(), ConstantPredicates.alwaysTrueTri(), ConstantPredicates.alwaysTrue(),
                  ConstantPredicates.alwaysTrue());
        }

        public static FluidTankSpec createFillOnly(IntSupplier rate, IntSupplier capacity, Predicate<@NotNull FluidStack> isValid) {
            return createFillOnly(rate, capacity, isValid, ConstantPredicates.alwaysTrue());
        }

        public static FluidTankSpec createFillOnly(IntSupplier rate, IntSupplier capacity, Predicate<@NotNull FluidStack> isValid,
              Predicate<@NotNull ItemStack> supportsStack) {
            return new FluidTankSpec(rate, capacity, ConstantPredicates.notExternal(), (chemical, automation, stack) -> supportsStack.test(stack), isValid, supportsStack);
        }
    }
}
