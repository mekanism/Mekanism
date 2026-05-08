package mekanism.generators.common.content.turbine;

import mekanism.api.AutomationType;
import mekanism.api.IContentsListener;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.ChemicalResource;
import mekanism.common.capabilities.chemical.VariableCapacityChemicalTank;
import mekanism.common.registries.MekanismChemicals;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public class TurbineChemicalTank extends VariableCapacityChemicalTank {

    private final TurbineMultiblockData multiblock;

    public TurbineChemicalTank(TurbineMultiblockData multiblock, @Nullable IContentsListener listener) {
        super(multiblock::getSteamCapacity, multiblock.notExternalFormedBiPred(), multiblock.formedBiPred(),
              chemical -> chemical.is(MekanismChemicals.STEAM), null, listener);
        this.multiblock = multiblock;
    }

    @Override
    public int insert(ChemicalResource resource, int amount, TransactionContext transaction, AutomationType automationType) {
        int inserted = super.insert(resource, amount, transaction, automationType);
        if (multiblock.isFormed()) {
            //TODO - 26.1: Test this
            multiblock.steamInputJournal.addSteam(inserted, transaction);
        }
        return inserted;
    }
}