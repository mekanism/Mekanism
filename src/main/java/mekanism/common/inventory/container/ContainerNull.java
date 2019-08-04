package mekanism.common.inventory.container;

import mekanism.common.tile.base.TileEntityMekanism;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;

public class ContainerNull extends ContainerMekanism<TileEntityMekanism> {

    public ContainerNull(EntityPlayer player, TileEntityMekanism tile) {
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
    protected void addInventorySlots(InventoryPlayer inventory) {
    }
}