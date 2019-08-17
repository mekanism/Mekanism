package mekanism.generators.common.inventory.container.reactor;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.text.TextComponentUtil;
import mekanism.generators.common.inventory.container.GeneratorsContainerTypes;
import mekanism.generators.common.item.ItemHohlraum;
import mekanism.generators.common.tile.reactor.TileEntityReactorController;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ReactorControllerContainer extends MekanismTileContainer<TileEntityReactorController> {

    public ReactorControllerContainer(int id, PlayerInventory inv, TileEntityReactorController tile) {
        super(GeneratorsContainerTypes.REACTOR_CONTROLLER, id, inv, tile);
    }

    public ReactorControllerContainer(int id, PlayerInventory inv, PacketBuffer buf) {
        this(id, inv, getTileFromBuf(buf, TileEntityReactorController.class));
    }

    @Nonnull
    @Override
    public ItemStack transferStackInSlot(PlayerEntity player, int slotID) {
        ItemStack stack = ItemStack.EMPTY;
        Slot currentSlot = inventorySlots.get(slotID);
        if (currentSlot != null && currentSlot.getHasStack()) {
            ItemStack slotStack = currentSlot.getStack();
            stack = slotStack.copy();
            if (slotStack.getItem() instanceof ItemHohlraum) {
                if (slotID != 0) {
                    if (!mergeItemStack(slotStack, 0, 1, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (!mergeItemStack(slotStack, 1, inventorySlots.size(), false)) {
                    return ItemStack.EMPTY;
                }
            } else if (slotID >= 1 && slotID <= 27) {
                if (!mergeItemStack(slotStack, 28, inventorySlots.size(), false)) {
                    return ItemStack.EMPTY;
                }
            } else if (slotID > 27) {
                if (!mergeItemStack(slotStack, 1, 27, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (!mergeItemStack(slotStack, 1, inventorySlots.size(), true)) {
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

    @Override
    protected void addSlots() {
        addSlot(new SlotReactor(tile, 0, 80, 39));
    }

    @Nullable
    @Override
    public Container createMenu(int i, @Nonnull PlayerInventory inv, @Nonnull PlayerEntity player) {
        return new ReactorControllerContainer(i, inv, tile);
    }

    @Nonnull
    @Override
    public ITextComponent getDisplayName() {
        return TextComponentUtil.translate("mekanismgenerators.container.reactor_controller");
    }

    public class SlotReactor extends Slot {

        public SlotReactor(IInventory inventory, int index, int x, int y) {
            super(inventory, index, x, y);
        }

        @Override
        @OnlyIn(Dist.CLIENT)
        public boolean isEnabled() {
            return tile != null && MekanismUtils.isActive(tile.getWorld(), tile.getPos());
        }
    }
}