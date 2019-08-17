package mekanism.common.inventory.container.tile;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.common.inventory.container.MekanismContainerTypes;
import mekanism.common.inventory.slot.SlotPersonalChest;
import mekanism.common.tile.TileEntityPersonalChest;
import mekanism.common.util.text.TextComponentUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.ITextComponent;

//TODO: InventoryTweaks
//@ChestContainer(isLargeChest = true)
public class PersonalChestTileContainer extends MekanismTileContainer<TileEntityPersonalChest> {

    public PersonalChestTileContainer(int id, PlayerInventory inv, TileEntityPersonalChest tile) {
        super(MekanismContainerTypes.PERSONAL_CHEST_BLOCK, id, inv, tile);
    }

    public PersonalChestTileContainer(int id, PlayerInventory inv, PacketBuffer buf) {
        this(id, inv, getTileFromBuf(buf, TileEntityPersonalChest.class));
    }

    @Override
    protected void addSlots() {
        for (int slotY = 0; slotY < 6; slotY++) {
            for (int slotX = 0; slotX < 9; slotX++) {
                addSlot(new SlotPersonalChest(tile, slotX + slotY * 9, 8 + slotX * 18, 26 + slotY * 18));
            }
        }
    }

    @Override
    protected int getInventoryOffset() {
        return 148;
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

    @Nullable
    @Override
    public Container createMenu(int i, @Nonnull PlayerInventory inv, @Nonnull PlayerEntity player) {
        return new PersonalChestTileContainer(i, inv, tile);
    }

    @Nonnull
    @Override
    public ITextComponent getDisplayName() {
        return TextComponentUtil.translate("mekanism.container.personal_chest");
    }
}