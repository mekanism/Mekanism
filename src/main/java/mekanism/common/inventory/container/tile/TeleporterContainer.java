package mekanism.common.inventory.container.tile;

import mekanism.common.inventory.container.MekanismContainerTypes;
import mekanism.common.tile.TileEntityTeleporter;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.network.PacketBuffer;

public class TeleporterContainer extends MekanismTileContainer<TileEntityTeleporter> {

    public TeleporterContainer(int id, PlayerInventory inv, TileEntityTeleporter tile) {
        super(MekanismContainerTypes.TELEPORTER, id, inv, tile);
    }

    public TeleporterContainer(int id, PlayerInventory inv, PacketBuffer buf) {
        this(id, inv, getTileFromBuf(buf, TileEntityTeleporter.class));
    }

    @Override
    protected int getInventoryYOffset() {
        return 148;
    }
}