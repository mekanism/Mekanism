package mekanism.common.item.interfaces;

import mekanism.common.util.ItemAccessUtils;
import net.minecraft.core.TypedInstance;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.transfer.access.ItemAccess;

public interface IChemicalItem {

    boolean hasChemical(ItemAccess itemAccess);

    default <ITEM extends TypedInstance<Item> & DataComponentGetter> boolean hasChemical(ITEM instance) {
        return hasChemical(ItemAccessUtils.queryOnlyAccess(instance));
    }
}