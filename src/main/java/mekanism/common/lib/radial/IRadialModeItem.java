package mekanism.common.lib.radial;

import mekanism.api.radial.RadialData;
import mekanism.api.radial.mode.IRadialMode;
import mekanism.common.item.interfaces.IModeItem.IAttachmentBasedModeItem;
import net.minecraft.core.TypedInstance;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jspecify.annotations.Nullable;

public interface IRadialModeItem<MODE extends IRadialMode> extends IGenericRadialModeItem, IAttachmentBasedModeItem<MODE> {

    @Override
    <ITEM extends TypedInstance<Item> & DataComponentGetter> RadialData<MODE> getRadialData(ITEM instance);

    @Nullable
    @Override
    default <ITEM extends TypedInstance<Item> & DataComponentGetter, M extends IRadialMode> M getMode(ITEM instance, RadialData<M> radialData) {
        return radialData == getRadialData(instance) ? (M) getMode(instance) : null;
    }

    @Override
    default <M extends IRadialMode> void setMode(ItemAccess itemAccess, Player player, RadialData<M> radialData, M mode, @Nullable TransactionContext transaction) {
        if (radialData == getRadialData(itemAccess.getResource())) {
            setMode(itemAccess, player, (MODE) mode, transaction);
        }
    }
}