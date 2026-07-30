package mekanism.common.item.block;

import mekanism.common.block.attribute.Attribute;
import mekanism.common.block.basic.BlockBin;
import mekanism.common.component.LockData;
import mekanism.common.item.interfaces.IDroppableContents.IDroppableAttachmentContents;
import mekanism.common.registries.MekanismDataComponents;
import mekanism.common.tier.BinTier;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.transfer.item.ItemResource;

public class ItemBlockBin extends ItemBlockTooltip<BlockBin> implements IDroppableAttachmentContents {

    public ItemBlockBin(BlockBin block, Item.Properties properties) {
        super(block, properties
              .component(MekanismDataComponents.BIN_TIER, Attribute.getTierNN(block, BinTier.class))
              .component(MekanismDataComponents.LOCK, LockData.EMPTY)
        );
    }

    @Override
    public boolean canContentsDrop(ItemResource itemType) {
        BinTier tier = itemType.get(MekanismDataComponents.BIN_TIER);
        return tier == null || !tier.isCreative();
    }
}