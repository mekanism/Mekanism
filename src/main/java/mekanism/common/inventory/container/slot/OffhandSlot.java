package mekanism.common.inventory.container.slot;

import net.minecraft.world.Container;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class OffhandSlot extends InsertableSlot {

    private final Player owner;

    public OffhandSlot(Container inventory, int index, int x, int y, Player owner) {
        super(inventory, index, x, y);
        this.owner = owner;
        setBackground(InventoryMenu.BLOCK_ATLAS, InventoryMenu.EMPTY_ARMOR_SLOT_SHIELD);
    }

    @Override
    public void setByPlayer(@NotNull ItemStack newStack, @NotNull ItemStack oldStack) {
        this.owner.onEquipItem(EquipmentSlot.OFFHAND, oldStack, newStack);
        super.setByPlayer(newStack, oldStack);
    }
}