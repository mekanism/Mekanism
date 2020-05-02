package mekanism.common.inventory.container.tile;

import mekanism.common.content.qio.TileEntityQIODriveArray;
import mekanism.common.registries.MekanismContainerTypes;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.network.PacketBuffer;

public class QIODriveArrayContainer extends MekanismTileContainer<TileEntityQIODriveArray> {

    public QIODriveArrayContainer(int id, PlayerInventory inv, TileEntityQIODriveArray tile) {
        super(MekanismContainerTypes.QIO_DRIVE_ARRAY, id, inv, tile);
    }

    public QIODriveArrayContainer(int id, PlayerInventory inv, PacketBuffer buf) {
        this(id, inv, getTileFromBuf(buf, TileEntityQIODriveArray.class));
    }

    @Override
    protected int getInventoryYOffset() {
        return super.getInventoryYOffset() + 40;
    }
}