package mekanism.common.inventory.container.tile;

import mekanism.common.registries.MekanismContainerTypes;
import mekanism.common.tile.qio.TileEntityQIOExporter;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.network.PacketBuffer;

public class QIOExporterContainer extends MekanismTileContainer<TileEntityQIOExporter> {

    public QIOExporterContainer(int id, PlayerInventory inv, TileEntityQIOExporter tile) {
        super(MekanismContainerTypes.QIO_EXPORTER, id, inv, tile);
    }

    public QIOExporterContainer(int id, PlayerInventory inv, PacketBuffer buf) {
        this(id, inv, getTileFromBuf(buf, TileEntityQIOExporter.class));
    }

    @Override
    protected int getInventoryYOffset() {
        return super.getInventoryYOffset() + 74;
    }
}