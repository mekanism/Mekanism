package mekanism.common.inventory.container.item;

import java.util.List;
import javax.annotation.Nonnull;
import mekanism.api.inventory.IInventorySlot;
import mekanism.common.inventory.InventoryPersonalChest;
import mekanism.common.inventory.container.slot.HotBarSlot;
import mekanism.common.item.block.ItemBlockPersonalChest;
import mekanism.common.registries.MekanismContainerTypes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Hand;

public class PersonalChestItemContainer extends MekanismItemContainer {

    private InventoryPersonalChest itemInventory;

    public PersonalChestItemContainer(int id, PlayerInventory inv, Hand hand, ItemStack stack) {
        super(MekanismContainerTypes.PERSONAL_CHEST_ITEM, id, inv, hand, stack);
    }

    public PersonalChestItemContainer(int id, PlayerInventory inv, PacketBuffer buf) {
        this(id, inv, buf.readEnumValue(Hand.class), getStackFromBuffer(buf, ItemBlockPersonalChest.class));
    }

    @Override
    protected void addSlotsAndOpen() {
        //We have to initialize this before actually adding the slots
        itemInventory = new InventoryPersonalChest(stack);
        super.addSlotsAndOpen();
    }

    @Override
    protected void addSlots() {
        super.addSlots();
        //Get all the inventory slots the tile has
        List<IInventorySlot> inventorySlots = itemInventory.getInventorySlots(null);
        for (IInventorySlot inventorySlot : inventorySlots) {
            Slot containerSlot = inventorySlot.createContainerSlot();
            if (containerSlot != null) {
                addSlot(containerSlot);
            }
        }
    }

    public Hand getHand() {
        return hand;
    }

    @Override
    protected int getInventoryYOffset() {
        return 148;
    }

    @Override
    protected HotBarSlot createHotBarSlot(@Nonnull PlayerInventory inv, int index, int x, int y) {
        // special handling to prevent removing the personal chest from the player's inventory slot
        if (index == inv.currentItem && hand == Hand.MAIN_HAND) {
            return new HotBarSlot(inv, index, x, y) {
                @Override
                public boolean canTakeStack(@Nonnull PlayerEntity player) {
                    return false;
                }
            };
        }
        return super.createHotBarSlot(inv, index, x, y);
    }

    @Nonnull
    @Override
    public ItemStack slotClick(int slotId, int dragType, @Nonnull ClickType clickType, @Nonnull PlayerEntity player) {
        if (clickType == ClickType.SWAP) {
            if (hand == Hand.OFF_HAND && dragType == 40) {
                //Block pressing f to swap it when it is in the offhand
                return ItemStack.EMPTY;
            } else if (hand == Hand.MAIN_HAND && dragType >= 0 && dragType < PlayerInventory.getHotbarSize()) {
                //Block taking out of the selected slot (we don't validate we have a hotbar slot as we always should for this container)
                if (!hotBarSlots.get(dragType).canTakeStack(player)) {
                    return ItemStack.EMPTY;
                }
            }
        }
        return super.slotClick(slotId, dragType, clickType, player);
    }
}