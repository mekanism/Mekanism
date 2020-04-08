package mekanism.generators.common.content.fission;

import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.chemical.IChemicalTank;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.gas.IMekanismGasHandler;
import mekanism.api.fluid.IExtendedFluidTank;
import mekanism.api.fluid.IMekanismFluidHandler;
import mekanism.common.multiblock.SynchronizedData;
import mekanism.generators.common.tile.fission.TileEntityFissionReactorCasing;
import net.minecraft.util.Direction;

public class SynchronizedFissionReactorData extends SynchronizedData<SynchronizedFissionReactorData> implements IMekanismFluidHandler, IMekanismGasHandler {

    private List<IExtendedFluidTank> fluidTanks;
    private List<IChemicalTank<Gas, GasStack>> gasTanks;

    public SynchronizedFissionReactorData(TileEntityFissionReactorCasing tile) {

    }

    @Nonnull
    @Override
    public List<IExtendedFluidTank> getFluidTanks(@Nullable Direction side) {
        return fluidTanks;
    }

    @Nonnull
    @Override
    public List<? extends IChemicalTank<Gas, GasStack>> getGasTanks(@Nullable Direction side) {
        return gasTanks;
    }
}
