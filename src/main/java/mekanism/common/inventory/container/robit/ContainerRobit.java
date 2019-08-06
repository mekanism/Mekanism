package mekanism.common.inventory.container.robit;

import javax.annotation.Nonnull;
import mekanism.common.entity.EntityRobit;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.Slot;

public abstract class ContainerRobit extends Container {

    public EntityRobit robit;

    protected ContainerRobit(EntityRobit robit, PlayerInventory inventory) {
        this.robit = robit;
        robit.openInventory(inventory.player);
        addSlots();
        addInventorySlots(inventory);
    }

    protected abstract void addSlots();

    protected void addInventorySlots(PlayerInventory inventory) {
        for (int slotY = 0; slotY < 3; slotY++) {
            for (int slotX = 0; slotX < 9; slotX++) {
                addSlotToContainer(new Slot(inventory, slotX + slotY * 9 + 9, 8 + slotX * 18, 84 + slotY * 18));
            }
        }
        for (int slotY = 0; slotY < 9; slotY++) {
            addSlotToContainer(new Slot(inventory, slotY, 8 + slotY * 18, 142));
        }
    }

    @Override
    public void onContainerClosed(PlayerEntity entityplayer) {
        super.onContainerClosed(entityplayer);
        robit.closeInventory(entityplayer);
    }

    @Override
    public boolean canInteractWith(@Nonnull PlayerEntity entityplayer) {
        return !robit.isDead;
    }
}