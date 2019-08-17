package mekanism.common.inventory.container.item;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.common.inventory.InventoryPersonalChest;
import mekanism.common.inventory.container.MekanismContainerTypes;
import mekanism.common.inventory.slot.SlotPersonalChest;
import mekanism.common.item.block.machine.ItemBlockPersonalChest;
import mekanism.common.util.text.TextComponentUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Hand;
import net.minecraft.util.text.ITextComponent;

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
        itemInventory = new InventoryPersonalChest(stack, hand);
        super.addSlotsAndOpen();
    }

    @Override
    protected void addSlots() {
        for (int slotY = 0; slotY < 6; slotY++) {
            for (int slotX = 0; slotX < 9; slotX++) {
                addSlot(new SlotPersonalChest(itemInventory, slotX + slotY * 9, 8 + slotX * 18, 26 + slotY * 18));
            }
        }
    }

    public InventoryPersonalChest getItemInventory() {
        return itemInventory;
    }

    @Override
    protected int getInventoryOffset() {
        return 148;
    }

    @Override
    protected void closeInventory(PlayerEntity player) {
        itemInventory.closeInventory(player);
    }

    @Override
    protected void openInventory(@Nonnull PlayerInventory inventory) {
        itemInventory.openInventory(inventory.player);
    }

    @Nonnull
    @Override
    public ItemStack transferStackInSlot(PlayerEntity player, int slotID) {
        ItemStack stack = ItemStack.EMPTY;
        Slot currentSlot = inventorySlots.get(slotID);
        if (currentSlot != null && currentSlot.getHasStack()) {
            ItemStack slotStack = currentSlot.getStack();
            stack = slotStack.copy();
            if (slotID < 54) {
                if (!mergeItemStack(slotStack, 54, inventorySlots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!mergeItemStack(slotStack, 0, 54, false)) {
                return ItemStack.EMPTY;
            }
            if (slotStack.getCount() == 0) {
                currentSlot.putStack(ItemStack.EMPTY);
            } else {
                currentSlot.onSlotChanged();
            }
            if (slotStack.getCount() == stack.getCount()) {
                return ItemStack.EMPTY;
            }
            currentSlot.onTake(player, slotStack);
        }
        return stack;
    }

    @Nonnull
    @Override
    public ItemStack slotClick(int slotId, int dragType, ClickType clickType, PlayerEntity player) {
        int hotbarSlotId = slotId - 81;
        //Disallow moving Personal Chest if held and accessed directly from inventory (not from a placed block)
        if (hotbarSlotId >= 0 && hotbarSlotId < 9 && player.inventory.currentItem == hotbarSlotId) {
            ItemStack itemStack = player.inventory.getStackInSlot(hotbarSlotId);
            if (!itemStack.isEmpty() && itemStack.getItem() instanceof ItemBlockPersonalChest) {
                return ItemStack.EMPTY;
            }
        }
        return super.slotClick(slotId, dragType, clickType, player);
    }

    @Nullable
    @Override
    public Container createMenu(int i, @Nonnull PlayerInventory inv, @Nonnull PlayerEntity player) {
        return new PersonalChestItemContainer(i, inv, hand, stack);
    }

    @Nonnull
    @Override
    public ITextComponent getDisplayName() {
        return TextComponentUtil.translate("mekanism.container.personal_chest");
    }
}