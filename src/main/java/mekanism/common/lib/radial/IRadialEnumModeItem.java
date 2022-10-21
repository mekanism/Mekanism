package mekanism.common.lib.radial;

import mekanism.api.IIncrementalEnum;
import mekanism.api.radial.mode.IRadialMode;
import mekanism.common.util.ItemDataUtils;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public interface IRadialEnumModeItem<MODE extends Enum<MODE> & IIncrementalEnum<MODE> & IRadialMode> extends IRadialModeItem<MODE> {

    String getModeSaveKey();

    MODE getModeByIndex(int ordinal);

    @Override
    default MODE getMode(ItemStack stack) {
        return getModeByIndex(ItemDataUtils.getInt(stack, getModeSaveKey()));
    }

    @Override
    default void setMode(ItemStack stack, Player player, MODE mode) {
        ItemDataUtils.setInt(stack, getModeSaveKey(), mode.ordinal());
    }
}