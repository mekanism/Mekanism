package mekanism.common.inventory.container.tile;

import java.util.List;
import mekanism.api.inventory.IInventorySlot;
import mekanism.api.security.IBlockSecurityUtils;
import mekanism.common.inventory.container.IEmptyContainer;
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.inventory.container.slot.VirtualInventoryContainerSlot;
import mekanism.common.registration.impl.ContainerTypeRegistryObject;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.util.WorldUtils;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MekanismTileContainer<TILE extends TileEntityMekanism> extends MekanismContainer {

    private VirtualInventoryContainerSlot upgradeSlot;
    private VirtualInventoryContainerSlot upgradeOutputSlot;
    @NotNull
    protected final TILE tile;

    public MekanismTileContainer(ContainerTypeRegistryObject<?> type, int id, Inventory inv, @NotNull TILE tile) {
        super(type, id, inv);
        this.tile = tile;
        addContainerTrackers();
        addSlotsAndOpen();
    }

    protected void addContainerTrackers() {
        tile.addContainerTrackers(this);
    }

    public TILE getTileEntity() {
        return tile;
    }

    @Override
    public boolean canPlayerAccess(@NotNull Player player) {
        Level level = tile.getLevel();
        if (level == null) {
            return false;
        }
        return IBlockSecurityUtils.INSTANCE.canAccess(player, level, tile.getBlockPos(), tile);
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

    @Override
    protected void addSlots() {
        super.addSlots();
        if (this instanceof IEmptyContainer) {
            //Don't include the inventory slots
            return;
        }
        if (tile.supportsUpgrades()) {
            //Add the virtual slot for the upgrade (add them before the main inventory to make sure they take priority in targeting)
            addSlot(upgradeSlot = tile.getComponent().getUpgradeSlot().createContainerSlot());
            addSlot(upgradeOutputSlot = tile.getComponent().getUpgradeOutputSlot().createContainerSlot());
        }
        if (tile.hasInventory()) {
            //Get all the inventory slots the tile has
            List<IInventorySlot> inventorySlots = tile.getInventorySlots(null);
            for (IInventorySlot inventorySlot : inventorySlots) {
                Slot containerSlot = inventorySlot.createContainerSlot();
                if (containerSlot != null) {
                    addSlot(containerSlot);
                }
            }
        }
    }

    @Nullable
    public VirtualInventoryContainerSlot getUpgradeSlot() {
        return upgradeSlot;
    }

    @Nullable
    public VirtualInventoryContainerSlot getUpgradeOutputSlot() {
        return upgradeOutputSlot;
    }
}