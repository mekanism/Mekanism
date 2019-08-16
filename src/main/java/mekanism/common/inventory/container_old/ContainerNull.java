package mekanism.common.inventory.container_old;

import mekanism.common.tile.base.TileEntityMekanism;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;

public class ContainerNull extends ContainerMekanism<TileEntityMekanism> {

    public ContainerNull(PlayerEntity player, TileEntityMekanism tile) {
        super(tile, player == null ? null : player.inventory);
    }

    public ContainerNull(TileEntityMekanism tile) {
        this(null, tile);
    }

    public ContainerNull() {
        this(null, null);
    }

    @Override
    protected void addSlots() {
    }

    @Override
    protected void addInventorySlots(PlayerInventory inventory) {
    }
}