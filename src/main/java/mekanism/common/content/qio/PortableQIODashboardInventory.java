package mekanism.common.content.qio;

import java.util.ArrayList;
import java.util.List;
import mekanism.api.inventory.IInventorySlot;
import mekanism.common.attachments.FrequencyAware;
import mekanism.common.attachments.qio.PortableDashboardContents;
import mekanism.common.registries.MekanismDataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public class PortableQIODashboardInventory implements IQIOCraftingWindowHolder {

    private final QIOCraftingWindow[] craftingWindows;
    private final List<IInventorySlot> slots;
    private final ItemStack stack;
    @Nullable
    private final Level level;
    private boolean initializing;

    public PortableQIODashboardInventory(@Nullable Level level, ItemStack stack) {
        this.stack = stack;
        this.level = level;
        List<IInventorySlot> slots = new ArrayList<>();
        craftingWindows = new QIOCraftingWindow[MAX_CRAFTING_WINDOWS];
        List<ItemStack> contents = stack.getOrDefault(MekanismDataComponents.QIO_DASHBOARD, PortableDashboardContents.EMPTY).contents();
        initializing = true;
        for (int tableIndex = 0; tableIndex < craftingWindows.length; tableIndex++) {
            int finalTableIndex = tableIndex;
            QIOCraftingWindow craftingWindow = new QIOCraftingWindow(this, (byte) tableIndex, slot -> () -> {
                //Skip contents change handling until we actually have our crafting window updated
                if (!initializing) {
                    ItemStack stored = craftingWindows[finalTableIndex].getInputSlot(slot).getStack().copy();
                    PortableDashboardContents content = stack.getOrDefault(MekanismDataComponents.QIO_DASHBOARD, PortableDashboardContents.EMPTY);
                    stack.set(MekanismDataComponents.QIO_DASHBOARD, content.with(finalTableIndex, slot, stored));
                }
            });
            craftingWindows[tableIndex] = craftingWindow;
            for (int slot = 0; slot < 9; slot++) {
                IInventorySlot inputSlot = craftingWindow.getInputSlot(slot);
                slots.add(inputSlot);
                //Note: setStack will ensure the stack is copied
                inputSlot.setStack(contents.get(tableIndex * 9 + slot));
            }
            slots.add(craftingWindow.getOutputSlot());
        }
        this.slots = List.copyOf(slots);
        initializing = false;
    }

    public List<IInventorySlot> getSlots() {
        return slots;
    }

    @Nullable
    @Override
    public Level getLevel() {
        return level;
    }

    @Override
    public QIOCraftingWindow[] getCraftingWindows() {
        return craftingWindows;
    }

    @Nullable
    @Override
    public QIOFrequency getFrequency() {
        if (level != null && !level.isClientSide() && !stack.isEmpty()) {//Note: This shouldn't be empty, but we validate it just in case
            FrequencyAware<QIOFrequency> frequencyAware = stack.get(MekanismDataComponents.QIO_FREQUENCY);
            if (frequencyAware != null) {
                return frequencyAware.getFrequency(stack, MekanismDataComponents.QIO_FREQUENCY.value());
            }
        }
        return null;
    }

    @Override
    public void onContentsChanged() {
    }
}