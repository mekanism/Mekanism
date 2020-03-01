package mekanism.common.block.machine;

import mekanism.api.block.IHasModel;
import mekanism.common.block.states.IStateFluidLoggable;
import mekanism.common.content.machines.Machine;
import mekanism.common.registries.MekanismMachines;
import mekanism.common.tile.TileEntityPressurizedReactionChamber;

public class BlockPressurizedReactionChamber extends BlockMachine<TileEntityPressurizedReactionChamber, Machine<TileEntityPressurizedReactionChamber>> implements IHasModel, IStateFluidLoggable {

    public BlockPressurizedReactionChamber() {
        super(MekanismMachines.PRESSURIZED_REACTION_CHAMBER);
    }
}