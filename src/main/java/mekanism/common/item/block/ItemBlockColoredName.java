package mekanism.common.item.block;

import javax.annotation.Nonnull;
import mekanism.api.EnumColor;
import mekanism.common.block.interfaces.IColoredBlock;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class ItemBlockColoredName extends ItemBlockMekanism {

    public ItemBlockColoredName(Block block) {
        super(block);
    }

    @Nonnull
    @Override
    public String getItemStackDisplayName(@Nonnull ItemStack stack) {
        //TODO: Can this be removed? Or is this what is getting it to be colored
        EnumColor color = getColor(stack);
        if (color == EnumColor.BLACK) {
            color = EnumColor.DARK_GREY;
        }
        return color + super.getItemStackDisplayName(stack);
    }

    private EnumColor getColor(ItemStack stack) {
        Item item = stack.getItem();
        if (item instanceof ItemBlockColoredName) {
            return ((IColoredBlock) (((ItemBlockColoredName) item).block)).getColor();
        }
        return EnumColor.BLACK;
    }
}