package mekanism.common.item.interfaces;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;

public interface IRadialModeItem<TYPE extends Enum<TYPE> & IRadialSelectorEnum<TYPE>> extends IModeItem {

    Class<TYPE> getModeClass();

    default TYPE getDefaultMode() {
        return getModeClass().getEnumConstants()[0];
    }

    TYPE getModeByIndex(int ordinal);

    TYPE getMode(ItemStack stack);

    void setMode(ItemStack stack, PlayerEntity player, TYPE mode);
}
