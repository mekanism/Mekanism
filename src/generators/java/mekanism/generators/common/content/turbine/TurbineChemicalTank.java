package mekanism.generators.common.content.turbine;

import mekanism.api.AutomationType;
import mekanism.api.IContentsListener;
import mekanism.api.chemical.ChemicalIds;
import mekanism.api.chemical.ChemicalResource;
import mekanism.common.capabilities.chemical.VariableCapacityChemicalTank;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jetbrains.annotations.Range;
import org.jspecify.annotations.Nullable;

public class TurbineChemicalTank extends VariableCapacityChemicalTank {

    private final TurbineMultiblockData multiblock;

    public TurbineChemicalTank(TurbineMultiblockData multiblock, @Nullable IContentsListener listener) {
        super(multiblock::getSteamCapacity, multiblock.notExternalFormedBiPred(), multiblock.formedBiPred(),
              chemical -> chemical.is(ChemicalIds.STEAM), null, null, null, listener);
        this.multiblock = multiblock;
    }

    @Override
    @Range(from = 0, to = Integer.MAX_VALUE)
    public int insert(ChemicalResource resource, @Range(from = 0, to = Integer.MAX_VALUE) int amount, TransactionContext transaction, AutomationType automationType) {
        int inserted = super.insert(resource, amount, transaction, automationType);
        if (multiblock.isFormed()) {
            multiblock.steamInputJournal.addSteam(inserted, transaction);
        }
        return inserted;
    }
}