package mekanism.common.item.block.machine;

import mekanism.common.attachments.containers.ContainerType;
import mekanism.common.block.prefab.BlockTile;
import mekanism.common.capabilities.heat.BasicHeatCapacitor;
import mekanism.common.tile.machine.TileEntityFuelwoodHeater;
import net.neoforged.bus.api.IEventBus;

public class ItemBlockFuelwoodHeater extends ItemBlockMachine {

    public ItemBlockFuelwoodHeater(BlockTile<?, ?> block) {
        super(block);
    }

    @Override
    public void attachAttachments(IEventBus eventBus) {
        super.attachAttachments(eventBus);
        //Note: We pass null for the event bus to not expose this attachment as a capability
        ContainerType.HEAT.addDefaultContainer(null, this, stack -> BasicHeatCapacitor.createBasicItem(TileEntityFuelwoodHeater.HEAT_CAPACITY,
              TileEntityFuelwoodHeater.INVERSE_CONDUCTION_COEFFICIENT, TileEntityFuelwoodHeater.INVERSE_INSULATION_COEFFICIENT
        ));
    }
}