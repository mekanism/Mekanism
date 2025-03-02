package mekanism.common.item.block;

import java.util.List;
import mekanism.api.text.EnumColor;
import mekanism.common.MekanismLang;
import mekanism.common.attachments.LockData;
import mekanism.common.attachments.containers.item.ComponentBackedBinInventorySlot;
import mekanism.common.block.attribute.Attribute;
import mekanism.common.block.basic.BlockBin;
import mekanism.common.inventory.slot.BinInventorySlot;
import mekanism.common.item.interfaces.IDroppableContents.IDroppableAttachmentContents;
import mekanism.common.registries.MekanismDataComponents;
import mekanism.common.tier.BinTier;
import mekanism.common.util.text.TextUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import org.jetbrains.annotations.NotNull;

public class ItemBlockBin extends ItemBlockTooltip<BlockBin> implements IDroppableAttachmentContents {

    public ItemBlockBin(BlockBin block, Item.Properties properties) {
        super(block, properties.component(MekanismDataComponents.LOCK, LockData.EMPTY));
    }

    @Override
    public BinTier getTier() {
        return Attribute.getTier(getBlock(), BinTier.class);
    }

    @Override
    protected void addStats(@NotNull ItemStack stack, @NotNull Item.TooltipContext context, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        ComponentBackedBinInventorySlot slot = BinInventorySlot.getForStack(stack);
        BinTier tier = getTier();
        if (slot != null && tier != null) {
            if (slot.isEmpty()) {
                tooltip.add(MekanismLang.EMPTY.translateColored(EnumColor.DARK_RED));
            } else {
                tooltip.add(MekanismLang.STORING.translateColored(EnumColor.BRIGHT_GREEN, EnumColor.GRAY, slot.getStack()));
                if (tier == BinTier.CREATIVE) {
                    tooltip.add(MekanismLang.ITEM_AMOUNT.translateColored(EnumColor.PURPLE, EnumColor.GRAY, MekanismLang.INFINITE));
                } else {
                    tooltip.add(MekanismLang.ITEM_AMOUNT.translateColored(EnumColor.PURPLE, EnumColor.GRAY, TextUtils.format(slot.getCount())));
                }
            }
            ItemStack lockStack = slot.getLockStack();
            if (!lockStack.isEmpty()) {
                tooltip.add(MekanismLang.LOCKED.translateColored(EnumColor.AQUA, EnumColor.GRAY, lockStack));
            }
            if (tier == BinTier.CREATIVE) {
                tooltip.add(MekanismLang.CAPACITY.translateColored(EnumColor.INDIGO, EnumColor.GRAY, MekanismLang.INFINITE));
            } else {
                tooltip.add(MekanismLang.CAPACITY_ITEMS.translateColored(EnumColor.INDIGO, EnumColor.GRAY, TextUtils.format(tier.getStorage())));
            }
        }
    }

    @Override
    public boolean canContentsDrop(ItemStack stack) {
        return getTier() != BinTier.CREATIVE;
    }
}