package mekanism.common.item.block.machine;

import mekanism.common.attachments.containers.ContainerType;
import mekanism.common.block.prefab.BlockTile;
import mekanism.common.capabilities.fluid.BasicFluidTank;
import mekanism.common.capabilities.fluid.item.RateLimitFluidTank;
import mekanism.common.tile.machine.TileEntityElectricPump;
import net.neoforged.bus.api.IEventBus;

public class ItemBlockElectricPump extends ItemBlockMachine {

    public ItemBlockElectricPump(BlockTile<?, ?> block) {
        super(block);
    }

    @Override
    public void attachAttachments(IEventBus eventBus) {
        super.attachAttachments(eventBus);
        //Note: We pass null for the event bus to not expose this attachment as a capability
        ContainerType.FLUID.addDefaultContainer(null, this, stack -> RateLimitFluidTank.createBasicItem(TileEntityElectricPump.MAX_FLUID,
              BasicFluidTank.manualOnly, BasicFluidTank.alwaysTrueBi, BasicFluidTank.alwaysTrue
        ));
    }
}