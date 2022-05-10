package mekanism.common.item.interfaces;

import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;

public interface IItemHUDProvider {

    void addHUDStrings(List<Component> list, Player player, ItemStack stack, EquipmentSlot slotType);

    default void addCurioHUDStrings(List<Component> list, Player player, ItemStack stack) {
        if (this instanceof ArmorItem armor) {
            addHUDStrings(list, player, stack, armor.getSlot());
        }
    }
}