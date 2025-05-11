package mekanism.api.chemical;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.ToIntFunction;
import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.container.ContainerInteraction;
import mekanism.api.container.InContainerGetter;
import mekanism.api.container.LongContainerInteraction;
import net.minecraft.core.Direction;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public class ChemicalUtils {

    private ChemicalUtils() {
    }

    /**
     * Util method for a generic insert implementation for various handlers. Mainly for internal use only
     *
     * @since 10.5.13
     */
    public static ChemicalStack insert(ChemicalStack stack, @Nullable Direction side, Action action, ToIntFunction<@Nullable Direction> tankCount,
          InContainerGetter<ChemicalStack> inTankGetter, ContainerInteraction<ChemicalStack> insertChemical) {
        if (stack.isEmpty()) {
            //Short circuit if nothing is actually being inserted
            return ChemicalStack.EMPTY;
        }
        int tanks = tankCount.applyAsInt(side);
        if (tanks == 0) {
            return stack;
        } else if (tanks == 1) {
            return insertChemical.interact(0, stack, side, action);
        }
        ChemicalStack toInsert = stack;
        //Start by trying to insert into the tanks that have the same type
        IntList emptyTanks = new IntArrayList();
        for (int tank = 0; tank < tanks; tank++) {
            ChemicalStack inTank = inTankGetter.getStored(tank, side);
            if (inTank.isEmpty()) {
                emptyTanks.add(tank);
            } else if (ChemicalStack.isSameChemical(inTank, stack)) {
                ChemicalStack remainder = insertChemical.interact(tank, toInsert, side, action);
                if (remainder.isEmpty()) {
                    //If we have no remaining chemical, return that we fit it all
                    return ChemicalStack.EMPTY;
                }
                //Update what we have left to insert, to be the amount we were unable to insert
                toInsert = remainder;
            }
        }
        for (int tank : emptyTanks) {
            ChemicalStack remainder = insertChemical.interact(tank, toInsert, side, action);
            if (remainder.isEmpty()) {
                //If we have no remaining chemical, return that we fit it all
                return ChemicalStack.EMPTY;
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
    public static ChemicalStack insert(ChemicalStack stack, @Nullable Direction side, Function<@Nullable Direction, List<IChemicalTank>> tankSupplier, Action action,
          AutomationType automationType) {
        if (stack.isEmpty()) {
            //Short circuit if nothing is actually being inserted
            return ChemicalStack.EMPTY;
        }
        List<IChemicalTank> chemicalTanks = tankSupplier.apply(side);
        return insert(stack, action, automationType, chemicalTanks.size(), chemicalTanks);
    }

    /**
     * Util method for a generic insert implementation for various handlers. Mainly for internal use only
     *
     * @since 10.6.0
     */
    public static ChemicalStack insert(ChemicalStack stack, Action action, AutomationType automationType, int size, List<IChemicalTank> chemicalTanks) {
        if (stack.isEmpty()) {
            //Short circuit if nothing is actually being inserted
            return ChemicalStack.EMPTY;
        } else if (size == 0) {
            return stack;
        } else if (size == 1) {
            //noinspection SequencedCollectionMethodCanBeUsed: we know size
            return chemicalTanks.get(0).insert(stack, action, automationType);
        }
        ChemicalStack toInsert = stack;
        //Start by trying to insert into the tanks that have the same type
        List<IChemicalTank> emptyTanks = new ArrayList<>();
        for (IChemicalTank tank : chemicalTanks) {
            if (tank.isEmpty()) {
                emptyTanks.add(tank);
            } else if (tank.isTypeEqual(stack)) {
                ChemicalStack remainder = tank.insert(toInsert, action, automationType);
                if (remainder.isEmpty()) {
                    //If we have no remaining chemical, return that we fit it all
                    return ChemicalStack.EMPTY;
                }
                //Update what we have left to insert, to be the amount we were unable to insert
                toInsert = remainder;
            }
        }
        for (IChemicalTank tank : emptyTanks) {
            ChemicalStack remainder = tank.insert(toInsert, action, automationType);
            if (remainder.isEmpty()) {
                //If we have no remaining chemical, return that we fit it all
                return ChemicalStack.EMPTY;
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
    public static ChemicalStack extract(long amount, @Nullable Direction side, Action action, ToIntFunction<@Nullable Direction> tankCount,
          InContainerGetter<ChemicalStack> inTankGetter, LongContainerInteraction<ChemicalStack> extractChemical) {
        if (amount == 0) {
            return ChemicalStack.EMPTY;
        }
        int tanks = tankCount.applyAsInt(side);
        if (tanks == 0) {
            return ChemicalStack.EMPTY;
        } else if (tanks == 1) {
            return extractChemical.interact(0, amount, side, action);
        }
        ChemicalStack extracted = ChemicalStack.EMPTY;
        long toDrain = amount;
        for (int tank = 0; tank < tanks; tank++) {
            if (extracted.isEmpty() || ChemicalStack.isSameChemical(extracted, inTankGetter.getStored(tank, side))) {
                //If there is chemical in the tank that matches the type we have started draining, or we haven't found a type yet
                ChemicalStack drained = extractChemical.interact(tank, toDrain, side, action);
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
    public static ChemicalStack extract(long amount, @Nullable Direction side, Function<@Nullable Direction, List<IChemicalTank>> tankSupplier, Action action,
          AutomationType automationType) {
        if (amount == 0) {
            return ChemicalStack.EMPTY;
        }
        List<IChemicalTank> chemicalTanks = tankSupplier.apply(side);
        return extract(amount, action, automationType, chemicalTanks.size(), chemicalTanks);
    }

    /**
     * Util method for a generic extraction implementation for various handlers. Mainly for internal use only
     *
     * @since 10.6.0
     */
    public static ChemicalStack extract(long amount, Action action, AutomationType automationType, int size, List<IChemicalTank> chemicalTanks) {
        if (amount == 0 || size == 0) {
            return ChemicalStack.EMPTY;
        } else if (size == 1) {
            //noinspection SequencedCollectionMethodCanBeUsed: we know size
            return chemicalTanks.get(0).extract(amount, action, automationType);
        }
        ChemicalStack extracted = ChemicalStack.EMPTY;
        long toDrain = amount;
        for (IChemicalTank tank : chemicalTanks) {
            if (extracted.isEmpty() || tank.isTypeEqual(extracted)) {
                //If there is chemical in the tank that matches the type we have started draining, or we haven't found a type yet
                ChemicalStack drained = tank.extract(toDrain, action, automationType);
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
    public static ChemicalStack extract(ChemicalStack stack, @Nullable Direction side, Action action, ToIntFunction<@Nullable Direction> tankCount,
          InContainerGetter<ChemicalStack> inTankGetter, LongContainerInteraction<ChemicalStack> extractChemical) {
        if (stack.isEmpty()) {
            return ChemicalStack.EMPTY;
        }
        int tanks = tankCount.applyAsInt(side);
        if (tanks == 0) {
            return ChemicalStack.EMPTY;
        } else if (tanks == 1) {
            ChemicalStack inTank = inTankGetter.getStored(0, side);
            if (inTank.isEmpty() || !ChemicalStack.isSameChemical(inTank, stack)) {
                return ChemicalStack.EMPTY;
            }
            return extractChemical.interact(0, stack.getAmount(), side, action);
        }
        ChemicalStack extracted = ChemicalStack.EMPTY;
        long toDrain = stack.getAmount();
        for (int tank = 0; tank < tanks; tank++) {
            if (ChemicalStack.isSameChemical(stack, inTankGetter.getStored(tank, side))) {
                //If there is chemical in the tank that matches the type we are trying to drain, try to drain from it
                ChemicalStack drained = extractChemical.interact(tank, toDrain, side, action);
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
    public static ChemicalStack extract(ChemicalStack stack, @Nullable Direction side, Function<@Nullable Direction, List<IChemicalTank>> tankSupplier, Action action,
          AutomationType automationType) {
        if (stack.isEmpty()) {
            return ChemicalStack.EMPTY;
        }
        List<IChemicalTank> chemicalTanks = tankSupplier.apply(side);
        return extract(stack, action, automationType, chemicalTanks.size(), chemicalTanks);
    }

    /**
     * Util method for a generic extraction implementation for various handlers. Mainly for internal use only
     *
     * @since 10.6.0
     */
    public static ChemicalStack extract(ChemicalStack stack, Action action, AutomationType automationType, int size, Iterable<IChemicalTank> chemicalTanks) {
        if (stack.isEmpty() || size == 0) {
            return ChemicalStack.EMPTY;
        } else if (size == 1) {
            IChemicalTank tank = chemicalTanks.iterator().next();
            if (tank.isEmpty() || !tank.isTypeEqual(stack)) {
                return ChemicalStack.EMPTY;
            }
            return tank.extract(stack.getAmount(), action, automationType);
        }
        ChemicalStack extracted = ChemicalStack.EMPTY;
        long toDrain = stack.getAmount();
        for (IChemicalTank tank : chemicalTanks) {
            if (tank.isTypeEqual(stack)) {
                //If there is chemical in the tank that matches the type we are trying to drain, try to drain from it
                ChemicalStack drained = tank.extract(toDrain, action, automationType);
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