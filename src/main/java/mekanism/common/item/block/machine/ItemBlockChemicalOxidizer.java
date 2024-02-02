package mekanism.common.item.block.machine;

import mekanism.api.chemical.ChemicalTankBuilder;
import mekanism.common.attachments.containers.ContainerType;
import mekanism.common.block.prefab.BlockTile;
import mekanism.common.capabilities.chemical.variable.RateLimitGasTank;
import mekanism.common.tile.machine.TileEntityChemicalOxidizer;
import net.neoforged.bus.api.IEventBus;

public class ItemBlockChemicalOxidizer extends ItemBlockMachine {

    public ItemBlockChemicalOxidizer(BlockTile<?, ?> block) {
        super(block);
    }

    @Override
    public void attachAttachments(IEventBus eventBus) {
        super.attachAttachments(eventBus);
        //Note: We pass null for the event bus to not expose this attachment as a capability
        ContainerType.GAS.addDefaultContainer(null, this, stack -> RateLimitGasTank.createBasicItem(TileEntityChemicalOxidizer.MAX_GAS,
              ChemicalTankBuilder.GAS.manualOnly, ChemicalTankBuilder.GAS.alwaysTrueBi, ChemicalTankBuilder.GAS.alwaysTrue
        ));
    }
}