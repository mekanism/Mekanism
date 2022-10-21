package mekanism.common.inventory.container.entity.robit;

import java.util.List;
import mekanism.api.inventory.IInventorySlot;
import mekanism.common.entity.EntityRobit;
import mekanism.common.inventory.container.entity.MekanismEntityContainer;
import mekanism.common.registration.impl.ContainerTypeRegistryObject;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import org.jetbrains.annotations.NotNull;

public class RobitContainer extends MekanismEntityContainer<EntityRobit> {

    public RobitContainer(ContainerTypeRegistryObject<?> type, int id, Inventory inv, EntityRobit robit) {
        super(type, id, inv, robit);
        robit.addContainerTrackers(this);
    }

    @Override
    protected void addSlots() {
        super.addSlots();
        if (entity.hasInventory()) {
            //Get all the inventory slots the entity has/exposes for this container type
            List<IInventorySlot> inventorySlots = entity.getContainerInventorySlots(getType());
            for (IInventorySlot inventorySlot : inventorySlots) {
                Slot containerSlot = inventorySlot.createContainerSlot();
                if (containerSlot != null) {
                    addSlot(containerSlot);
                }
            }
        }
    }

    @Override
    protected void openInventory(@NotNull Inventory inv) {
        super.openInventory(inv);
        entity.open(inv.player);
    }

    @Override
    protected void closeInventory(@NotNull Player player) {
        super.closeInventory(player);
        entity.close(player);
    }
}