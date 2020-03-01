package mekanism.common.block.machine;

import mekanism.api.block.IHasModel;
import mekanism.common.block.states.IStateFluidLoggable;
import mekanism.common.content.machines.Machine;
import mekanism.common.inventory.container.ContainerProvider;
import mekanism.common.inventory.container.tile.DigitalMinerContainer;
import mekanism.common.registries.MekanismMachines;
import mekanism.common.tile.TileEntityDigitalMiner;
import mekanism.common.util.text.TextComponentUtil;
import net.minecraft.inventory.container.INamedContainerProvider;

public class BlockDigitalMiner extends BlockMachine<TileEntityDigitalMiner, Machine<TileEntityDigitalMiner>> implements IHasModel, IStateFluidLoggable {

    public BlockDigitalMiner() {
        super(MekanismMachines.DIGITAL_MINER);
    }
}
