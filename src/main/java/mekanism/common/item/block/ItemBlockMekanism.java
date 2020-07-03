package mekanism.common.item.block;

import javax.annotation.Nonnull;
import mekanism.api.text.EnumColor;
import mekanism.api.text.TextComponentUtil;
import mekanism.api.tier.ITier;
import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;

public class ItemBlockMekanism<BLOCK extends Block> extends BlockItem {

    @Nonnull
    private final BLOCK block;

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

    @Nonnull
    @Override
    public ITextComponent getDisplayName(@Nonnull ItemStack stack) {
        EnumColor color = getTextColor(stack);
        if (color == null) {
            return super.getDisplayName(stack);
        }
        return TextComponentUtil.build(color, super.getDisplayName(stack));
    }
}