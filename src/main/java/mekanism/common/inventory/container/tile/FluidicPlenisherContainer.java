package mekanism.common.inventory.container.tile;

import mekanism.common.registries.MekanismContainerTypes;
import mekanism.common.tile.TileEntityFluidicPlenisher;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.network.PacketBuffer;

public class FluidicPlenisherContainer extends MekanismTileContainer<TileEntityFluidicPlenisher> {

    public FluidicPlenisherContainer(int id, PlayerInventory inv, TileEntityFluidicPlenisher tile) {
        super(MekanismContainerTypes.FLUIDIC_PLENISHER, id, inv, tile);
    }

    public FluidicPlenisherContainer(int id, PlayerInventory inv, PacketBuffer buf) {
        this(id, inv, getTileFromBuf(buf, TileEntityFluidicPlenisher.class));
    }
}