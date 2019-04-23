package mekanism.common.inventory.container;

import mekanism.common.tile.prefab.TileEntityContainerBlock;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;

public class ContainerNull extends ContainerMekanism<TileEntityContainerBlock> {

    public ContainerNull(EntityPlayer player, TileEntityContainerBlock tile) {
        super(tile, player == null ? null : player.inventory);
    }

    public ContainerNull(TileEntityContainerBlock tile) {
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
