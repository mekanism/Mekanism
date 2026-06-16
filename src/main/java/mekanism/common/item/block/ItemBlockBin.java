package mekanism.common.item.block;

import java.util.function.Consumer;
import mekanism.api.resource.LargeResourceStack;
import mekanism.api.text.EnumColor;
import mekanism.common.MekanismLang;
import mekanism.common.block.attribute.Attribute;
import mekanism.common.block.basic.BlockBin;
import mekanism.common.component.LockData;
import mekanism.common.component.containers.type.ContainerType;
import mekanism.common.item.interfaces.IDroppableContents.IDroppableAttachmentContents;
import mekanism.common.registries.MekanismDataComponents;
import mekanism.common.tier.BinTier;
import mekanism.common.util.StorageUtils;
import mekanism.common.util.text.TextUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.item.ItemResource;

public class ItemBlockBin extends ItemBlockTooltip<BlockBin> implements IDroppableAttachmentContents {

    private final BinTier tier;

    public ItemBlockBin(BlockBin block, Item.Properties properties) {
        tier = Attribute.getTierNN(block, BinTier.class);
        super(block, properties.component(MekanismDataComponents.LOCK, LockData.EMPTY));
    }

    @Override
    public BinTier getTier() {
        return tier;
    }

    @Override
    protected void addStats(ItemStack stack, ItemAccess itemAccess, Item.TooltipContext context, TooltipDisplay tooltipDisplay,
          Consumer<Component> tooltipAdder, TooltipFlag flag) {
        LargeResourceStack<ItemResource> contents = ContainerType.ITEM.getStoredContentsFromAttachment(itemAccess);
        if (contents.isEmpty()) {
            tooltipAdder.accept(MekanismLang.EMPTY.translateColored(EnumColor.DARK_RED));
        } else {
            tooltipAdder.accept(MekanismLang.STORING.translateColored(EnumColor.BRIGHT_GREEN, EnumColor.GRAY, contents.resource()));
            if (tier == BinTier.CREATIVE) {
                tooltipAdder.accept(MekanismLang.ITEM_AMOUNT.translateColored(EnumColor.PURPLE, EnumColor.GRAY, MekanismLang.INFINITE));
            } else {
                tooltipAdder.accept(MekanismLang.ITEM_AMOUNT.translateColored(EnumColor.PURPLE, EnumColor.GRAY, TextUtils.format(contents.amount())));
            }
        }
        ItemResource lockType = stack.getOrDefault(MekanismDataComponents.LOCK, LockData.EMPTY).lock();
        if (!lockType.isEmpty()) {
            tooltipAdder.accept(MekanismLang.LOCKED.translateColored(EnumColor.AQUA, EnumColor.GRAY, lockType));
        }
        StorageUtils.addCapacity(tooltipAdder, tier);
    }

    @Override
    public boolean canContentsDrop(ItemResource itemType) {
        return tier != BinTier.CREATIVE;
    }
}