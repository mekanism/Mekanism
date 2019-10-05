package mekanism.common.inventory.container;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.inventory.container.Slot;

public abstract class MekanismContainer extends Container {

    @Nullable
    protected final PlayerInventory inv;

    protected MekanismContainer(@Nullable ContainerType<?> type, int id, @Nullable PlayerInventory inv) {
        super(type, id);
        this.inv = inv;
    }

    /**
     * Adds slots and opens, must be called at end of extending classes constructors
     */
    protected void addSlotsAndOpen() {
        addSlots();
        if (inv != null) {
            addInventorySlots(inv);
            openInventory(inv);
        }
    }

    @Override
    public boolean canInteractWith(@Nonnull PlayerEntity player) {
        //Is this the proper default
        return true;
    }

    @Override
    public void onContainerClosed(PlayerEntity player) {
        super.onContainerClosed(player);
        closeInventory(player);
    }

    protected void closeInventory(PlayerEntity player) {
    }

    protected void openInventory(@Nonnull PlayerInventory inv) {
    }

    protected int getInventoryYOffset() {
        return 84;
    }

    protected int getInventoryXOffset() {
        return 8;
    }

    protected void addInventorySlots(@Nonnull PlayerInventory inv) {
        if (this instanceof IEmptyContainer) {
            //Don't include the player's inventory slots
            return;
        }
        int yOffset = getInventoryYOffset();
        int xOffset = getInventoryXOffset();
        for (int slotY = 0; slotY < 3; slotY++) {
            for (int slotX = 0; slotX < 9; slotX++) {
                addSlot(new Slot(inv, slotX + slotY * 9 + 9, xOffset + slotX * 18, yOffset + slotY * 18));
            }
        }
        yOffset += 58;
        for (int slotY = 0; slotY < 9; slotY++) {
            addSlot(new Slot(inv, slotY, xOffset + slotY * 18, yOffset));
        }
    }

    protected void addSlots() {
    }
}