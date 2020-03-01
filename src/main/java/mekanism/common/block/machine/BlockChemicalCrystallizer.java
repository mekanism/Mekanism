package mekanism.common.block.machine;

import mekanism.api.block.IHasModel;
import mekanism.common.block.states.IStateFluidLoggable;
import mekanism.common.content.machines.Machine;
import mekanism.common.registries.MekanismMachines;
import mekanism.common.tile.TileEntityChemicalCrystallizer;

public class BlockChemicalCrystallizer extends BlockMachine<TileEntityChemicalCrystallizer, Machine<TileEntityChemicalCrystallizer>> implements IHasModel, IStateFluidLoggable {

    public BlockChemicalCrystallizer() {
        super(MekanismMachines.CHEMICAL_CRYSTALLIZER);
    }
}