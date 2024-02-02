package mekanism.common.item.block.machine;

import mekanism.api.chemical.ChemicalTankBuilder;
import mekanism.common.attachments.containers.ContainerType;
import mekanism.common.block.prefab.BlockTile;
import mekanism.common.capabilities.chemical.variable.RateLimitPigmentTank;
import mekanism.common.tile.machine.TileEntityPigmentExtractor;
import net.neoforged.bus.api.IEventBus;

public class ItemBlockPigmentExtractor extends ItemBlockMachine {

    public ItemBlockPigmentExtractor(BlockTile<?, ?> block) {
        super(block);
    }

    @Override
    public void attachAttachments(IEventBus eventBus) {
        super.attachAttachments(eventBus);
        //Note: We pass null for the event bus to not expose this attachment as a capability
        ContainerType.PIGMENT.addDefaultContainer(null, this, stack -> RateLimitPigmentTank.createBasicItem(TileEntityPigmentExtractor.MAX_PIGMENT,
              ChemicalTankBuilder.PIGMENT.manualOnly, ChemicalTankBuilder.PIGMENT.alwaysTrueBi, ChemicalTankBuilder.PIGMENT.alwaysTrue
        ));
    }
}