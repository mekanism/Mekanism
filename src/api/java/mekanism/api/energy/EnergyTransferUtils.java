package mekanism.api.energy;

import it.unimi.dsi.fastutil.ints.Int2DoubleFunction;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import java.util.function.IntSupplier;
import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.api.Action;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class EnergyTransferUtils {//TODO: Rename this and use it by the heat handlers

    /**
     * Util method for a generic insert implementation for various handlers. Mainly for internal use only
     */
    public static double insert(double stack, Action action, IntSupplier containerCount, Int2DoubleFunction inContainerGetter, InsertDouble insertEnergy) {
        int containers = containerCount.getAsInt();
        if (containers == 1) {
            return insertEnergy.insert(0, stack, action);
        }
        IntList matchingContainers = new IntArrayList();
        IntList emptyContainers = new IntArrayList();
        for (int container = 0; container < containers; container++) {
            double inContainer = inContainerGetter.applyAsDouble(container);
            if (inContainer == 0) {
                emptyContainers.add(container);
            } else {
                matchingContainers.add(container);
            }
        }
        double toInsert = stack;
        //Start by trying to insert into the tanks that have the same type
        for (int container : matchingContainers) {
            double remainder = insertEnergy.insert(container, toInsert, action);
            if (remainder == 0) {
                //If we have no remaining energy, return that we fit it all
                return 0;
            }
            //Update what we have left to insert, to be the amount we were unable to insert
            toInsert = remainder;
        }
        for (int tank : emptyContainers) {
            double remainder = insertEnergy.insert(tank, toInsert, action);
            if (remainder == 0) {
                //If we have no remaining energy, return that we fit it all
                return 0;
            }
            //Update what we have left to insert, to be the amount we were unable to insert
            toInsert = remainder;
        }
        return toInsert;
    }

    /**
     * Util method for a generic extraction implementation for various handlers. Mainly for internal use only
     */
    public static double extract(double amount, Action action, IntSupplier containerCount, ExtractDouble extractEnergy) {
        int containers = containerCount.getAsInt();
        if (containers == 1) {
            return extractEnergy.extract(0, amount, action);
        }
        double extracted = 0;
        double toExtract = amount;
        for (int container = 0; container < containers; container++) {
            double drained = extractEnergy.extract(container, toExtract, action);
            if (drained > 0) {
                //If we were able to extract something, do so
                extracted += drained;
                toExtract -= drained;
                if (toExtract == 0) {
                    //If we are done extracting break and return the amount extracted
                    break;
                }
                //Otherwise keep looking and attempt to extract more from the handler
            }
        }
        return extracted;
    }

    @FunctionalInterface
    public interface InsertDouble {

        double insert(int container, double amount, Action action);
    }

    @FunctionalInterface
    public interface ExtractDouble {

        double extract(int container, double amount, Action action);
    }
}