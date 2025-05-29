package mekanism.api.chemical;

import java.util.List;
import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.IContentsListener;
import mekanism.api.annotations.NothingNullByDefault;
import net.minecraft.core.Direction;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public interface IMekanismChemicalHandler extends ISidedChemicalHandler, IContentsListener {

    /**
     * Used to check if an instance of {@link IMekanismChemicalHandler} actually has the ability to handle chemicals.
     *
     * @return True if we are actually capable of handling chemicals.
     *
     * @apiNote If for some reason you are comparing to {@link IMekanismChemicalHandler} without having gotten the object via the chemical handler capability, then you
     * must call this method to make sure that it really can handle chemicals. As most mekanism tiles have this class in their hierarchy.
     * @implNote If this returns false the capability should not be exposed AND methods should turn reasonable defaults for not doing anything.
     */
    default boolean canHandleChemicals() {
        return true;
    }

    /**
     * Returns the list of TANKs that this chemical handler exposes on the given side.
     *
     * @param side The side we are interacting with the handler from (null for internal).
     *
     * @return The list of all TANKs that this {@link IMekanismChemicalHandler} contains for the given side. If there are no tanks for the side or {@link #canHandleChemicals()} is
     * false then it returns an empty list.
     *
     * @implNote When side is null (an internal request), this method <em>MUST</em> return all tanks in the handler. Additionally, if {@link #canHandleChemicals()} is false, this
     * <em>MUST</em> return an empty list.
     */
    List<IChemicalTank> getChemicalTanks(@Nullable Direction side);

    /**
     * Returns the {@link IChemicalTank} that has the given index from the list of tanks on the given side.
     *
     * @param tank The index of the tank to retrieve.
     * @param side The side we are interacting with the handler from (null for internal).
     *
     * @return The {@link IChemicalTank} that has the given index from the list of tanks on the given side.
     */
    @Nullable
    default IChemicalTank getChemicalTank(int tank, @Nullable Direction side) {
        List<IChemicalTank> tanks = getChemicalTanks(side);
        return tank >= 0 && tank < tanks.size() ? tanks.get(tank) : null;
    }

    @Override
    default int getCountChemicalTanks(@Nullable Direction side) {
        return getChemicalTanks(side).size();
    }

    @Override
    default ChemicalStack getChemicalInTank(int tank, @Nullable Direction side) {
        IChemicalTank chemicalTank = getChemicalTank(tank, side);
        return chemicalTank == null ? ChemicalStack.EMPTY : chemicalTank.getStack();
    }

    @Override
    default void setChemicalInTank(int tank, ChemicalStack stack, @Nullable Direction side) {
        IChemicalTank chemicalTank = getChemicalTank(tank, side);
        if (chemicalTank != null) {
            chemicalTank.setStack(stack);
        }
    }

    @Override
    default long getChemicalTankCapacity(int tank, @Nullable Direction side) {
        IChemicalTank chemicalTank = getChemicalTank(tank, side);
        return chemicalTank == null ? 0 : chemicalTank.getCapacity();
    }

    @Override
    default boolean isValid(int tank, ChemicalStack stack, @Nullable Direction side) {
        IChemicalTank chemicalTank = getChemicalTank(tank, side);
        return chemicalTank != null && chemicalTank.isValid(stack);
    }

    /**
     * @implNote Any overrides to this should also override {@link #insertChemical(ChemicalStack, Direction, Action)} as it bypasses calling this method in order to skip
     * looking up the containers for every sub operation.
     */
    @Override
    default ChemicalStack insertChemical(int tank, ChemicalStack stack, @Nullable Direction side, Action action) {
        IChemicalTank chemicalTank = getChemicalTank(tank, side);
        return chemicalTank == null ? stack : chemicalTank.insert(stack, action, AutomationType.handler(side));
    }

    /**
     * @implNote Any overrides to this should also override {@link #extractChemical(long, Direction, Action)} and
     * {@link #extractChemical(ChemicalStack, Direction, Action)} as they bypass calling this method in order to skip looking up the containers for every sub
     * operation.
     */
    @Override
    default ChemicalStack extractChemical(int tank, long amount, @Nullable Direction side, Action action) {
        IChemicalTank chemicalTank = getChemicalTank(tank, side);
        return chemicalTank == null ? ChemicalStack.EMPTY : chemicalTank.extract(amount, action, AutomationType.handler(side));
    }

    @Override
    default ChemicalStack insertChemical(ChemicalStack stack, @Nullable Direction side, Action action) {
        return ChemicalUtils.insert(stack, side, this::getChemicalTanks, action, AutomationType.handler(side));
    }

    @Override
    default ChemicalStack extractChemical(long amount, @Nullable Direction side, Action action) {
        return ChemicalUtils.extract(amount, side, this::getChemicalTanks, action, AutomationType.handler(side));
    }

    @Override
    default ChemicalStack extractChemical(ChemicalStack stack, @Nullable Direction side, Action action) {
        return ChemicalUtils.extract(stack, side, this::getChemicalTanks, action, AutomationType.handler(side));
    }
}