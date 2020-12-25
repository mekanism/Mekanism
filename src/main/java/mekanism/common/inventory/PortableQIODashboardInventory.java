package mekanism.common.inventory;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;
import mekanism.api.inventory.IInventorySlot;
import mekanism.common.content.qio.IQIOCraftingWindowHolder;
import mekanism.common.content.qio.QIOCraftingWindow;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class PortableQIODashboardInventory extends ItemStackMekanismInventory implements IQIOCraftingWindowHolder {

    /**
     * @apiNote This is only not final for purposes of being able to assign and use it in getInitialInventory.
     */
    private QIOCraftingWindow[] craftingWindows;
    @Nullable
    private final World world;

    public PortableQIODashboardInventory(ItemStack stack, @Nullable World world) {
        super(stack);
        this.world = world;
    }

    @Override
    protected List<IInventorySlot> getInitialInventory() {
        List<IInventorySlot> slots = new ArrayList<>();
        craftingWindows = new QIOCraftingWindow[MAX_CRAFTING_WINDOWS];
        for (byte tableIndex = 0; tableIndex < craftingWindows.length; tableIndex++) {
            QIOCraftingWindow craftingWindow =  new QIOCraftingWindow(this, tableIndex);
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
}