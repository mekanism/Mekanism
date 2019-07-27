package mekanism.common.item;

import javax.annotation.Nonnull;
import mekanism.api.EnumColor;
import mekanism.common.util.LangUtils;
import net.minecraft.block.Block;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;

public class ItemBlockPlastic extends ItemBlock {

    public Block metaBlock;

    public ItemBlockPlastic(Block block) {
        super(block);
        metaBlock = block;
        setHasSubtypes(true);
    }

    @Override
    public int getMetadata(int i) {
        return i;
    }

    @Nonnull
    @Override
    public String getItemStackDisplayName(@Nonnull ItemStack stack) {
        EnumDyeColor dyeColour = EnumDyeColor.byDyeDamage(stack.getItemDamage() & 15);
        EnumColor colour = EnumColor.DYES[dyeColour.getDyeDamage()];
        String colourName;
        if (LangUtils.canLocalize(getTranslationKey(stack) + "." + colour.dyeName)) {
            return LangUtils.localize(getTranslationKey(stack) + "." + colour.dyeName);
        }
        if (colour == EnumColor.BLACK) {
            colourName = EnumColor.DARK_GREY + colour.getDyeName();
        } else {
            colourName = colour.getDyedName();
        }
        return colourName + " " + super.getItemStackDisplayName(stack);
    }
}