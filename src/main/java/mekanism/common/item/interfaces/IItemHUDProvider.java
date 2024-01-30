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
        //Note: We use the passed in stack rather than this instance so that if we implement the hud provider
        // on a non armor (such as for modules) then it can still forward the call if the module container is an armor item
        if (stack.getItem() instanceof ArmorItem armor) {
            addHUDStrings(list, player, stack, armor.getEquipmentSlot());
        }
    }
}