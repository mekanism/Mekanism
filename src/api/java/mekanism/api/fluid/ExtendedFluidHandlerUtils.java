package mekanism.api.fluid;

import it.unimi.dsi.fastutil.ints.Int2ObjectFunction;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import java.util.function.IntSupplier;
import mekanism.api.Action;
import mekanism.api.annotations.NonNull;
import net.minecraftforge.fluids.FluidStack;

public class ExtendedFluidHandlerUtils {

    /**
     * Util method for a generic insert implementation for various handlers. Mainly for internal use only
     */
    public static FluidStack insert(FluidStack stack, Action action, IntSupplier tankCount, Int2ObjectFunction<@NonNull FluidStack> inTankGetter, InsertFluid insertFluid) {
        int tanks = tankCount.getAsInt();
        if (tanks == 1) {
            return insertFluid.insert(0, stack, action);
        }
        IntList matchingTanks = new IntArrayList();
        IntList emptyTanks = new IntArrayList();
        for (int tank = 0; tank < tanks; tank++) {
            FluidStack inTank = inTankGetter.get(tank);
            if (inTank.isEmpty()) {
                emptyTanks.add(tank);
            } else if (inTank.isFluidEqual(stack)) {
                matchingTanks.add(tank);
            }
        }
        FluidStack toInsert = stack;
        //Start by trying to insert into the tanks that have the same type
        for (int tank : matchingTanks) {
            FluidStack remainder = insertFluid.insert(tank, toInsert, action);
            if (remainder.isEmpty()) {
                //If we have no remaining fluid, return that we fit it all
                return FluidStack.EMPTY;
            }
            //Update what we have left to insert, to be the amount we were unable to insert
            toInsert = remainder;
        }
        for (int tank : emptyTanks) {
            FluidStack remainder = insertFluid.insert(tank, toInsert, action);
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
     */
    public static FluidStack extract(int amount, Action action, IntSupplier tankCount, Int2ObjectFunction<@NonNull FluidStack> inTankGetter, ExtractFluid extractFluid) {
        int tanks = tankCount.getAsInt();
        if (tanks == 1) {
            return extractFluid.extract(0, amount, action);
        }
        FluidStack extracted = FluidStack.EMPTY;
        int toDrain = amount;
        for (int tank = 0; tank < tanks; tank++) {
            if (extracted.isEmpty() || extracted.isFluidEqual(inTankGetter.get(tank))) {
                //If there is fluid in the tank that matches the type we have started draining, or we haven't found a type yet
                FluidStack drained = extractFluid.extract(tank, toDrain, action);
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
                    //Otherwise keep looking and attempt to drain more from the handler, making sure that it is of
                    // the same type as we have found
                }
            }
        }
        return extracted;
    }

    /**
     * Util method for a generic extraction implementation for various handlers. Mainly for internal use only
     */
    public static FluidStack extract(FluidStack stack, Action action, IntSupplier tankCount, Int2ObjectFunction<@NonNull FluidStack> inTankGetter, ExtractFluid extractFluid) {
        int tanks = tankCount.getAsInt();
        if (tanks == 1) {
            FluidStack inTank = inTankGetter.get(0);
            if (inTank.isEmpty() || !inTank.isFluidEqual(stack)) {
                return FluidStack.EMPTY;
            }
            return extractFluid.extract(0, stack.getAmount(), action);
        }
        FluidStack extracted = FluidStack.EMPTY;
        int toDrain = stack.getAmount();
        for (int tank = 0; tank < tanks; tank++) {
            if (stack.isFluidEqual(inTankGetter.get(tank))) {
                //If there is fluid in the tank that matches the type we are trying to drain, try to draining from it
                FluidStack drained = extractFluid.extract(tank, toDrain, action);
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
                    //Otherwise keep looking and attempt to drain more from the handler
                }
            }
        }
        return extracted;
    }

    @FunctionalInterface
    public interface InsertFluid {

        FluidStack insert(int tank, FluidStack stack, Action action);
    }

    @FunctionalInterface
    public interface ExtractFluid {

        FluidStack extract(int tank, int amount, Action action);
    }
}