package mekanism.common.inventory.container.tile.fluid;

import mekanism.common.inventory.container.MekanismContainerTypes;
import mekanism.common.inventory.slot.SlotOutput;
import mekanism.common.tile.fluid_tank.TileEntityFluidTank;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Slot;
import net.minecraft.network.PacketBuffer;

public class FluidTankContainer extends MekanismFluidContainer<TileEntityFluidTank> {

    public FluidTankContainer(int id, PlayerInventory inv, TileEntityFluidTank tile) {
        super(MekanismContainerTypes.FLUID_TANK, id, inv, tile);
    }

    public FluidTankContainer(int id, PlayerInventory inv, PacketBuffer buf) {
        this(id, inv, getTileFromBuf(buf, TileEntityFluidTank.class));
    }

    @Override
    protected void addSlots() {
        addSlot(new Slot(tile, 0, 146, 19));
        addSlot(new SlotOutput(tile, 1, 146, 51));
    }
}