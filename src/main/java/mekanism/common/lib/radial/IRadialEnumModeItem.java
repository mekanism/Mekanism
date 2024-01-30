package mekanism.common.lib.radial;

import mekanism.api.IIncrementalEnum;
import mekanism.api.radial.mode.IRadialMode;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.attachment.AttachmentType;

public interface IRadialEnumModeItem<MODE extends Enum<MODE> & IIncrementalEnum<MODE> & IRadialMode> extends IRadialModeItem<MODE> {

    AttachmentType<MODE> getModeAttachment();

    @Override
    default MODE getMode(ItemStack stack) {
        return stack.getData(getModeAttachment());
    }

    @Override
    default void setMode(ItemStack stack, Player player, MODE mode) {
        stack.setData(getModeAttachment(), mode);
    }
}