package mekanism.common.item.interfaces;

import mekanism.api.NBTConstants;
import mekanism.api.text.EnumColor;
import mekanism.common.util.ItemDataUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public interface IColoredItem {

    @Nullable
    default EnumColor getColor(ItemStack stack) {
        if (ItemDataUtils.hasData(stack, NBTConstants.COLOR, Tag.TAG_INT)) {
            return EnumColor.byIndexStatic(ItemDataUtils.getInt(stack, NBTConstants.COLOR));
        }
        return null;
    }

    default void setColor(ItemStack stack, @Nullable EnumColor color) {
        if (color == null) {
            ItemDataUtils.removeData(stack, NBTConstants.COLOR);
        } else {
            ItemDataUtils.setInt(stack, NBTConstants.COLOR, color.ordinal());
        }
    }
}