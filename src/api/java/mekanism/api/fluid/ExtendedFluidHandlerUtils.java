package mekanism.api.fluid;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.ToIntFunction;
import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.container.ContainerInteraction;
import mekanism.api.container.InContainerGetter;
import mekanism.api.container.IntContainerInteraction;
import net.minecraft.core.Direction;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.Nullable;

public class ExtendedFluidHandlerUtils {

    /**
     * Util method for a generic insert implementation for various handlers. Mainly for internal use only
     *
     * @since 10.5.13
     */
    public static FluidStack insert(FluidStack stack, @Nullable Direction side, Action action, ToIntFunction<@Nullable Direction> tankCount, InContainerGetter<FluidStack> inTankGetter,
          ContainerInteraction<FluidStack> insertFluid) {
        if (stack.isEmpty()) {
            //Short circuit if nothing is actually being inserted
            return FluidStack.EMPTY;
        }
        int tanks = tankCount.applyAsInt(side);
        if (tanks == 0) {
            return stack;
        } else if (tanks == 1) {
            return insertFluid.interact(0, stack, side, action);
        }
        FluidStack toInsert = stack;
        //Start by trying to insert into the tanks that have the same type
        IntList emptyTanks = new IntArrayList();
        for (int tank = 0; tank < tanks; tank++) {
            FluidStack inTank = inTankGetter.getStored(tank, side);
            if (inTank.isEmpty()) {
                emptyTanks.add(tank);
            } else if (FluidStack.isSameFluidSameComponents(inTank, stack)) {
                FluidStack remainder = insertFluid.interact(tank, toInsert, side, action);
                if (remainder.isEmpty()) {
                    //If we have no remaining fluid, return that we fit it all
                    return FluidStack.EMPTY;
                }
                //Update what we have left to insert, to be the amount we were unable to insert
                toInsert = remainder;
            }
        }
        for (int tank : emptyTanks) {
            FluidStack remainder = insertFluid.interact(tank, toInsert, side, action);
            if (remainder.isEmpty()) {
                //If we have no remaining fluid, return that we fit it all
                return FluidStack.EMPTY;
            }
            //Update what we have left to insert, to be the amount we were unable to insert
            toInsert = remainder;
        }
        return toInsert;
    }

    /**
     * Util method for a generic insert implementation for various handlers. Mainly for internal use only
     *
     * @since 10.5.13
     */
    public static FluidStack insert(FluidStack stack, @Nullable Direction side, Function<@Nullable Direction, List<IExtendedFluidTank>> fluidTankSupplier,
          Action action, AutomationType automationType) {
        if (stack.isEmpty()) {
            //Short circuit if nothing is actually being inserted
            return FluidStack.EMPTY;
        }
        List<IExtendedFluidTank> fluidTanks = fluidTankSupplier.apply(side);
        return insert(stack, action, automationType, fluidTanks.size(), fluidTanks);
    }

    /**
     * Util method for a generic insert implementation for various handlers. Mainly for internal use only
     *
     * @since 10.6.0
     */
    public static FluidStack insert(FluidStack stack, Action action, AutomationType automationType, int size, List<IExtendedFluidTank> fluidTanks) {
        if (stack.isEmpty()) {
            //Short circuit if nothing is actually being inserted
            return FluidStack.EMPTY;
        } else if (size == 0) {
            return stack;
        } else if (size == 1) {
            //noinspection SequencedCollectionMethodCanBeUsed: we know size
            return fluidTanks.get(0).insert(stack, action, automationType);
        }
        FluidStack toInsert = stack;
        //Start by trying to insert into the tanks that have the same type
        List<IExtendedFluidTank> emptyTanks = new ArrayList<>();
        for (IExtendedFluidTank tank : fluidTanks) {
            if (tank.isEmpty()) {
                emptyTanks.add(tank);
            } else if (tank.isFluidEqual(stack)) {
                FluidStack remainder = tank.insert(toInsert, action, automationType);
                if (remainder.isEmpty()) {
                    //If we have no remaining fluid, return that we fit it all
                    return FluidStack.EMPTY;
                }
                //Update what we have left to insert, to be the amount we were unable to insert
                toInsert = remainder;
            }
        }
        for (IExtendedFluidTank tank : emptyTanks) {
            FluidStack remainder = tank.insert(toInsert, action, automationType);
            if (remainder.isEmpty()) {
                //If we have no remaining fluid, return that we fit it all
                return FluidStack.EMPTY;
            }
            //Update what we have left to insert, to be the amount we were unable to insert
            toInsert = remainder;
        }
        return toInsert;
    }

    /**
     * Util method for a generic extraction implementation for various handlers. Mainly for internal use only
     *
     * @since 10.5.13
     */
    public static FluidStack extract(int amount, @Nullable Direction side, Action action, ToIntFunction<@Nullable Direction> tankCount, InContainerGetter<FluidStack> inTankGetter,
          IntContainerInteraction<FluidStack> extractFluid) {
        if (amount == 0) {
            return FluidStack.EMPTY;
        }
        int tanks = tankCount.applyAsInt(side);
        if (tanks == 0) {
            return FluidStack.EMPTY;
        } else if (tanks == 1) {
            return extractFluid.interact(0, amount, side, action);
        }
        FluidStack extracted = FluidStack.EMPTY;
        int toDrain = amount;
        for (int tank = 0; tank < tanks; tank++) {
            if (extracted.isEmpty() || FluidStack.isSameFluidSameComponents(extracted, inTankGetter.getStored(tank, side))) {
                //If there is fluid in the tank that matches the type we have started draining, or we haven't found a type yet
                FluidStack drained = extractFluid.interact(tank, toDrain, side, action);
                if (!drained.isEmpty()) {
                    //If we were able to drain something, set it as the type we have extracted/increase how much we have extracted
                    if (extracted.isEmpty()) {
                        extracted = drained;
                    } else {
                        extracted.grow(drained.getAmount());
                    }
                    toDrain -= drained.getAmount();
                    if (toDrain == 0) {
                        //If we are done draining break and return the amount extracted
                        break;
                    }
                    //Otherwise, keep looking and attempt to drain more from the handler, making sure that it is of
                    // the same type as we have found
                }
            }
        }
        return extracted;
    }

    /**
     * Util method for a generic extraction implementation for various handlers. Mainly for internal use only
     *
     * @since 10.5.13
     */
    public static FluidStack extract(int amount, @Nullable Direction side, Function<@Nullable Direction, List<IExtendedFluidTank>> fluidTankSupplier,
          Action action, AutomationType automationType) {
        if (amount == 0) {
            return FluidStack.EMPTY;
        }
        List<IExtendedFluidTank> fluidTanks = fluidTankSupplier.apply(side);
        return extract(amount, action, automationType, fluidTanks.size(), fluidTanks);
    }

    /**
     * Util method for a generic extraction implementation for various handlers. Mainly for internal use only
     *
     * @since 10.6.0
     */
    public static FluidStack extract(int amount, Action action, AutomationType automationType, int size, List<IExtendedFluidTank> fluidTanks) {
        if (amount == 0 || size == 0) {
            return FluidStack.EMPTY;
        } else if (size == 1) {
            //noinspection SequencedCollectionMethodCanBeUsed: we know size
            return fluidTanks.get(0).extract(amount, action, automationType);
        }
        FluidStack extracted = FluidStack.EMPTY;
        int toDrain = amount;
        for (IExtendedFluidTank fluidTank : fluidTanks) {
            if (extracted.isEmpty() || fluidTank.isFluidEqual(extracted)) {
                //If there is fluid in the tank that matches the type we have started draining, or we haven't found a type yet
                FluidStack drained = fluidTank.extract(toDrain, action, automationType);
                if (!drained.isEmpty()) {
                    //If we were able to drain something, set it as the type we have extracted/increase how much we have extracted
                    if (extracted.isEmpty()) {
                        extracted = drained;
                    } else {
                        extracted.grow(drained.getAmount());
                    }
                    toDrain -= drained.getAmount();
                    if (toDrain == 0) {
                        //If we are done draining break and return the amount extracted
                        break;
                    }
                    //Otherwise, keep looking and attempt to drain more from the handler, making sure that it is of
                    // the same type as we have found
                }
            }
        }
        return extracted;
    }

    /**
     * Util method for a generic extraction implementation for various handlers. Mainly for internal use only
     *
     * @since 10.5.13
     */
    public static FluidStack extract(FluidStack stack, @Nullable Direction side, Action action, ToIntFunction<@Nullable Direction> tankCount, InContainerGetter<FluidStack> inTankGetter,
          IntContainerInteraction<FluidStack> extractFluid) {
        if (stack.isEmpty()) {
            return FluidStack.EMPTY;
        }
        int tanks = tankCount.applyAsInt(side);
        if (tanks == 0) {
            return FluidStack.EMPTY;
        } else if (tanks == 1) {
            FluidStack inTank = inTankGetter.getStored(0, side);
            if (inTank.isEmpty() || !FluidStack.isSameFluidSameComponents(inTank, stack)) {
                return FluidStack.EMPTY;
            }
            return extractFluid.interact(0, stack.getAmount(), side, action);
        }
        FluidStack extracted = FluidStack.EMPTY;
        int toDrain = stack.getAmount();
        for (int tank = 0; tank < tanks; tank++) {
            if (FluidStack.isSameFluidSameComponents(stack, inTankGetter.getStored(tank, side))) {
                //If there is fluid in the tank that matches the type we are trying to drain, try to drain from it
                FluidStack drained = extractFluid.interact(tank, toDrain, side, action);
                if (!drained.isEmpty()) {
                    //If we were able to drain something, set it as the type we have extracted/increase how much we have extracted
                    if (extracted.isEmpty()) {
                        extracted = drained;
                    } else {
                        extracted.grow(drained.getAmount());
                    }
                    toDrain -= drained.getAmount();
                    if (toDrain == 0) {
                        //If we are done draining break and return the amount extracted
                        break;
                    }
                    //Otherwise, keep looking and attempt to drain more from the handler
                }
            }
        }
        return extracted;
    }

    /**
     * Util method for a generic extraction implementation for various handlers. Mainly for internal use only
     *
     * @since 10.5.13
     */
    public static FluidStack extract(FluidStack stack, @Nullable Direction side, Function<@Nullable Direction, List<IExtendedFluidTank>> fluidTankSupplier,
          Action action, AutomationType automationType) {
        if (stack.isEmpty()) {
            return FluidStack.EMPTY;
        }
        List<IExtendedFluidTank> fluidTanks = fluidTankSupplier.apply(side);
        return extract(stack, action, automationType, fluidTanks.size(), fluidTanks);
    }

    /**
     * Util method for a generic extraction implementation for various handlers. Mainly for internal use only
     *
     * @since 10.6.0
     */
    public static FluidStack extract(FluidStack stack, Action action, AutomationType automationType, int size, List<IExtendedFluidTank> fluidTanks) {
        if (stack.isEmpty() || size == 0) {
            return FluidStack.EMPTY;
        } else if (size == 1) {
            //noinspection SequencedCollectionMethodCanBeUsed: we know size
            IExtendedFluidTank tank = fluidTanks.get(0);
            if (tank.isEmpty() || !tank.isFluidEqual(stack)) {
                return FluidStack.EMPTY;
            }
            return tank.extract(stack.getAmount(), action, automationType);
        }
        FluidStack extracted = FluidStack.EMPTY;
        int toDrain = stack.getAmount();
        for (IExtendedFluidTank fluidTank : fluidTanks) {
            if (fluidTank.isFluidEqual(stack)) {
                //If there is fluid in the tank that matches the type we are trying to drain, try to drain from it
                FluidStack drained = fluidTank.extract(toDrain, action, automationType);
                if (!drained.isEmpty()) {
                    //If we were able to drain something, set it as the type we have extracted/increase how much we have extracted
                    if (extracted.isEmpty()) {
                        extracted = drained;
                    } else {
                        extracted.grow(drained.getAmount());
                    }
                    toDrain -= drained.getAmount();
                    if (toDrain == 0) {
                        //If we are done draining break and return the amount extracted
                        break;
                    }
                    //Otherwise, keep looking and attempt to drain more from the handler
                }
            }
        }
        return extracted;
    }
}