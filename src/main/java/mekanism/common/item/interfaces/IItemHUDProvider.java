package mekanism.common.item.interfaces;

import java.util.List;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;

public interface IItemHUDProvider {

    void addHUDStrings(List<ITextComponent> list, PlayerEntity player, ItemStack stack, EquipmentSlotType slotType);
}