package mekanism.common.item.block;

import java.util.List;
import mekanism.api.text.EnumColor;
import mekanism.common.MekanismLang;
import mekanism.common.block.attribute.Attribute;
import mekanism.common.block.basic.BlockBin;
import mekanism.common.inventory.BinMekanismInventory;
import mekanism.common.inventory.slot.BinInventorySlot;
import mekanism.common.item.interfaces.IItemSustainedInventory;
import mekanism.common.registration.impl.ItemDeferredRegister;
import mekanism.common.tier.BinTier;
import mekanism.common.util.text.TextUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class ItemBlockBin extends ItemBlockTooltip<BlockBin> implements IItemSustainedInventory {

    public ItemBlockBin(BlockBin block) {
        super(block, ItemDeferredRegister.getMekBaseProperties().stacksTo(1));
    }

    @Override
    public BinTier getTier() {
        return Attribute.getTier(getBlock(), BinTier.class);
    }

    @Override
    protected void addStats(@NotNull ItemStack stack, Level world, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        BinMekanismInventory inventory = BinMekanismInventory.create(stack);
        BinTier tier = getTier();
        if (inventory != null && tier != null) {
            BinInventorySlot slot = inventory.getBinSlot();
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
            if (slot.isLocked()) {
                tooltip.add(MekanismLang.LOCKED.translateColored(EnumColor.AQUA, EnumColor.GRAY, slot.getLockStack()));
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