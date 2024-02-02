package mekanism.common.item.block.machine;

import mekanism.common.attachments.containers.ContainerType;
import mekanism.common.block.prefab.BlockTile;
import mekanism.common.capabilities.heat.BasicHeatCapacitor;
import mekanism.common.tile.machine.TileEntityResistiveHeater;
import net.neoforged.bus.api.IEventBus;

public class ItemBlockResistiveHeater extends ItemBlockMachine {

    public ItemBlockResistiveHeater(BlockTile<?, ?> block) {
        super(block);
    }

    @Override
    public void attachAttachments(IEventBus eventBus) {
        super.attachAttachments(eventBus);
        //Note: We pass null for the event bus to not expose this attachment as a capability
        //TODO - 1.20.4: Re-evaluate as I believe the resistive heater's heat capacity and stuff can be changed?
        ContainerType.HEAT.addDefaultContainer(null, this, stack -> BasicHeatCapacitor.createBasicItem(TileEntityResistiveHeater.HEAT_CAPACITY,
              TileEntityResistiveHeater.INVERSE_CONDUCTION_COEFFICIENT, TileEntityResistiveHeater.INVERSE_INSULATION_COEFFICIENT
        ));
    }
}