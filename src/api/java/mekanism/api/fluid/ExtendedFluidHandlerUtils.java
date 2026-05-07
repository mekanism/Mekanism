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
import net.minecraft.core.Direction;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.Nullable;

public class ExtendedFluidHandlerUtils {//TODO - 26.1: Remove this

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
    public static FluidStack insert(FluidStack stack, @Nullable Direction side, Function<@Nullable Direction, List<IFluidTank>> fluidTankSupplier,
          Action action, AutomationType automationType) {
        if (stack.isEmpty()) {
            //Short circuit if nothing is actually being inserted
            return FluidStack.EMPTY;
        }
        List<IFluidTank> fluidTanks = fluidTankSupplier.apply(side);
        return insert(stack, action, automationType, fluidTanks.size(), fluidTanks);
    }

    /**
     * Util method for a generic insert implementation for various handlers. Mainly for internal use only
     *
     * @since 10.6.0
     */
    public static FluidStack insert(FluidStack stack, Action action, AutomationType automationType, int size, List<IFluidTank> fluidTanks) {
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
        List<IFluidTank> emptyTanks = new ArrayList<>();
        for (IFluidTank tank : fluidTanks) {
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
        for (IFluidTank tank : emptyTanks) {
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
}