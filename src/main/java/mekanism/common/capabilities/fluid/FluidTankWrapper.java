package mekanism.common.capabilities.fluid;

import mekanism.api.AutomationType;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.IChemicalTank;
import mekanism.api.fluid.IFluidTank;
import mekanism.api.resource.ResourceContainerWrapper;
import mekanism.common.capabilities.merged.ChemicalTankWrapper;
import mekanism.common.capabilities.merged.MergedTank;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jetbrains.annotations.Range;

/**
 * Like {@link ChemicalTankWrapper}
 */
@NothingNullByDefault
public class FluidTankWrapper extends ResourceContainerWrapper<FluidResource, IFluidTank> implements IFluidTank {

    private final IChemicalTank chemicalTank;;
    private final MergedTank mergedTank;

    public FluidTankWrapper(MergedTank mergedTank, IFluidTank internal, IChemicalTank chemicalTank) {
        super(internal);
        //TODO: Do we want to short circuit it so that if we are not empty it allows for inserting before checking the insertCheck
        this.mergedTank = mergedTank;
        this.chemicalTank = chemicalTank;
    }

    public MergedTank getMergedTank() {
        return mergedTank;
    }

    @Override
    @Range(from = 0, to = Integer.MAX_VALUE)
    public int insert(FluidResource resource, @Range(from = 0, to = Integer.MAX_VALUE) int amount, TransactionContext transaction, AutomationType automationType) {
        //Only allow inserting if the chemical tank is empty
        return chemicalTank.isEmpty() ? super.insert(resource, amount, transaction, automationType) : 0;
    }
}