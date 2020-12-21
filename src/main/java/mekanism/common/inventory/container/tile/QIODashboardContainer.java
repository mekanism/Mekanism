package mekanism.common.inventory.container.tile;

import javax.annotation.Nonnull;
import mekanism.common.content.qio.QIOFrequency;
import mekanism.common.inventory.container.QIOItemViewerContainer;
import mekanism.common.lib.frequency.FrequencyType;
import mekanism.common.lib.security.ISecurityObject;
import mekanism.common.registries.MekanismContainerTypes;
import mekanism.common.tile.qio.TileEntityQIODashboard;
import mekanism.common.util.WorldUtils;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.network.PacketBuffer;

public class QIODashboardContainer extends QIOItemViewerContainer {

    private final TileEntityQIODashboard tile;

    private QIODashboardContainer(int id, PlayerInventory inv, TileEntityQIODashboard tile, boolean remote) {
        super(MekanismContainerTypes.QIO_DASHBOARD, id, inv, remote);
        this.tile = tile;
        if (tile != null) {
            tile.addContainerTrackers(this);
        }
        addSlotsAndOpen();
    }

    /**
     * @apiNote Call from the server
     */
    public QIODashboardContainer(int id, PlayerInventory inv, TileEntityQIODashboard tile) {
        this(id, inv, tile, false);
    }

    /**
     * @apiNote Call from the client
     */
    public QIODashboardContainer(int id, PlayerInventory inv, PacketBuffer buf) {
        this(id, inv, MekanismTileContainer.getTileFromBuf(buf, TileEntityQIODashboard.class), true);
    }

    @Override
    public QIODashboardContainer recreate() {
        QIODashboardContainer container = new QIODashboardContainer(windowId, inv, tile);
        sync(container);
        return container;
    }

    @Override
    public QIOFrequency getFrequency() {
        return tile.getFrequency(FrequencyType.QIO);
    }

    @Override
    protected void openInventory(@Nonnull PlayerInventory inv) {
        super.openInventory(inv);
        if (tile != null) {
            tile.open(inv.player);
        }
    }

    @Override
    protected void closeInventory(@Nonnull PlayerEntity player) {
        super.closeInventory(player);
        if (tile != null) {
            tile.close(player);
        }
    }

    @Override
    public boolean canInteractWith(@Nonnull PlayerEntity player) {
        if (tile == null) {
            return true;
        }
        //prevent Containers from remaining valid after the chunk has unloaded;
        return tile.hasGui() && !tile.isRemoved() && WorldUtils.isBlockLoaded(tile.getWorld(), tile.getPos());
    }

    public TileEntityQIODashboard getTileEntity() {
        return tile;
    }

    @Override
    public ISecurityObject getSecurityObject() {
        return tile;
    }
}
