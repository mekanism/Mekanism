package mekanism.common.inventory.container.entity.robit;

import java.util.List;
import javax.annotation.Nullable;
import mekanism.api.inventory.slot.IInventorySlot;
import mekanism.common.entity.EntityRobit;
import mekanism.common.inventory.container.entity.MekanismEntityContainer;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.inventory.container.Slot;

public abstract class RobitContainer extends MekanismEntityContainer<EntityRobit> {

    protected RobitContainer(@Nullable ContainerType<?> type, int id, @Nullable PlayerInventory inv, EntityRobit entity) {
        super(type, id, inv, entity);
    }

    @Override
    protected void addSlots() {
        super.addSlots();
        //TODO: Overwrite transferStackInSlot with the logic in the IInventorySlots??
        if (entity.hasInventory()) {
            //Get all the inventory slots the entity has/exposes for this container type
            //TODO: Check to make sure that the repair and crafting containers still work fine
            List<IInventorySlot> inventorySlots = entity.getInventorySlots(getType());
            for (IInventorySlot inventorySlot : inventorySlots) {
                Slot containerSlot = inventorySlot.createContainerSlot();
                if (containerSlot != null) {
                    addSlot(containerSlot);
                }
            }
        }
    }
}