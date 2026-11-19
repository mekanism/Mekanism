package mekanism.common.item.block;

import mekanism.common.block.attribute.Attribute;
import mekanism.common.block.basic.BlockBin;
import mekanism.common.component.LockData;
import mekanism.common.item.interfaces.IDroppableContents.IDroppableAttachmentContents;
import mekanism.common.registries.MekanismDataComponents;
import mekanism.common.tier.BinTier;
import mekanism.common.util.InventoryUtils;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.transfer.item.ItemResource;

public class ItemBlockBin extends BlockItem implements IDroppableAttachmentContents {

    public ItemBlockBin(BlockBin block, Item.Properties properties) {
        super(block, properties
              .component(MekanismDataComponents.BIN_TIER, Attribute.getTierNN(block, BinTier.class))
              .component(MekanismDataComponents.LOCK, LockData.EMPTY)
        );
    }

    @Override
    public void onDestroyed(ItemEntity item, DamageSource damageSource) {
        //Try to drop the inventory contents if we are a block item that persists our inventory
        InventoryUtils.dropItemContents(item, damageSource);
    }

    @Override
    public boolean canContentsDrop(ItemResource itemType) {
        BinTier tier = itemType.get(MekanismDataComponents.BIN_TIER);
        return tier == null || !tier.isCreative();
    }
}