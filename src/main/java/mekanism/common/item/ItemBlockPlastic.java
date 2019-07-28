package mekanism.common.item;

import javax.annotation.Nonnull;
import mekanism.api.EnumColor;
import mekanism.common.block.interfaces.IColoredBlock;
import mekanism.common.util.LangUtils;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class ItemBlockPlastic extends ItemBlockMekanism {

    public ItemBlockPlastic(Block block) {
        super(block);
    }

    @Nonnull
    @Override
    public String getItemStackDisplayName(@Nonnull ItemStack stack) {
        EnumColor color = getColor(stack);
        String colorName;
        if (LangUtils.canLocalize(getTranslationKey(stack) + "." + color.dyeName)) {
            return LangUtils.localize(getTranslationKey(stack) + "." + color.dyeName);
        }
        if (color == EnumColor.BLACK) {
            colorName = EnumColor.DARK_GREY + color.getDyeName();
        } else {
            colorName = color.getDyedName();
        }
        return colorName + " " + super.getItemStackDisplayName(stack);
    }

    private EnumColor getColor(ItemStack stack) {
        Item item = stack.getItem();
        if (item instanceof ItemBlockPlastic) {
            IColoredBlock block = (IColoredBlock) (((ItemBlockPlastic) item).block);
            return block.getColor();
        }
        return EnumColor.BLACK;
    }
}