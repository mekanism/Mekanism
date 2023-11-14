package mekanism.common.capabilities.fluid.item;

import java.util.Collection;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.IntSupplier;
import java.util.function.Predicate;
import mekanism.api.AutomationType;
import mekanism.api.IContentsListener;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.fluid.IExtendedFluidTank;
import mekanism.api.functions.ConstantPredicates;
import mekanism.common.capabilities.GenericTankSpec;
import mekanism.common.capabilities.fluid.item.RateLimitFluidHandler.RateLimitFluidTank;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.util.TriPredicate;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;

@NothingNullByDefault
public class RateLimitMultiTankFluidHandler extends ItemStackMekanismFluidHandler {

    public static RateLimitMultiTankFluidHandler create(ItemStack stack, Collection<FluidTankSpec> fluidTanks) {
        return new RateLimitMultiTankFluidHandler(stack, fluidTanks);
    }

    @SuppressWarnings("unchecked")
    private RateLimitMultiTankFluidHandler(ItemStack stack, Collection<FluidTankSpec> fluidTanks) {
        super(stack, fluidTanks.stream()
              .map(spec -> (Function<IContentsListener, IExtendedFluidTank>) listener -> new RateLimitFluidTank(spec.rate, spec.capacity, spec.canExtract,
                    (fluid, automationType) -> spec.canInsert.test(fluid, automationType, stack), spec.isValid, listener))
              .toArray(Function[]::new));
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
