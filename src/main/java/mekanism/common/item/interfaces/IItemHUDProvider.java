package mekanism.common.item.interfaces;

import java.util.List;
import net.minecraft.core.TypedInstance;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.equipment.Equippable;

public interface IItemHUDProvider {

    <ITEM extends TypedInstance<Item> & DataComponentGetter> void addHUDStrings(List<Component> list, Player player, ITEM instance, EquipmentSlot slotType);

    default <ITEM extends TypedInstance<Item> & DataComponentGetter> void addCurioHUDStrings(List<Component> list, Player player, ITEM instance) {
        //Note: We use the passed in stack rather than this instance so that if we implement the hud provider
        // on a non armor (such as for modules) then it can still forward the call if the module container is an armor item
        Equippable equippable = instance.get(DataComponents.EQUIPPABLE);
        if (equippable != null && equippable.slot().getType() == EquipmentSlot.Type.HUMANOID_ARMOR) {
            addHUDStrings(list, player, instance, equippable.slot());
        }
    }
}