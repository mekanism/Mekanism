package mekanism.common.inventory.container.tile;

import java.util.List;
import java.util.Objects;
import mekanism.api.inventory.IInventorySlot;
import mekanism.api.security.IBlockSecurityUtils;
import mekanism.common.inventory.container.IEmptyContainer;
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.inventory.container.slot.VirtualInventoryContainerSlot;
import mekanism.common.registration.impl.ContainerTypeRegistryObject;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.component.TileComponentUpgrade;
import mekanism.common.util.WorldUtils;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

public class MekanismTileContainer<TILE extends TileEntityMekanism> extends MekanismContainer {

    @Nullable
    private VirtualInventoryContainerSlot upgradeSlot;
    @Nullable
    private VirtualInventoryContainerSlot upgradeOutputSlot;
    protected final TILE tile;

    public MekanismTileContainer(ContainerTypeRegistryObject<?> type, int id, Inventory inv, TILE tile) {
        this.tile = tile;
        super(type, id, inv);
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
    public boolean canPlayerAccess(Player player) {
        Level level = tile.getLevel();
        if (level == null) {
            return false;
        }
        return IBlockSecurityUtils.INSTANCE.canAccess(player, level, tile.getBlockPos(), tile);
    }

    @Override
    protected void openInventory(Inventory inv) {
        super.openInventory(inv);
        tile.open(inv.player);
    }

    @Override
    protected void closeInventory(Player player) {
        super.closeInventory(player);
        tile.close(player);
    }

    @Override
    public boolean stillValid(Player player) {
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
            TileComponentUpgrade upgradeComponent = Objects.requireNonNull(tile.getComponent(), "Upgrade component should be present");
            //Add the virtual slot for the upgrade (add them before the main inventory to make sure they take priority in targeting)
            addSlot(upgradeSlot = upgradeComponent.getUpgradeSlot().createContainerSlot());
            addSlot(upgradeOutputSlot = upgradeComponent.getUpgradeOutputSlot().createContainerSlot());
        }
        if (tile.hasInventory()) {
            //Get all the inventory slots the tile has
            List<IInventorySlot> inventorySlots = tile.getInventorySlots();
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