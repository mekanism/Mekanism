package mekanism.common.inventory.container.entity.robit;

import java.util.List;
import javax.annotation.Nullable;
import mekanism.api.inventory.IInventorySlot;
import mekanism.common.entity.EntityRobit;
import mekanism.common.inventory.container.entity.MekanismEntityContainer;
import mekanism.common.registration.impl.ContainerTypeRegistryObject;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Slot;

public abstract class RobitContainer extends MekanismEntityContainer<EntityRobit> {

    protected RobitContainer(ContainerTypeRegistryObject<?> type, int id, @Nullable PlayerInventory inv, EntityRobit robit) {
        super(type, id, inv, robit);
        robit.addContainerTrackers(getType(), this);
    }

    @Override
    protected void addSlots() {
        super.addSlots();
        //TODO: Overwrite transferStackInSlot with the logic in the IInventorySlots??
        if (entity.hasInventory()) {
            //Get all the inventory slots the entity has/exposes for this container type
            //TODO: Check to make sure that the repair and crafting containers still work fine
            List<IInventorySlot> inventorySlots = entity.getContainerInventorySlots(getType());
            for (IInventorySlot inventorySlot : inventorySlots) {
                Slot containerSlot = inventorySlot.createContainerSlot();
                if (containerSlot != null) {
                    addSlot(containerSlot);
                }
            }
        }
    }
}