package mekanism.api.chemical;

import java.util.List;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.container.IMekanismResourceHandler;
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
}