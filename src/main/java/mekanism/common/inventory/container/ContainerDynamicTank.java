package mekanism.common.inventory.container;

import mekanism.common.tile.TileEntityDynamicTank;
import net.minecraft.entity.player.InventoryPlayer;

public class ContainerDynamicTank extends ContainerFluidStorage {
    public ContainerDynamicTank(InventoryPlayer inventory, TileEntityDynamicTank tank) {
        super(inventory, tank, 20);
    }
}