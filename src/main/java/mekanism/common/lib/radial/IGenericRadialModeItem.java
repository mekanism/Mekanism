package mekanism.common.lib.radial;

import mekanism.api.radial.RadialData;
import mekanism.api.radial.mode.IRadialMode;
import mekanism.common.item.interfaces.IModeItem;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public interface IGenericRadialModeItem extends IModeItem {

    /**
     * @return Current radial data or {@code null} if this item doesn't currently have a radial to display.
     */
    @Nullable
    RadialData<?> getRadialData(ItemStack stack);

    @Nullable <M extends IRadialMode> M getMode(ItemStack stack, RadialData<M> radialData);

    <M extends IRadialMode> void setMode(ItemStack stack, Player player, RadialData<M> radialData, M mode);
}