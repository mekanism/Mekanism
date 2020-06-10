package mekanism.common.item.interfaces;

import mekanism.api.text.EnumColor;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

public interface IRadialModeItem extends IModeItem {

    Class<? extends IRadialSelectorEnum> getModeClass();

    IRadialSelectorEnum getMode(ItemStack stack);

    void setMode(ItemStack stack, PlayerEntity player, IRadialSelectorEnum mode);

    public interface IRadialSelectorEnum {

        ITextComponent getShortText();

        ResourceLocation getIcon();

        default EnumColor getColor() {
            return null;
        }

        int ordinal();
    }
}
