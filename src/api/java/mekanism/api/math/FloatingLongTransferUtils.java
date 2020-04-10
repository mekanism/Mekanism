package mekanism.api.math;

import it.unimi.dsi.fastutil.ints.Int2ObjectFunction;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import java.util.function.IntSupplier;
import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.api.Action;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class FloatingLongTransferUtils {

    /**
     * Util method for a generic insert implementation for various handlers. Mainly for internal use only
     */
    public static FloatingLong insert(FloatingLong stack, Action action, IntSupplier containerCount, Int2ObjectFunction<FloatingLong> inContainerGetter, InsertFloatingLong insert) {
        int containers = containerCount.getAsInt();
        if (containers == 1) {
            return insert.insert(0, stack, action);
        }
        IntList matchingContainers = new IntArrayList();
        IntList emptyContainers = new IntArrayList();
        for (int container = 0; container < containers; container++) {
            FloatingLong inContainer = inContainerGetter.apply(container);
            if (inContainer.isZero()) {
                emptyContainers.add(container);
            } else {
                matchingContainers.add(container);
            }
        }
        FloatingLong toInsert = stack;
        //Start by trying to insert into the tanks that have the same type
        for (int container : matchingContainers) {
            FloatingLong remainder = insert.insert(container, toInsert, action);
            if (remainder.isZero()) {
                //If we have no remainder, return that we fit it all
                return FloatingLong.ZERO;
            }
            //Update what we have left to insert, to be the amount we were unable to insert
            toInsert = remainder;
        }
        for (int container : emptyContainers) {
            FloatingLong remainder = insert.insert(container, toInsert, action);
            if (remainder.isZero()) {
                //If we have no remainder, return that we fit it all
                return FloatingLong.ZERO;
            }
            //Update what we have left to insert, to be the amount we were unable to insert
            toInsert = remainder;
        }
        return toInsert;
    }

    /**
     * Util method for a generic extraction implementation for various handlers. Mainly for internal use only
     */
    public static FloatingLong extract(FloatingLong amount, Action action, IntSupplier containerCount, ExtractFloatingLong extract) {
        int containers = containerCount.getAsInt();
        if (containers == 1) {
            return extract.extract(0, amount, action);
        }
        FloatingLong extracted = FloatingLong.ZERO;
        FloatingLong toExtract = amount.copy();
        for (int container = 0; container < containers; container++) {
            FloatingLong drained = extract.extract(container, toExtract, action);
            if (!drained.isZero()) {
                //If we were able to extract something, do so
                if (extracted.isZero()) {
                    extracted = drained;
                } else {
                    extracted = extracted.plusEqual(drained);
                }
                toExtract = toExtract.minusEqual(drained);
                if (toExtract.isZero()) {
                    //If we are done extracting break and return the amount extracted
                    break;
                }
                //Otherwise keep looking and attempt to extract more from the handler
            }
        }
        return extracted;
    }

    @FunctionalInterface
    public interface InsertFloatingLong {

        FloatingLong insert(int container, FloatingLong amount, Action action);
    }

    @FunctionalInterface
    public interface ExtractFloatingLong {

        FloatingLong extract(int container, FloatingLong amount, Action action);
    }
}