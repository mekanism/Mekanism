package mekanism.common.lib.radial;

import mekanism.api.radial.RadialData;
import mekanism.api.radial.mode.IRadialMode;
import mekanism.common.item.interfaces.IModeItem;
import net.minecraft.core.TypedInstance;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jspecify.annotations.Nullable;

public interface IGenericRadialModeItem extends IModeItem {

    /// @return Current radial data or `null` if this item doesn't currently have a radial to display.
    @Nullable
    <ITEM extends TypedInstance<Item> & DataComponentGetter> RadialData<?> getRadialData(ITEM instance);

    @Nullable <ITEM extends TypedInstance<Item> & DataComponentGetter, M extends IRadialMode> M getMode(ITEM instance, RadialData<M> radialData);

    <M extends IRadialMode> void setMode(ItemAccess itemAccess, Player player, RadialData<M> radialData, M mode, @Nullable TransactionContext transaction);
}