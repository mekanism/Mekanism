package mekanism.common.inventory.container.item;

import javax.annotation.Nonnull;
import mekanism.common.item.ItemDictionary;
import mekanism.common.registries.MekanismContainerTypes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Hand;

public class DictionaryContainer extends MekanismItemContainer {

    public DictionaryContainer(int id, PlayerInventory inv, Hand hand, ItemStack stack) {
        super(MekanismContainerTypes.DICTIONARY, id, inv, hand, stack);
    }

    public DictionaryContainer(int id, PlayerInventory inv, PacketBuffer buf) {
        this(id, inv, buf.readEnumValue(Hand.class), getStackFromBuffer(buf, ItemDictionary.class));
    }

    @Override
    protected int getInventoryYOffset() {
        return super.getInventoryYOffset() + 5;
    }

    @Nonnull
    @Override
    public ItemStack transferStackInSlot(@Nonnull PlayerEntity player, int slotID) {
        Slot slot = inventorySlots.get(slotID);
        return slot == null ? ItemStack.EMPTY : slot.getStack();
    }
}