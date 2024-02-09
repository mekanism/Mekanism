package mekanism.common.attachments;

import java.util.ArrayList;
import java.util.List;
import mekanism.api.inventory.IInventorySlot;
import mekanism.common.content.qio.IQIOCraftingWindowHolder;
import mekanism.common.content.qio.QIOCraftingWindow;
import mekanism.common.content.qio.QIOFrequency;
import mekanism.common.lib.frequency.Frequency;
import mekanism.common.registries.MekanismAttachmentTypes;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.registries.MekanismItems;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import org.jetbrains.annotations.Nullable;

public class PortableQIODashboardInventory implements IQIOCraftingWindowHolder {

    public static PortableQIODashboardInventory create(IAttachmentHolder holder) {
        if (holder instanceof ItemStack stack && !stack.isEmpty()) {
            if (stack.is(MekanismItems.PORTABLE_QIO_DASHBOARD.asItem()) || stack.is(MekanismBlocks.QIO_DASHBOARD.asItem())) {
                //Legacy is loaded by the container type
                return new PortableQIODashboardInventory(stack);
            }
        }
        throw new IllegalArgumentException("Attempted to attach a QIO Dashboard to an object that doesn't support them.");
    }

    private final QIOCraftingWindow[] craftingWindows;
    private final List<IInventorySlot> slots;
    private final ItemStack stack;
    @Nullable
    private Level level;

    private PortableQIODashboardInventory(ItemStack stack) {
        this.stack = stack;
        List<IInventorySlot> slots = new ArrayList<>();
        craftingWindows = new QIOCraftingWindow[MAX_CRAFTING_WINDOWS];
        for (byte tableIndex = 0; tableIndex < craftingWindows.length; tableIndex++) {
            QIOCraftingWindow craftingWindow = new QIOCraftingWindow(this, tableIndex);
            craftingWindows[tableIndex] = craftingWindow;
            for (int slot = 0; slot < 9; slot++) {
                slots.add(craftingWindow.getInputSlot(slot));
            }
            slots.add(craftingWindow.getOutputSlot());
        }
        this.slots = List.copyOf(slots);
    }

    public PortableQIODashboardInventory updateLevel(@Nullable Level level) {
        this.level = level;
        //Force refresh the recipe
        for (QIOCraftingWindow craftingWindow : craftingWindows) {
            craftingWindow.invalidateRecipe();
        }
        return this;
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
            Frequency frequency = stack.getData(MekanismAttachmentTypes.FREQUENCY_AWARE).getFrequency();
            if (frequency instanceof QIOFrequency freq) {
                return freq;
            }
        }
        return null;
    }

    @Override
    public void onContentsChanged() {
    }
}