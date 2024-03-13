package mekanism.common.inventory.container.tile;

import mekanism.api.security.IBlockSecurityUtils;
import mekanism.common.inventory.container.QIOItemViewerContainer;
import mekanism.common.network.PacketUtils;
import mekanism.common.network.to_server.PacketGuiInteract;
import mekanism.common.network.to_server.PacketGuiInteract.GuiInteraction;
import mekanism.common.registries.MekanismContainerTypes;
import mekanism.common.tile.qio.TileEntityQIODashboard;
import mekanism.common.util.WorldUtils;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class QIODashboardContainer extends QIOItemViewerContainer {

    private final TileEntityQIODashboard tile;

    public QIODashboardContainer(int id, Inventory inv, TileEntityQIODashboard tile, boolean remote) {
        super(MekanismContainerTypes.QIO_DASHBOARD, id, inv, remote, tile);
        this.tile = tile;
        tile.addContainerTrackers(this);
        addSlotsAndOpen();
    }

    @Override
    public QIODashboardContainer recreate() {
        QIODashboardContainer container = new QIODashboardContainer(containerId, inv, tile, true);
        sync(container);
        return container;
    }

    @Override
    protected void openInventory(@NotNull Inventory inv) {
        super.openInventory(inv);
        tile.open(inv.player);
    }

    @Override
    protected void closeInventory(@NotNull Player player) {
        super.closeInventory(player);
        tile.close(player);
    }

    @Override
    public boolean stillValid(@NotNull Player player) {
        //prevent Containers from remaining valid after the chunk has unloaded;
        return tile.hasGui() && !tile.isRemoved() && WorldUtils.isBlockLoaded(tile.getLevel(), tile.getBlockPos());
    }

    public TileEntityQIODashboard getTileEntity() {
        return tile;
    }

    @Override
    public boolean shiftClickIntoFrequency() {
        return tile.shiftClickIntoFrequency();
    }

    @Override
    public void toggleTargetDirection() {
        PacketUtils.sendToServer(new PacketGuiInteract(GuiInteraction.TARGET_DIRECTION_BUTTON, tile));
    }

    @Override
    public boolean canPlayerAccess(@NotNull Player player) {
        Level level = tile.getLevel();
        if (level == null) {
            return false;
        }
        return IBlockSecurityUtils.INSTANCE.canAccess(player, level, tile.getBlockPos(), tile);
    }
}
