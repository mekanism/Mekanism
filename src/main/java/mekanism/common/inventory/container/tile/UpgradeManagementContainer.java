package mekanism.common.inventory.container.tile;

import mekanism.common.inventory.container.slot.InventoryContainerSlot;
import mekanism.common.registries.MekanismContainerTypes;
import mekanism.common.tile.base.TileEntityMekanism;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.network.PacketBuffer;

public class UpgradeManagementContainer extends MekanismTileContainer<TileEntityMekanism> {

    public UpgradeManagementContainer(int id, PlayerInventory inv, TileEntityMekanism tile) {
        super(MekanismContainerTypes.UPGRADE_MANAGEMENT, id, inv, tile);
    }

    public UpgradeManagementContainer(int id, PlayerInventory inv, PacketBuffer buf) {
        this(id, inv, getTileFromBuf(buf, TileEntityMekanism.class));
    }

    @Override
    protected void addContainerTrackers() {
        if (tile != null && tile.supportsUpgrades()) {
            //Make sure just in case that it really does support upgrades
            tile.getComponent().addContainerTrackers(this);
        }
    }

    @Override
    protected void addSlots() {
        //Add the upgrade slot
        if (tile != null && tile.supportsUpgrades()) {
            InventoryContainerSlot containerSlot = tile.getComponent().getUpgradeSlot().createContainerSlot();
            if (containerSlot != null) {
                addSlot(containerSlot);
            }
        }
    }
}