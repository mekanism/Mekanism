package mekanism.common.inventory.container;

import mekanism.common.tile.TileEntityFluidTank;
import net.minecraft.entity.player.InventoryPlayer;

public class ContainerFluidTank extends ContainerFluidStorage {

    public ContainerFluidTank(InventoryPlayer inventory, TileEntityFluidTank tank) {
        super(inventory, tank, 19);
    }
}