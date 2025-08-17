package mekanism.api.math;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.ToIntFunction;
import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.container.InContainerGetterLong;
import mekanism.api.container.LongToLongContainerInteraction;
import mekanism.api.energy.IEnergyContainer;
import net.minecraft.core.Direction;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public class LongTransferUtils {

    private LongTransferUtils() {
    }

    /**
     * Util method for a generic insert implementation for various handlers. Mainly for internal use only
     *
     * @since 10.5.13
     */
    public static long insert(long stack, @Nullable Direction side, Action action, ToIntFunction<@Nullable Direction> containerCount,
          InContainerGetterLong inContainerGetter, LongToLongContainerInteraction insert) {
        if (stack <= 0L) {
            //Short circuit if no energy is trying to be inserted
            return 0L;
        }
        int containers = containerCount.applyAsInt(side);
        if (containers == 0) {
            return stack;
        } else if (containers == 1) {
            return insert.interact(0, stack, side, action);
        }
        long toInsert = stack;
        //Start by trying to insert into the containers that are not empty
        IntList emptyContainers = new IntArrayList();
        for (int container = 0; container < containers; container++) {
            long inContainer = inContainerGetter.getStored(container, side);
            if (inContainer == 0L) {
                emptyContainers.add(container);
            } else {
                long remainder = insert.interact(container, toInsert, side, action);
                if (remainder <= 0L) {
                    //If we have no remainder, return that we fit it all
                    return 0L;
                }
                //Update what we have left to insert, to be the amount we were unable to insert
                toInsert = remainder;
            }
        }
        for (int container : emptyContainers) {
            long remainder = insert.interact(container, toInsert, side, action);
            if (remainder <= 0L) {
                //If we have no remainder, return that we fit it all
                return 0L;
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
    public static long insert(long stack, @Nullable Direction side, Function<@Nullable Direction, List<IEnergyContainer>> energyContainerSupplier,
          Action action, AutomationType automationType) {
        if (stack <= 0L) {
            //Short circuit if no energy is trying to be inserted
            return 0L;
        }
        List<IEnergyContainer> energyContainers = energyContainerSupplier.apply(side);
        return insert(stack, action, automationType, energyContainers.size(), energyContainers);
    }

    /**
     * Util method for a generic insert implementation for various handlers. Mainly for internal use only
     *
     * @since 10.6.0
     */
    public static long insert(long stack, Action action, AutomationType automationType, int containerCount, List<IEnergyContainer> energyContainers) {
        if (stack <= 0L) {
            //Short circuit if no energy is trying to be inserted
            return 0L;
        } else if (containerCount == 0) {
            return stack;
        } else if (containerCount == 1) {
            //noinspection SequencedCollectionMethodCanBeUsed: we know size
            return energyContainers.get(0).insert(stack, action, automationType);
        }
        long toInsert = stack;
        //Start by trying to insert into the containers that are not empty
        List<IEnergyContainer> emptyContainers = new ArrayList<>();
        for (IEnergyContainer energyContainer : energyContainers) {
            long inContainer = energyContainer.getEnergy();
            if (inContainer == 0L) {
                emptyContainers.add(energyContainer);
            } else {
                long remainder = energyContainer.insert(toInsert, action, automationType);
                //noinspection NonStrictComparisonCanBeEquality - Prevent against bad implementations of insert
                if (remainder <= 0L) {
                    //If we have no remainder, return that we fit it all
                    return 0L;
                }
                //Update what we have left to insert, to be the amount we were unable to insert
                toInsert = remainder;
            }
        }
        for (IEnergyContainer container : emptyContainers) {
            long remainder = container.insert(toInsert, action, automationType);
            //noinspection NonStrictComparisonCanBeEquality - Prevent against bad implementations of insert
            if (remainder <= 0L) {
                //If we have no remainder, return that we fit it all
                return 0L;
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
    public static long extract(long amount, @Nullable Direction side, Action action, ToIntFunction<@Nullable Direction> containerCount,
          LongToLongContainerInteraction extract) {
        if (amount <= 0L) {
            //Short circuit if no energy is trying to be extracted
            return 0L;
        }
        int containers = containerCount.applyAsInt(side);
        if (containers == 0) {
            return 0L;
        } else if (containers == 1) {
            return extract.interact(0, amount, side, action);
        }
        long extracted = 0;
        long toExtract = amount;
        for (int container = 0; container < containers; container++) {
            long drained = extract.interact(container, toExtract, side, action);
            if (drained > 0L) {
                //If we were able to extract something, do so
                if (extracted == 0L) {
                    extracted = drained;
                } else {
                    extracted += drained;
                }
                toExtract -= drained;
                if (toExtract == 0L) {
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
    public static long extract(long amount, @Nullable Direction side, Function<@Nullable Direction, List<IEnergyContainer>> energyContainerSupplier,
          Action action, AutomationType automationType) {
        if (amount == 0L) {
            //Short circuit if no energy is trying to be extracted
            return 0L;
        }
        List<IEnergyContainer> energyContainers = energyContainerSupplier.apply(side);
        return extract(amount, action, automationType, energyContainers.size(), energyContainers);
    }

    /**
     * Util method for a generic extraction implementation for various handlers. Mainly for internal use only
     *
     * @since 10.6.0
     */
    public static long extract(long amount, Action action, AutomationType automationType, int containerCount, List<IEnergyContainer> energyContainers) {
        if (amount <= 0L || containerCount == 0) {
            //Short circuit if no energy is trying to be extracted
            return 0L;
        } else if (containerCount == 1) {
            //noinspection SequencedCollectionMethodCanBeUsed: we know size
            return energyContainers.get(0).extract(amount, action, automationType);
        }
        long extracted = 0;
        long toExtract = amount;
        for (IEnergyContainer energyContainer : energyContainers) {
            long drained = energyContainer.extract(toExtract, action, automationType);
            if (drained > 0L) {
                //If we were able to extract something, do so
                if (extracted == 0L) {
                    extracted = drained;
                } else {
                    extracted += drained;
                }
                toExtract -= drained;
                if (toExtract == 0L) {
                    //If we are done extracting break and return the amount extracted
                    break;
                }
                //Otherwise, keep looking and attempt to extract more from the handler
            }
        }
        return extracted;
    }
}