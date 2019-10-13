package mekanism.common.inventory.container.entity.robit;

import javax.annotation.Nonnull;
import mekanism.common.entity.EntityRobit;
import mekanism.common.inventory.container.MekanismContainerTypes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;

public class MainRobitContainer extends RobitContainer {

    public MainRobitContainer(int id, PlayerInventory inv, EntityRobit robit) {
        super(MekanismContainerTypes.MAIN_ROBIT, id, inv, robit);
    }

    public MainRobitContainer(int id, PlayerInventory inv, PacketBuffer buf) {
        this(id, inv, getEntityFromBuf(buf, EntityRobit.class));
    }

    @Nonnull
    @Override
    public ItemStack transferStackInSlot(PlayerEntity player, int slotID) {
        return ItemStack.EMPTY;
    }

    @Override
    protected void addSlots() {
        addSlot(new SlotDischarge(entity, 27, 153, 17));
    }
}