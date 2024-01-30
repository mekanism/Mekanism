package mekanism.common.lib.radial;

import mekanism.api.radial.RadialData;
import mekanism.api.radial.mode.IRadialMode;
import mekanism.common.item.interfaces.IModeItem.IAttachmentBasedModeItem;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface IRadialModeItem<MODE extends IRadialMode> extends IGenericRadialModeItem, IAttachmentBasedModeItem<MODE> {

    @NotNull
    @Override
    RadialData<MODE> getRadialData(ItemStack stack);

    @Nullable
    @Override
    default <M extends IRadialMode> M getMode(ItemStack stack, RadialData<M> radialData) {
        return radialData == getRadialData(stack) ? (M) getMode(stack) : null;
    }

    @Override
    default <M extends IRadialMode> void setMode(ItemStack stack, Player player, RadialData<M> radialData, M mode) {
        if (radialData == getRadialData(stack)) {
            setMode(stack, player, (MODE) mode);
        }
    }
}