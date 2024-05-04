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
    public static <CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>> STACK insert(STACK stack, @Nullable Direction side, Action action,
          STACK empty, ToIntFunction<@Nullable Direction> tankCount, InContainerGetter<STACK> inTankGetter, ContainerInteraction<STACK> insertChemical) {
        if (stack.isEmpty()) {
            //Short circuit if nothing is actually being inserted
            return empty;
        }
        int tanks = tankCount.applyAsInt(side);
        if (tanks == 0) {
            return stack;
        } else if (tanks == 1) {
            return insertChemical.interact(0, stack, side, action);
        }
        STACK toInsert = stack;
        //Start by trying to insert into the tanks that have the same type
        IntList emptyTanks = new IntArrayList();
        for (int tank = 0; tank < tanks; tank++) {
            STACK inTank = inTankGetter.getStored(tank, side);
            if (inTank.isEmpty()) {
                emptyTanks.add(tank);
            } else if (ChemicalStack.isSameChemical(inTank, stack)) {
                STACK remainder = insertChemical.interact(tank, toInsert, side, action);
                if (remainder.isEmpty()) {
                    //If we have no remaining chemical, return that we fit it all
                    return empty;
                }
                //Update what we have left to insert, to be the amount we were unable to insert
                toInsert = remainder;
            }
        }
        for (int tank : emptyTanks) {
            STACK remainder = insertChemical.interact(tank, toInsert, side, action);
            if (remainder.isEmpty()) {
                //If we have no remaining chemical, return that we fit it all
                return empty;
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
    public static <CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>, TANK extends IChemicalTank<CHEMICAL, STACK>> STACK insert(STACK stack,
          @Nullable Direction side, Function<@Nullable Direction, List<TANK>> tankSupplier, Action action, AutomationType automationType, STACK empty) {
        if (stack.isEmpty()) {
            //Short circuit if nothing is actually being inserted
            return empty;
        }
        List<TANK> chemicalContainers = tankSupplier.apply(side);
        if (chemicalContainers.isEmpty()) {
            return stack;
        } else if (chemicalContainers.size() == 1) {
            return chemicalContainers.getFirst().insert(stack, action, automationType);
        }
        STACK toInsert = stack;
        //Start by trying to insert into the tanks that have the same type
        List<TANK> emptyTanks = new ArrayList<>();
        for (TANK tank : chemicalContainers) {
            if (tank.isEmpty()) {
                emptyTanks.add(tank);
            } else if (tank.isTypeEqual(stack)) {
                STACK remainder = tank.insert(toInsert, action, automationType);
                if (remainder.isEmpty()) {
                    //If we have no remaining chemical, return that we fit it all
                    return empty;
                }
                //Update what we have left to insert, to be the amount we were unable to insert
                toInsert = remainder;
            }
        }
        for (TANK tank : emptyTanks) {
            STACK remainder = tank.insert(toInsert, action, automationType);
            if (remainder.isEmpty()) {
                //If we have no remaining chemical, return that we fit it all
                return empty;
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
    public static <CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>> STACK extract(long amount, @Nullable Direction side, Action action,
          STACK empty, ToIntFunction<@Nullable Direction> tankCount, InContainerGetter<STACK> inTankGetter, LongContainerInteraction<STACK> extractChemical) {
        if (amount == 0) {
            return empty;
        }
        int tanks = tankCount.applyAsInt(side);
        if (tanks == 0) {
            return empty;
        } else if (tanks == 1) {
            return extractChemical.interact(0, amount, side, action);
        }
        STACK extracted = empty;
        long toDrain = amount;
        for (int tank = 0; tank < tanks; tank++) {
            if (extracted.isEmpty() || ChemicalStack.isSameChemical(extracted, inTankGetter.getStored(tank, side))) {
                //If there is chemical in the tank that matches the type we have started draining, or we haven't found a type yet
                STACK drained = extractChemical.interact(tank, toDrain, side, action);
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
    public static <CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>, TANK extends IChemicalTank<CHEMICAL, STACK>> STACK extract(long amount,
          @Nullable Direction side, Function<@Nullable Direction, List<TANK>> tankSupplier, Action action, AutomationType automationType, STACK empty) {
        if (amount == 0) {
            return empty;
        }
        List<TANK> chemicalTanks = tankSupplier.apply(side);
        if (chemicalTanks.isEmpty()) {
            return empty;
        } else if (chemicalTanks.size() == 1) {
            return chemicalTanks.getFirst().extract(amount, action, automationType);
        }
        STACK extracted = empty;
        long toDrain = amount;
        for (TANK tank : chemicalTanks) {
            if (extracted.isEmpty() || tank.isTypeEqual(extracted)) {
                //If there is chemical in the tank that matches the type we have started draining, or we haven't found a type yet
                STACK drained = tank.extract(toDrain, action, automationType);
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
    public static <CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>> STACK extract(STACK stack, @Nullable Direction side, Action action, STACK empty,
          ToIntFunction<@Nullable Direction> tankCount, InContainerGetter<STACK> inTankGetter, LongContainerInteraction<STACK> extractChemical) {
        if (stack.isEmpty()) {
            return empty;
        }
        int tanks = tankCount.applyAsInt(side);
        if (tanks == 0) {
            return empty;
        } else if (tanks == 1) {
            STACK inTank = inTankGetter.getStored(0, side);
            if (inTank.isEmpty() || !ChemicalStack.isSameChemical(inTank, stack)) {
                return empty;
            }
            return extractChemical.interact(0, stack.getAmount(), side, action);
        }
        STACK extracted = empty;
        long toDrain = stack.getAmount();
        for (int tank = 0; tank < tanks; tank++) {
            if (ChemicalStack.isSameChemical(stack, inTankGetter.getStored(tank, side))) {
                //If there is chemical in the tank that matches the type we are trying to drain, try to drain from it
                STACK drained = extractChemical.interact(tank, toDrain, side, action);
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
    public static <CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>, TANK extends IChemicalTank<CHEMICAL, STACK>> STACK extract(STACK stack,
          @Nullable Direction side, Function<@Nullable Direction, List<TANK>> tankSupplier, Action action, AutomationType automationType, STACK empty) {
        if (stack.isEmpty()) {
            return empty;
        }
        List<TANK> chemicalTanks = tankSupplier.apply(side);
        if (chemicalTanks.isEmpty()) {
            return empty;
        } else if (chemicalTanks.size() == 1) {
            TANK tank = chemicalTanks.getFirst();
            if (tank.isEmpty() || !tank.isTypeEqual(stack)) {
                return empty;
            }
            return tank.extract(stack.getAmount(), action, automationType);
        }
        STACK extracted = empty;
        long toDrain = stack.getAmount();
        for (TANK tank : chemicalTanks) {
            if (tank.isTypeEqual(stack)) {
                //If there is chemical in the tank that matches the type we are trying to drain, try to drain from it
                STACK drained = tank.extract(toDrain, action, automationType);
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