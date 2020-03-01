package mekanism.common.block.machine;

import mekanism.api.block.IHasModel;
import mekanism.common.block.states.IStateFluidLoggable;
import mekanism.common.registries.MekanismMachines;
import mekanism.common.tile.TileEntityMetallurgicInfuser;

public class BlockMetallurgicInfuser extends BlockFactoryMachine<TileEntityMetallurgicInfuser> implements IHasModel, IStateFluidLoggable {

    public BlockMetallurgicInfuser() {
        super(MekanismMachines.METALLURGIC_INFUSER);
    }
}