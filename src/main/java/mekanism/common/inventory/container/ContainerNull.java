package mekanism.common.inventory.container;

import mekanism.common.tile.base.TileEntityContainer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;

public class ContainerNull extends ContainerMekanism<TileEntityContainer> {

    public ContainerNull(EntityPlayer player, TileEntityContainer tile) {
        super(tile, player == null ? null : player.inventory);
    }

    public ContainerNull(TileEntityContainer tile) {
        this(null, tile);
    }

    public ContainerNull() {
        this(null, null);
    }

    @Override
    protected void addSlots() {
    }

    @Override
    protected void addInventorySlots(InventoryPlayer inventory) {
    }
}