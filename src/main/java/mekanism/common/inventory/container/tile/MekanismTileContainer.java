package mekanism.common.inventory.container.tile;

import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.inventory.IInventorySlot;
import mekanism.common.inventory.container.IEmptyContainer;
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.inventory.container.slot.VirtualInventoryContainerSlot;
import mekanism.common.lib.security.ISecurityObject;
import mekanism.common.registration.impl.ContainerTypeRegistryObject;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.util.WorldUtils;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Slot;

public class MekanismTileContainer<TILE extends TileEntityMekanism> extends MekanismContainer {

    private VirtualInventoryContainerSlot upgradeSlot;
    private VirtualInventoryContainerSlot upgradeOutputSlot;
    @Nonnull
    protected final TILE tile;

    public MekanismTileContainer(ContainerTypeRegistryObject<?> type, int id, PlayerInventory inv, @Nonnull TILE tile) {
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
    public ISecurityObject getSecurityObject() {
        return getTileEntity();
    }

    @Override
    protected void openInventory(@Nonnull PlayerInventory inv) {
        super.openInventory(inv);
        tile.open(inv.player);
    }

    @Override
    protected void closeInventory(@Nonnull PlayerEntity player) {
        super.closeInventory(player);
        tile.close(player);
    }

    @Override
    public boolean stillValid(@Nonnull PlayerEntity player) {
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