package mekanism.common.item.block;

import javax.annotation.Nonnull;
import mekanism.api.block.IColoredBlock;
import mekanism.api.text.EnumColor;
import mekanism.common.util.text.TextComponentUtil;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;

public class ItemBlockColoredName<BLOCK extends Block & IColoredBlock> extends ItemBlockMekanism<BLOCK> {

    public ItemBlockColoredName(BLOCK block) {
        this(block, new Item.Properties());
    }

    public ItemBlockColoredName(BLOCK block, Item.Properties properties) {
        super(block, properties);
    }

    @Nonnull
    @Override
    public ITextComponent getDisplayName(@Nonnull ItemStack stack) {
        //TODO: Can this be removed? Or is this what is getting it to be colored
        EnumColor color = getColor(stack);
        if (color == EnumColor.BLACK) {
            color = EnumColor.DARK_GRAY;
        }
        return TextComponentUtil.build(color, super.getDisplayName(stack));
    }

    private EnumColor getColor(ItemStack stack) {
        Item item = stack.getItem();
        if (item instanceof ItemBlockColoredName) {
            return ((IColoredBlock) ((ItemBlockColoredName) item).getBlock()).getColor();
        }
        return EnumColor.BLACK;
    }
}