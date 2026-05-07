package mekanism.common.capabilities.fluid.item;

import java.util.function.BiPredicate;
import java.util.function.IntSupplier;
import java.util.function.Predicate;
import mekanism.api.AutomationType;
import mekanism.api.IContentsListener;
import mekanism.api.fluid.IFluidTank;
import mekanism.api.functions.ConstantPredicates;
import mekanism.common.attachments.containers.fluid.ComponentBackedFluidTank;
import mekanism.common.attachments.containers.fluid.FluidTanksBuilder;
import mekanism.common.capabilities.GenericTankSpec;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.util.TriPredicate;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import org.jetbrains.annotations.Nullable;

public class FluidTankSpec extends GenericTankSpec<FluidResource> {

    private final IntSupplier rate;
    private final IntSupplier capacity;

    public FluidTankSpec(IntSupplier rate, IntSupplier capacity, BiPredicate<FluidResource, AutomationType> canExtract,
          TriPredicate<FluidResource, AutomationType, ItemStack> canInsert, Predicate<FluidResource> isValid, Predicate<ItemStack> supportsStack) {
        super(canExtract, canInsert, isValid, supportsStack);
        this.rate = rate;
        this.capacity = capacity;
    }

    public <TANK extends IFluidTank> TANK createTank(TankFromSpecCreator<TANK> tankCreator, ItemStack stack) {
        return tankCreator.create(rate, capacity, canExtract, (fluidType, automationType) -> canInsert.test(fluidType, automationType, stack), isValid, null);
    }

    //TODO - 1.20.5: Re-evaluate this
    public void addTank(FluidTanksBuilder builder, ComponentTankFromSpecCreator tankCreator) {
        builder.addTank((type, attachedTo, containerIndex) -> tankCreator.create(attachedTo, containerIndex, canExtract,
              (chemical, automationType) -> canInsert.test(chemical, automationType, attachedTo), isValid, rate, capacity));
    }

    public static FluidTankSpec create(IntSupplier rate, IntSupplier capacity) {
        return new FluidTankSpec(rate, capacity, ConstantPredicates.alwaysTrueBi(), ConstantPredicates.alwaysTrueTri(), ConstantPredicates.alwaysTrue(),
              ConstantPredicates.alwaysTrue());
    }

    public static FluidTankSpec createFillOnly(IntSupplier rate, IntSupplier capacity, Predicate<FluidResource> isValid) {
        return createFillOnly(rate, capacity, isValid, ConstantPredicates.alwaysTrue());
    }

    public static FluidTankSpec createFillOnly(IntSupplier rate, IntSupplier capacity, Predicate<FluidResource> isValid, Predicate<ItemStack> supportsStack) {
        return new FluidTankSpec(rate, capacity, ConstantPredicates.notExternal(), (_, _, stack) -> supportsStack.test(stack), isValid, supportsStack);
    }

    @FunctionalInterface
    public interface ComponentTankFromSpecCreator {

        ComponentBackedFluidTank create(ItemStack attachedTo, int tankIndex, BiPredicate<FluidResource, AutomationType> canExtract,
              BiPredicate<FluidResource, AutomationType> canInsert, Predicate<FluidResource> isValid, IntSupplier rate, IntSupplier capacity);
    }

    @FunctionalInterface
    public interface TankFromSpecCreator<TANK extends IFluidTank> {

        TANK create(IntSupplier rate, IntSupplier capacity, BiPredicate<FluidResource, AutomationType> canExtract, BiPredicate<FluidResource, AutomationType> canInsert,
              Predicate<FluidResource> isValid, @Nullable IContentsListener listener);

        default TANK create(IntSupplier rate, IntSupplier capacity, BiPredicate<FluidResource, AutomationType> canExtract,
              BiPredicate<FluidResource, AutomationType> canInsert, Predicate<FluidResource> isValid) {
            return create(rate, capacity, canExtract, canInsert, isValid, null);
        }
    }
}