package mekanism.common.inventory.slot;

import java.util.function.BiPredicate;
import mekanism.api.AutomationType;
import mekanism.api.IContentsListener;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.functions.ConstantPredicates;
import mekanism.common.content.qio.QIOCraftingWindow;
import mekanism.common.inventory.container.slot.VirtualInventoryContainerSlot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public class CraftingWindowInventorySlot extends BasicInventorySlot {

    public static CraftingWindowInventorySlot input(QIOCraftingWindow window, @Nullable IContentsListener saveListener) {
        return new CraftingWindowInventorySlot(ConstantPredicates.notExternal(), ConstantPredicates.alwaysTrueBi(), window, saveListener, window);
    }

    protected final QIOCraftingWindow craftingWindow;
    @Nullable
    private final IContentsListener inputTypeChange;
    private ItemStack lastCurrent = ItemStack.EMPTY;
    private boolean wasEmpty = true;

    protected CraftingWindowInventorySlot(BiPredicate<@NotNull ItemStack, @NotNull AutomationType> canExtract,
          BiPredicate<@NotNull ItemStack, @NotNull AutomationType> canInsert, QIOCraftingWindow craftingWindow, @Nullable IContentsListener saveListener,
          @Nullable IContentsListener inputTypeChange) {
        super(canExtract, canInsert, ConstantPredicates.alwaysTrue(), saveListener, 0, 0);
        this.craftingWindow = craftingWindow;
        this.inputTypeChange = inputTypeChange;
    }

    @NotNull
    @Override
    public VirtualInventoryContainerSlot createContainerSlot() {
        return new VirtualInventoryContainerSlot(this, craftingWindow.getWindowData(), getSlotOverlay(), this::setStackUnchecked);
    }

    @Override
    public void onContentsChanged() {
        super.onContentsChanged();
        if (inputTypeChange != null) {
            if (current.isEmpty() != wasEmpty || current != lastCurrent && !ItemStack.isSameItemSameComponents(current, lastCurrent)) {
                //If empty state changed, or they are not the same object, and they are not the same type, then mark our input type changed
                // Note: If they are the same object (growing or shrinking) then we know they are the same type given they are not empty
                lastCurrent = current;
                wasEmpty = current.isEmpty();
                inputTypeChange.onContentsChanged();
            }
        }
    }
}