package mekanism.common.item.block;

import javax.annotation.Nonnull;
import mekanism.api.text.EnumColor;
import mekanism.api.tier.ITier;
import mekanism.common.registration.impl.ItemDeferredRegister;
import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;

public class ItemBlockMekanism<BLOCK extends Block> extends BlockItem {

    @Nonnull
    private final BLOCK block;

    public ItemBlockMekanism(@Nonnull BLOCK block) {
        this(block, ItemDeferredRegister.getMekBaseProperties());
    }

    public ItemBlockMekanism(@Nonnull BLOCK block, Item.Properties properties) {
        super(block, properties);
        this.block = block;
    }

    @Nonnull
    @Override
    public BLOCK getBlock() {
        return block;
    }

    public ITier getTier() {
        return null;
    }

    public EnumColor getTextColor(ItemStack stack) {
        return getTier() != null ? getTier().getBaseTier().getTextColor() : null;
    }

    @Override
    public ITextComponent getDisplayName(ItemStack stack) {
        EnumColor color = getTextColor(stack);
        if (color == null)
            return super.getDisplayName(stack);
        return super.getDisplayName(stack).applyTextStyle(color.textFormatting);
    }
}