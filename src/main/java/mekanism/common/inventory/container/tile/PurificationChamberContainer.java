package mekanism.common.inventory.container.tile;

import mekanism.common.registries.MekanismContainerTypes;
import mekanism.common.tile.TileEntityPurificationChamber;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.network.PacketBuffer;

public class PurificationChamberContainer extends MekanismTileContainer<TileEntityPurificationChamber> {

    public PurificationChamberContainer(int id, PlayerInventory inv, TileEntityPurificationChamber tile) {
        super(MekanismContainerTypes.PURIFICATION_CHAMBER, id, inv, tile);
    }

    public PurificationChamberContainer(int id, PlayerInventory inv, PacketBuffer buf) {
        this(id, inv, getTileFromBuf(buf, TileEntityPurificationChamber.class));
    }
}