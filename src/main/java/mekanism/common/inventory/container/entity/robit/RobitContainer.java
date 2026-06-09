package mekanism.common.inventory.container.entity.robit;

import mekanism.api.inventory.IInventorySlot;
import mekanism.common.entity.EntityRobit;
import mekanism.common.inventory.container.entity.MekanismEntityContainer;
import mekanism.common.registration.impl.ContainerTypeRegistryObject;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;

public class RobitContainer extends MekanismEntityContainer<EntityRobit> {

    public RobitContainer(ContainerTypeRegistryObject<?> type, int id, Inventory inv, EntityRobit robit) {
        super(type, id, inv, robit);
        robit.addContainerTrackers(this);
    }

    @Override
    protected void addSlots() {
        super.addSlots();
        //Get all the inventory slots the entity has/exposes for this container type
        for (IInventorySlot inventorySlot : entity.getContainerInventorySlots(getType())) {
            Slot containerSlot = inventorySlot.createContainerSlot();
            if (containerSlot != null) {
                addSlot(containerSlot);
            }
        }
    }

    @Override
    protected void openInventory(Inventory inv) {
        super.openInventory(inv);
        entity.open(inv.player);
    }

    @Override
    protected void closeInventory(Player player) {
        super.closeInventory(player);
        entity.close(player);
    }
}