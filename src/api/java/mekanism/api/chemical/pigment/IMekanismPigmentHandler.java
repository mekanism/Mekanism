package mekanism.api.chemical.pigment;

import java.util.List;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.api.Action;
import mekanism.api.inventory.AutomationType;
import net.minecraft.util.Direction;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public interface IMekanismPigmentHandler extends ISidedPigmentHandler {

    /**
     * Used to check if an instance of {@link IMekanismPigmentHandler} actually has the ability to handle pigments.
     *
     * @return True if we are actually capable of handling pigments.
     *
     * @apiNote If for some reason you are comparing to {@link IMekanismPigmentHandler} without having gotten the object via the pigment handler capability, then you
     * must call this method to make sure that it really can handle pigments. As most mekanism tiles have this class in their hierarchy.
     * @implNote If this returns false the capability should not be exposed AND methods should turn reasonable defaults for not doing anything.
     */
    default boolean canHandlePigment() {
        return true;
    }

    /**
     * Returns the list of IPigmentTanks that this pigment handler exposes on the given side.
     *
     * @param side The side we are interacting with the handler from (null for internal).
     *
     * @return The list of all IPigmentTanks that this {@link IMekanismPigmentHandler} contains for the given side. If there are no tanks for the side or {@link
     * #canHandlePigment()} is false then it returns an empty list.
     *
     * @implNote When side is null (an internal request), this method <em>MUST</em> return all tanks in the handler. Additionally, if {@link #canHandlePigment()} is
     * false, this <em>MUST</em> return an empty list.
     */
    List<IPigmentTank> getPigmentTanks(@Nullable Direction side);

    /**
     * Called when the contents of this pigment handler change.
     */
    void onContentsChanged();

    /**
     * Returns the {@link IPigmentTank} that has the given index from the list of tanks on the given side.
     *
     * @param tank The index of the tank to retrieve.
     * @param side The side we are interacting with the handler from (null for internal).
     *
     * @return The {@link IPigmentTank} that has the given index from the list of tanks on the given side.
     */
    @Nullable
    default IPigmentTank getPigmentTank(int tank, @Nullable Direction side) {
        List<IPigmentTank> tanks = getPigmentTanks(side);
        return tank >= 0 && tank < tanks.size() ? tanks.get(tank) : null;
    }

    @Override
    default int getPigmentTankCount(@Nullable Direction side) {
        return getPigmentTanks(side).size();
    }

    @Override
    default PigmentStack getPigmentInTank(int tank, @Nullable Direction side) {
        IPigmentTank pigmentTank = getPigmentTank(tank, side);
        return pigmentTank == null ? PigmentStack.EMPTY : pigmentTank.getStack();
    }

    @Override
    default void setPigmentInTank(int tank, PigmentStack stack, @Nullable Direction side) {
        IPigmentTank pigmentTank = getPigmentTank(tank, side);
        if (pigmentTank != null) {
            pigmentTank.setStack(stack);
        }
    }

    @Override
    default long getPigmentTankCapacity(int tank, @Nullable Direction side) {
        IPigmentTank pigmentTank = getPigmentTank(tank, side);
        return pigmentTank == null ? 0 : pigmentTank.getCapacity();
    }

    @Override
    default boolean isPigmentValid(int tank, PigmentStack stack, @Nullable Direction side) {
        IPigmentTank pigmentTank = getPigmentTank(tank, side);
        return pigmentTank != null && pigmentTank.isValid(stack);
    }

    @Override
    default PigmentStack insertPigment(int tank, PigmentStack stack, @Nullable Direction side, Action action) {
        IPigmentTank pigmentTank = getPigmentTank(tank, side);
        return pigmentTank == null ? stack : pigmentTank.insert(stack, action, side == null ? AutomationType.INTERNAL : AutomationType.EXTERNAL);
    }

    @Override
    default PigmentStack extractPigment(int tank, long amount, @Nullable Direction side, Action action) {
        IPigmentTank pigmentTank = getPigmentTank(tank, side);
        return pigmentTank == null ? PigmentStack.EMPTY : pigmentTank.extract(amount, action, side == null ? AutomationType.INTERNAL : AutomationType.EXTERNAL);
    }
}