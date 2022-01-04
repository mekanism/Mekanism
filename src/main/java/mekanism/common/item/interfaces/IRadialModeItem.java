package mekanism.common.item.interfaces;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public interface IRadialModeItem<TYPE extends Enum<TYPE> & IRadialSelectorEnum<TYPE>> extends IModeItem {

    Class<TYPE> getModeClass();

    default TYPE getDefaultMode() {
        return getModeClass().getEnumConstants()[0];
    }

    TYPE getModeByIndex(int ordinal);

    TYPE getMode(ItemStack stack);

    void setMode(ItemStack stack, Player player, TYPE mode);
}
