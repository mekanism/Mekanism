package mekanism.api.chemical;

import java.util.ArrayList;
import java.util.List;
import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.annotations.NothingNullByDefault;

@NothingNullByDefault
public class ChemicalUtils {

    private ChemicalUtils() {
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
                        extracted.grow(drained.amount());
                    }
                    toDrain -= drained.amount();
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
            return tank.extract(stack.amount(), action, automationType);
        }
        ChemicalStack extracted = ChemicalStack.EMPTY;
        long toDrain = stack.amount();
        for (IChemicalTank tank : chemicalTanks) {
            if (tank.isTypeEqual(stack)) {
                //If there is chemical in the tank that matches the type we are trying to drain, try to drain from it
                ChemicalStack drained = tank.extract(toDrain, action, automationType);
                if (!drained.isEmpty()) {
                    //If we were able to drain something, set it as the type we have extracted/increase how much we have extracted
                    if (extracted.isEmpty()) {
                        extracted = drained;
                    } else {
                        extracted.grow(drained.amount());
                    }
                    toDrain -= drained.amount();
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