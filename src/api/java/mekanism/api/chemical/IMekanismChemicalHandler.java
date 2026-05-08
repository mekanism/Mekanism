package mekanism.api.chemical;

import java.util.List;
import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.container.IMekanismResourceHandler;
import net.minecraft.core.Direction;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public interface IMekanismChemicalHandler extends IMekanismResourceHandler<ChemicalResource, IChemicalTank>, IChemicalHandler {

    @Override
    default ChemicalResource getEmptyResource() {
        return ChemicalResource.EMPTY;
    }

    /**
     * Returns the {@link IChemicalTank} that has the given index from the list of tanks on the given side.
     *
     * @param tank The index of the tank to retrieve.
     *
     * @return The {@link IChemicalTank} that has the given index from the list of tanks on the given side.
     */
    @Nullable
    default IChemicalTank getChemicalTank(int tank) {
        List<IChemicalTank> tanks = getContainers();
        return tank >= 0 && tank < tanks.size() ? tanks.get(tank) : null;
    }

    @Override
    default int getChemicalTanks() {
        return getContainers().size();
    }

    @Override
    default ChemicalStack getChemicalInTank(int tank) {
        IChemicalTank chemicalTank = getChemicalTank(tank);
        return chemicalTank == null ? ChemicalStack.EMPTY : chemicalTank.getStack();
    }

    @Override
    default void setChemicalInTank(int tank, ChemicalStack stack) {
        IChemicalTank chemicalTank = getChemicalTank(tank);
        if (chemicalTank != null) {
            chemicalTank.setStack(stack);
        }
    }

    @Override
    default long getChemicalTankCapacity(int tank) {
        IChemicalTank chemicalTank = getChemicalTank(tank);
        return chemicalTank == null ? 0 : chemicalTank.getCapacity();
    }

    @Override
    default boolean isValid(int tank, ChemicalStack stack) {
        IChemicalTank chemicalTank = getChemicalTank(tank);
        return chemicalTank != null && chemicalTank.isChemicalValid(stack);
    }

    /**
     * @implNote Any overrides to this should also override {@link #insertChemical(ChemicalStack, Direction, Action)} as it bypasses calling this method in order to skip
     * looking up the containers for every sub operation.
     */
    @Override
    default ChemicalStack insertChemical(int tank, ChemicalStack stack, Action action) {
        IChemicalTank chemicalTank = getChemicalTank(tank);
        return chemicalTank == null ? stack : chemicalTank.insert(stack, action, AutomationType.INTERNAL);
    }

    /**
     * @implNote Any overrides to this should also override {@link #extractChemical(long, Direction, Action)} and
     * {@link #extractChemical(ChemicalStack, Direction, Action)} as they bypass calling this method in order to skip looking up the containers for every sub
     * operation.
     */
    @Override
    default ChemicalStack extractChemical(int tank, long amount, Action action) {
        IChemicalTank chemicalTank = getChemicalTank(tank);
        return chemicalTank == null ? ChemicalStack.EMPTY : chemicalTank.extract(amount, action, AutomationType.INTERNAL);
    }

    @Override
    default ChemicalStack insertChemical(ChemicalStack stack, Action action) {
        return ChemicalUtils.insert(stack, null, _ -> getContainers(), action, AutomationType.INTERNAL);
    }

    @Override
    default ChemicalStack extractChemical(long amount, Action action) {
        return ChemicalUtils.extract(amount, null, _ -> getContainers(), action, AutomationType.INTERNAL);
    }

    @Override
    default ChemicalStack extractChemical(ChemicalStack stack, Action action) {
        return ChemicalUtils.extract(stack, null, _ -> getContainers(), action, AutomationType.INTERNAL);
    }
}