package mekanism.common.inventory.container.robit;

import javax.annotation.Nonnull;
import mekanism.common.entity.EntityRobit;
import mekanism.common.inventory.slot.SlotEnergy.SlotDischarge;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;

public class ContainerRobitMain extends ContainerRobit {

    public ContainerRobitMain(InventoryPlayer inventory, EntityRobit entity) {
        super(entity, inventory);
    }

    @Nonnull
    @Override
    public ItemStack transferStackInSlot(EntityPlayer player, int slotID) {
        return ItemStack.EMPTY;
    }

    @Override
    protected void addSlots() {
        addSlotToContainer(new SlotDischarge(robit, 27, 153, 17));
    }
}
