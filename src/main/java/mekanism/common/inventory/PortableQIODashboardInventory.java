package mekanism.common.inventory;

import java.util.ArrayList;
import java.util.List;
import mekanism.api.inventory.IInventorySlot;
import mekanism.common.content.qio.IQIOCraftingWindowHolder;
import mekanism.common.content.qio.QIOCraftingWindow;
import mekanism.common.content.qio.QIOFrequency;
import mekanism.common.lib.frequency.Frequency;
import mekanism.common.registries.MekanismAttachmentTypes;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PortableQIODashboardInventory extends ItemStackMekanismInventory implements IQIOCraftingWindowHolder {

    @Nullable
    private final Level world;
    /**
     * @apiNote This is only not final for purposes of being able to assign and use it in getInitialInventory.
     */
    private QIOCraftingWindow[] craftingWindows;

    public PortableQIODashboardInventory(ItemStack stack, @NotNull Inventory inv) {
        super(stack);
        this.world = inv.player.level();
        for (QIOCraftingWindow craftingWindow : craftingWindows) {
            //Force refresh the recipe now that we have a world set and can actually calculate it
            craftingWindow.invalidateRecipe();
        }
    }

    @Override
    protected List<IInventorySlot> getInitialInventory() {
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
        return slots;
    }

    @Nullable
    @Override
    public Level getLevel() {
        return world;
    }

    @Override
    public QIOCraftingWindow[] getCraftingWindows() {
        return craftingWindows;
    }

    @Nullable
    @Override
    public QIOFrequency getFrequency() {
        if (world != null && !world.isClientSide() && !stack.isEmpty()) {//Note: This shouldn't be empty but we validate it just in case
            Frequency frequency = stack.getData(MekanismAttachmentTypes.FREQUENCY_AWARE).getFrequency();
            if (frequency instanceof QIOFrequency freq) {
                return freq;
            }
        }
        return null;
    }
}