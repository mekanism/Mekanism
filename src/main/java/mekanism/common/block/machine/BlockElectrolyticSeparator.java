package mekanism.common.block.machine;

import mekanism.api.block.IHasModel;
import mekanism.common.block.states.IStateFluidLoggable;
import mekanism.common.content.machines.Machine;
import mekanism.common.registries.MekanismMachines;
import mekanism.common.tile.TileEntityElectrolyticSeparator;

public class BlockElectrolyticSeparator extends BlockMachine<TileEntityElectrolyticSeparator, Machine<TileEntityElectrolyticSeparator>> implements IHasModel, IStateFluidLoggable {

    public BlockElectrolyticSeparator() {
        super(MekanismMachines.ELECTROLYTIC_SEPARATOR);
    }
}