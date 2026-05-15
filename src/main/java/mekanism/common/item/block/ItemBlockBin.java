package mekanism.common.item.block;

import java.util.function.Consumer;
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
import net.minecraft.world.item.component.TooltipDisplay;
import net.neoforged.neoforge.transfer.item.ItemResource;
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
    protected void addStats(@NotNull ItemStack stack, @NotNull Item.TooltipContext context, @NotNull TooltipDisplay tooltipDisplay, @NotNull Consumer<Component> tooltipAdder, @NotNull TooltipFlag flag) {
        ComponentBackedBinInventorySlot slot = BinInventorySlot.getForStack(stack);
        BinTier tier = getTier();
        if (slot != null && tier != null) {
            if (slot.isEmpty()) {
                tooltipAdder.accept(MekanismLang.EMPTY.translateColored(EnumColor.DARK_RED));
            } else {
                tooltipAdder.accept(MekanismLang.STORING.translateColored(EnumColor.BRIGHT_GREEN, EnumColor.GRAY, slot.getResource()));
                if (tier == BinTier.CREATIVE) {
                    tooltipAdder.accept(MekanismLang.ITEM_AMOUNT.translateColored(EnumColor.PURPLE, EnumColor.GRAY, MekanismLang.INFINITE));
                } else {
                    tooltipAdder.accept(MekanismLang.ITEM_AMOUNT.translateColored(EnumColor.PURPLE, EnumColor.GRAY, TextUtils.format(slot.amountAsInt())));
                }
            }
            ItemResource lockType = slot.getLockType();
            if (!lockType.isEmpty()) {
                tooltipAdder.accept(MekanismLang.LOCKED.translateColored(EnumColor.AQUA, EnumColor.GRAY, lockType));
            }
            if (tier == BinTier.CREATIVE) {
                tooltipAdder.accept(MekanismLang.CAPACITY.translateColored(EnumColor.INDIGO, EnumColor.GRAY, MekanismLang.INFINITE));
            } else {
                tooltipAdder.accept(MekanismLang.CAPACITY_ITEMS.translateColored(EnumColor.INDIGO, EnumColor.GRAY, TextUtils.format(tier.getStorage())));
            }
        }
    }

    @Override
    public boolean canContentsDrop(ItemStack stack) {
        return getTier() != BinTier.CREATIVE;
    }
}