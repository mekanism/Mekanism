package mekanism.common.item.block;

import mekanism.client.render.item.ISTERProvider;
import mekanism.common.block.BlockIndustrialAlarm;
import mekanism.common.registration.impl.ItemDeferredRegister;

public class ItemBlockIndustrialAlarm extends ItemBlockTooltip<BlockIndustrialAlarm> {

    public ItemBlockIndustrialAlarm(BlockIndustrialAlarm block) {
        super(block, ItemDeferredRegister.getMekBaseProperties().setISTER(ISTERProvider::industrialAlarm));
    }
}
