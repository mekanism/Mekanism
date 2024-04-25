package mekanism.api.math;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.IntSupplier;
import java.util.function.ToIntFunction;
import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.container.ContainerInteraction;
import mekanism.api.container.InContainerGetter;
import mekanism.api.energy.IEnergyContainer;
import net.minecraft.core.Direction;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public class FloatingLongTransferUtils {

    private FloatingLongTransferUtils() {
    }

    /**
     * Util method for a generic insert implementation for various handlers. Mainly for internal use only
     *
     * @deprecated Please use {@link #insert(FloatingLong, Direction, Action, ToIntFunction, InContainerGetter, ContainerInteraction)} to avoid capturing lambdas.
     */
    @Deprecated(forRemoval = true, since = "10.5.13")
    public static FloatingLong insert(FloatingLong stack, Action action, IntSupplier containerCount, IntFunction<FloatingLong> inContainerGetter,
          InsertFloatingLong insert) {
        if (stack.isZero()) {
            //Short circuit if nothing is actually being inserted
            return FloatingLong.ZERO;
        }
        return insert(stack, null, action, side -> containerCount.getAsInt(), (container, s) -> inContainerGetter.apply(container),
              (container, amt, side, a) -> insert.insert(container, amt, a));
    }

    /**
     * Util method for a generic insert implementation for various handlers. Mainly for internal use only
     *
     * @since 10.5.13
     */
    public static FloatingLong insert(FloatingLong stack, @Nullable Direction side, Action action, ToIntFunction<@Nullable Direction> containerCount,
          InContainerGetter<FloatingLong> inContainerGetter, ContainerInteraction<FloatingLong> insert) {
        if (stack.isZero()) {
            //Short circuit if no energy is trying to be inserted
            return FloatingLong.ZERO;
        }
        int containers = containerCount.applyAsInt(side);
        if (containers == 0) {
            return stack;
        } else if (containers == 1) {
            return insert.interact(0, stack, side, action);
        }
        FloatingLong toInsert = stack;
        //Start by trying to insert into the containers that are not empty
        IntList emptyContainers = new IntArrayList();
        for (int container = 0; container < containers; container++) {
            FloatingLong inContainer = inContainerGetter.getStored(container, side);
            if (inContainer.isZero()) {
                emptyContainers.add(container);
            } else {
                FloatingLong remainder = insert.interact(container, toInsert, side, action);
                if (remainder.isZero()) {
                    //If we have no remainder, return that we fit it all
                    return FloatingLong.ZERO;
                }
                //Update what we have left to insert, to be the amount we were unable to insert
                toInsert = remainder;
            }
        }
        for (int container : emptyContainers) {
            FloatingLong remainder = insert.interact(container, toInsert, side, action);
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
     * Util method for a generic insert implementation for various handlers. Mainly for internal use only
     *
     * @since 10.5.13
     */
    public static FloatingLong insert(FloatingLong stack, @Nullable Direction side, Function<@Nullable Direction, List<IEnergyContainer>> energyContainerSupplier,
          Action action, AutomationType automationType) {
        if (stack.isZero()) {
            //Short circuit if no energy is trying to be inserted
            return FloatingLong.ZERO;
        }
        List<IEnergyContainer> energyContainers = energyContainerSupplier.apply(side);
        if (energyContainers.isEmpty()) {
            return stack;
        } else if (energyContainers.size() == 1) {
            return energyContainers.get(0).insert(stack, action, automationType);
        }
        FloatingLong toInsert = stack;
        //Start by trying to insert into the containers that are not empty
        List<IEnergyContainer> emptyContainers = new ArrayList<>();
        for (IEnergyContainer energyContainer : energyContainers) {
            FloatingLong inContainer = energyContainer.getEnergy();
            if (inContainer.isZero()) {
                emptyContainers.add(energyContainer);
            } else {
                FloatingLong remainder = energyContainer.insert(toInsert, action, automationType);
                if (remainder.isZero()) {
                    //If we have no remainder, return that we fit it all
                    return FloatingLong.ZERO;
                }
                //Update what we have left to insert, to be the amount we were unable to insert
                toInsert = remainder;
            }
        }
        for (IEnergyContainer container : emptyContainers) {
            FloatingLong remainder = container.insert(toInsert, action, automationType);
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
     * 
     * @deprecated Please use {@link #extract(FloatingLong, Direction, Action, ToIntFunction, ContainerInteraction)} to avoid capturing lambdas.
     */
    @Deprecated(forRemoval = true, since = "10.5.13")
    public static FloatingLong extract(FloatingLong amount, Action action, IntSupplier containerCount, ExtractFloatingLong extract) {
        if (amount.isZero()) {
            //Short circuit if no energy is trying to be extracted
            return FloatingLong.ZERO;
        }
        return extract(amount, null, action, side -> containerCount.getAsInt(), (container, amt, side, a) -> extract.extract(container, amt, a));
    }

    /**
     * Util method for a generic extraction implementation for various handlers. Mainly for internal use only
     *
     * @since 10.5.13
     */
    public static FloatingLong extract(FloatingLong amount, @Nullable Direction side, Action action, ToIntFunction<@Nullable Direction> containerCount,
          ContainerInteraction<FloatingLong> extract) {
        if (amount.isZero()) {
            //Short circuit if no energy is trying to be extracted
            return FloatingLong.ZERO;
        }
        int containers = containerCount.applyAsInt(side);
        if (containers == 0) {
            return FloatingLong.ZERO;
        } else if (containers == 1) {
            return extract.interact(0, amount, side, action);
        }
        FloatingLong extracted = FloatingLong.ZERO;
        FloatingLong toExtract = amount.copy();
        for (int container = 0; container < containers; container++) {
            FloatingLong drained = extract.interact(container, toExtract, side, action);
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
                //Otherwise, keep looking and attempt to extract more from the handler
            }
        }
        return extracted;
    }

    /**
     * Util method for a generic extraction implementation for various handlers. Mainly for internal use only
     *
     * @since 10.5.13
     */
    public static FloatingLong extract(FloatingLong amount, @Nullable Direction side, Function<@Nullable Direction, List<IEnergyContainer>> energyContainerSupplier,
          Action action, AutomationType automationType) {
        if (amount.isZero()) {
            //Short circuit if no energy is trying to be extracted
            return FloatingLong.ZERO;
        }
        List<IEnergyContainer> energyContainers = energyContainerSupplier.apply(side);
        if (energyContainers.isEmpty()) {
            return FloatingLong.ZERO;
        } else if (energyContainers.size() == 1) {
            return energyContainers.get(0).extract(amount, action, automationType);
        }
        FloatingLong extracted = FloatingLong.ZERO;
        FloatingLong toExtract = amount.copy();
        for (IEnergyContainer energyContainer : energyContainers) {
            FloatingLong drained = energyContainer.extract(toExtract, action, automationType);
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
                //Otherwise, keep looking and attempt to extract more from the handler
            }
        }
        return extracted;
    }

    /**
     * @deprecated See {@link mekanism.api.container.ContainerInteraction}
     */
    @FunctionalInterface
    @Deprecated(forRemoval = true, since = "10.5.13")
    public interface InsertFloatingLong {

        FloatingLong insert(int container, FloatingLong amount, Action action);
    }

    /**
     * @deprecated See {@link mekanism.api.container.ContainerInteraction}
     */
    @FunctionalInterface
    @Deprecated(forRemoval = true, since = "10.5.13")
    public interface ExtractFloatingLong {

        FloatingLong extract(int container, FloatingLong amount, Action action);
    }
}