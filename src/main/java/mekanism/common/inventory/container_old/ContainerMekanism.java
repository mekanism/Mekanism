package mekanism.common.inventory.container_old;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.common.tile.base.TileEntityMekanism;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.inventory.container.Slot;

public abstract class ContainerMekanism<TILE extends TileEntityMekanism> extends Container {

    //TODO: Ensure things work properly this way
    @Nullable
    protected TILE tile;

    protected ContainerMekanism(int windowID, PlayerInventory inv, ContainerType type, @Nullable TILE tile) {
        super(type, windowID);
        this.tile = tile;
        if (shouldAddSlots()) {
            addSlots();
            if (inv != null) {
                addInventorySlots(inv);
                openInventory(inv);
            }
        }
    }

    //TODO
    protected ContainerMekanism(TILE tile, PlayerInventory inv) {
        this(1, inv, null, tile);
    }

    protected int getInventoryOffset() {
        return 84;
    }

    protected void addInventorySlots(PlayerInventory inventory) {
        int offset = getInventoryOffset();
        for (int slotY = 0; slotY < 3; slotY++) {
            for (int slotX = 0; slotX < 9; slotX++) {
                addSlot(new Slot(inventory, slotX + slotY * 9 + 9, 8 + slotX * 18, offset + slotY * 18));
            }
        }
        offset += 58;
        for (int slotY = 0; slotY < 9; slotY++) {
            addSlot(new Slot(inventory, slotY, 8 + slotY * 18, offset));
        }
    }

    protected boolean shouldAddSlots() {
        return true;
    }

    protected abstract void addSlots();

    protected void openInventory(PlayerInventory inventory) {
        if (tile != null) {
            tile.open(inventory.player);
            tile.openInventory(inventory.player);
        }
    }

    @Override
    public void onContainerClosed(PlayerEntity entityplayer) {
        super.onContainerClosed(entityplayer);
        closeInventory(entityplayer);
    }

    protected void closeInventory(PlayerEntity entityplayer) {
        if (tile != null) {
            tile.close(entityplayer);
            tile.closeInventory(entityplayer);
        }
    }

    @Override
    public boolean canInteractWith(@Nonnull PlayerEntity entityplayer) {
        return tile == null || tile.isUsableByPlayer(entityplayer);
    }
}