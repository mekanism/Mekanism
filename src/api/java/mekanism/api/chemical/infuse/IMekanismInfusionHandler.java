package mekanism.api.chemical.infuse;

import java.util.List;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.api.Action;
import mekanism.api.chemical.IChemicalTank;
import mekanism.api.inventory.AutomationType;
import net.minecraft.util.Direction;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public interface IMekanismInfusionHandler extends ISidedInfusionHandler {

    /**
     * Used to check if an instance of {@link IMekanismInfusionHandler} actually has the ability to handle infuse types.
     *
     * @return True if we are actually capable of handling infuse types.
     *
     * @apiNote If for some reason you are comparing to {@link IMekanismInfusionHandler} without having gotten the object via the infusion handler capability, then you
     * must call this method to make sure that it really can handle infusion types. As most mekanism tiles have this class in their hierarchy.
     * @implNote If this returns false the capability should not be exposed AND methods should turn reasonable defaults for not doing anything.
     */
    default boolean canHandleInfusion() {
        return true;
    }

    /**
     * Returns the list of IChemicalTanks that this infusion handler exposes on the given side.
     *
     * @param side The side we are interacting with the handler from (null for internal).
     *
     * @return The list of all IChemicalTanks that this {@link IMekanismInfusionHandler} contains for the given side. If there are no tanks for the side or {@link
     * #canHandleInfusion()} is false then it returns an empty list.
     *
     * @implNote When side is null (an internal request), this method <em>MUST</em> return all tanks in the handler. Additionally, if {@link #canHandleInfusion()} is
     * false, this <em>MUST</em> return an empty list.
     */
    List<? extends IChemicalTank<InfuseType, InfusionStack>> getInfusionTanks(@Nullable Direction side);

    /**
     * Called when the contents of this infusion handler change.
     */
    void onContentsChanged();

    /**
     * Returns the {@link IChemicalTank} that has the given index from the list of tanks on the given side.
     *
     * @param tank The index of the tank to retrieve.
     * @param side The side we are interacting with the handler from (null for internal).
     *
     * @return The {@link IChemicalTank} that has the given index from the list of tanks on the given side.
     */
    @Nullable
    default IChemicalTank<InfuseType, InfusionStack> getInfusionTank(int tank, @Nullable Direction side) {
        List<? extends IChemicalTank<InfuseType, InfusionStack>> tanks = getInfusionTanks(side);
        return tank >= 0 && tank < tanks.size() ? tanks.get(tank) : null;
    }

    @Override
    default int getInfusionTankCount(@Nullable Direction side) {
        return getInfusionTanks(side).size();
    }

    @Override
    default InfusionStack getInfusionInTank(int tank, @Nullable Direction side) {
        IChemicalTank<InfuseType, InfusionStack> infusionTank = getInfusionTank(tank, side);
        return infusionTank == null ? InfusionStack.EMPTY : infusionTank.getStack();
    }

    @Override
    default void setInfusionInTank(int tank, InfusionStack stack, @Nullable Direction side) {
        IChemicalTank<InfuseType, InfusionStack> infusionTank = getInfusionTank(tank, side);
        if (infusionTank != null) {
            infusionTank.setStack(stack);
        }
    }

    @Override
    default int getInfusionTankCapacity(int tank, @Nullable Direction side) {
        IChemicalTank<InfuseType, InfusionStack> infusionTank = getInfusionTank(tank, side);
        return infusionTank == null ? 0 : infusionTank.getCapacity();
    }

    @Override
    default boolean isInfusionValid(int tank, InfusionStack stack, @Nullable Direction side) {
        IChemicalTank<InfuseType, InfusionStack> infusionTank = getInfusionTank(tank, side);
        return infusionTank != null && infusionTank.isValid(stack);
    }

    @Override
    default InfusionStack insertInfusion(int tank, InfusionStack stack, @Nullable Direction side, Action action) {
        IChemicalTank<InfuseType, InfusionStack> infusionTank = getInfusionTank(tank, side);
        return infusionTank == null ? stack : infusionTank.insert(stack, action, side == null ? AutomationType.INTERNAL : AutomationType.EXTERNAL);
    }

    @Override
    default InfusionStack extractInfusion(int tank, int amount, @Nullable Direction side, Action action) {
        IChemicalTank<InfuseType, InfusionStack> infusionTank = getInfusionTank(tank, side);
        return infusionTank == null ? InfusionStack.EMPTY : infusionTank.extract(amount, action, side == null ? AutomationType.INTERNAL : AutomationType.EXTERNAL);
    }
}