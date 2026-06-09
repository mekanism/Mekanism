package mekanism.common.capabilities.fluid;

import mekanism.api.AutomationType;
import mekanism.api.fluid.IFluidTank;
import mekanism.api.resource.ResourceContainerWrapper;
import mekanism.common.lib.multiblock.IValveHandler.ValveData;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jetbrains.annotations.Range;

public class ValveFluidTankWrapper extends ResourceContainerWrapper<FluidResource, IFluidTank> implements IFluidTank {

    private final ValveData valveData;

    public ValveFluidTankWrapper(IFluidTank internal, ValveData valveData) {
        super(internal);
        this.valveData = valveData;
    }

    @Override
    @Range(from = 0, to = Integer.MAX_VALUE)
    public int insert(FluidResource resource, @Range(from = 0, to = Integer.MAX_VALUE) int amount, TransactionContext transaction, AutomationType automationType) {
        int inserted = super.insert(resource, amount, transaction, automationType);
        if (inserted > 0) {
            this.valveData.onTransfer(transaction);
        }
        return inserted;
    }
}