package mekanism.common.inventory;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.inventory.IInventorySlot;
import mekanism.common.content.qio.IQIOCraftingWindowHolder;
import mekanism.common.content.qio.QIOCraftingWindow;
import mekanism.common.content.qio.QIOFrequency;
import mekanism.common.lib.frequency.Frequency;
import mekanism.common.lib.frequency.IFrequencyItem;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class PortableQIODashboardInventory extends ItemStackMekanismInventory implements IQIOCraftingWindowHolder {

    @Nullable
    private final World world;
    /**
     * @apiNote This is only not final for purposes of being able to assign and use it in getInitialInventory.
     */
    private QIOCraftingWindow[] craftingWindows;

    public PortableQIODashboardInventory(ItemStack stack, @Nonnull PlayerInventory inv) {
        super(stack);
        this.world = inv.player.getCommandSenderWorld();
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
    public World getHolderWorld() {
        return world;
    }

    @Override
    public QIOCraftingWindow[] getCraftingWindows() {
        return craftingWindows;
    }

    @Nullable
    @Override
    public QIOFrequency getFrequency() {
        if (world != null && !world.isClientSide()) {
            IFrequencyItem frequencyItem = (IFrequencyItem) stack.getItem();
            if (frequencyItem.hasFrequency(stack)) {
                Frequency frequency = frequencyItem.getFrequency(stack);
                if (frequency instanceof QIOFrequency) {
                    return (QIOFrequency) frequency;
                } else {
                    // if this frequency no longer exists, remove the reference from the stack
                    frequencyItem.setFrequency(stack, null);
                }
            }
        }
        return null;
    }
}