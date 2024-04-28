package mekanism.common.attachments.qio;

import java.util.ArrayList;
import java.util.List;
import mekanism.api.inventory.IInventorySlot;
import mekanism.common.attachments.FrequencyAware;
import mekanism.common.content.qio.IQIOCraftingWindowHolder;
import mekanism.common.content.qio.QIOCraftingWindow;
import mekanism.common.content.qio.QIOFrequency;
import mekanism.common.registries.MekanismDataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

//TODO - 1.20.5: Move this to a different package?
public class PortableQIODashboardInventory implements IQIOCraftingWindowHolder {

    private final QIOCraftingWindow[] craftingWindows;
    private final List<IInventorySlot> slots;
    private final ItemStack stack;
    @Nullable
    private final Level level;

    public PortableQIODashboardInventory(@Nullable Level level, ItemStack stack) {
        this.stack = stack;
        this.level = level;
        List<IInventorySlot> slots = new ArrayList<>();
        craftingWindows = new QIOCraftingWindow[MAX_CRAFTING_WINDOWS];
        PortableDashboardContents contents = stack.get(MekanismDataComponents.QIO_DASHBOARD);
        for (int tableIndex = 0; tableIndex < craftingWindows.length; tableIndex++) {
            QIOCraftingWindow craftingWindow = new QIOCraftingWindow(this, (byte) tableIndex);
            craftingWindows[tableIndex] = craftingWindow;
            for (int slot = 0; slot < 9; slot++) {
                IInventorySlot inputSlot = craftingWindow.getInputSlot(slot);
                slots.add(inputSlot);
                if (contents != null) {
                    //Note: setStack will ensure the stack is copied
                    inputSlot.setStack(contents.contents().get(tableIndex * 9 + slot));
                }
            }
            slots.add(craftingWindow.getOutputSlot());
        }
        this.slots = List.copyOf(slots);
        //TODO - 1.20.5: Is this still necessary given we are doing it from the constructor?
        //Force refresh the recipe
        for (QIOCraftingWindow craftingWindow : craftingWindows) {
            craftingWindow.invalidateRecipe();
        }
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
        List<ItemStack> stacks = new ArrayList<>(PortableDashboardContents.TOTAL_SLOTS);
        for (QIOCraftingWindow craftingWindow : craftingWindows) {
            for (int slot = 0; slot < 9; slot++) {
                stacks.add(craftingWindow.getInputSlot(slot).getStack().copy());
            }
        }
        stack.set(MekanismDataComponents.QIO_DASHBOARD, new PortableDashboardContents(stacks));
    }
}