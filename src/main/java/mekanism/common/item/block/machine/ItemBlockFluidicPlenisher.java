package mekanism.common.item.block.machine;

import mekanism.common.attachments.containers.ContainerType;
import mekanism.common.block.prefab.BlockTile;
import mekanism.common.capabilities.fluid.BasicFluidTank;
import mekanism.common.capabilities.fluid.item.RateLimitFluidTank;
import mekanism.common.tile.machine.TileEntityFluidicPlenisher;
import net.neoforged.bus.api.IEventBus;

public class ItemBlockFluidicPlenisher extends ItemBlockMachine {

    public ItemBlockFluidicPlenisher(BlockTile<?, ?> block) {
        super(block);
    }

    @Override
    public void attachAttachments(IEventBus eventBus) {
        super.attachAttachments(eventBus);
        //Note: We pass null for the event bus to not expose this attachment as a capability
        ContainerType.FLUID.addDefaultContainer(null, this, stack -> RateLimitFluidTank.createBasicItem(TileEntityFluidicPlenisher.MAX_FLUID,
              BasicFluidTank.manualOnly, BasicFluidTank.alwaysTrueBi, BasicFluidTank.alwaysTrue
        ));
    }
}