package mekanism.common.inventory.slot;

import java.util.function.BiPredicate;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import mekanism.api.AutomationType;
import mekanism.api.IContentsListener;
import mekanism.api.annotations.FieldsAreNonnullByDefault;
import mekanism.api.annotations.NonNull;
import mekanism.common.content.qio.QIOCraftingWindow;
import mekanism.common.inventory.container.slot.VirtualInventoryContainerSlot;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.ItemHandlerHelper;

@FieldsAreNonnullByDefault
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class CraftingWindowInventorySlot extends BasicInventorySlot {

    public static CraftingWindowInventorySlot input(QIOCraftingWindow window, @Nullable IContentsListener saveListener) {
        return new CraftingWindowInventorySlot(notExternal, alwaysTrueBi, window, saveListener, window);
    }

    protected final QIOCraftingWindow craftingWindow;
    @Nullable
    private final IContentsListener inputTypeChange;
    private ItemStack lastCurrent = ItemStack.EMPTY;
    private boolean wasEmpty = true;

    protected CraftingWindowInventorySlot(BiPredicate<@NonNull ItemStack, @NonNull AutomationType> canExtract,
          BiPredicate<@NonNull ItemStack, @NonNull AutomationType> canInsert, QIOCraftingWindow craftingWindow, @Nullable IContentsListener saveListener,
          @Nullable IContentsListener inputTypeChange) {
        super(canExtract, canInsert, alwaysTrue, saveListener, 0, 0);
        this.craftingWindow = craftingWindow;
        this.inputTypeChange = inputTypeChange;
    }

    @Nonnull
    @Override
    public VirtualInventoryContainerSlot createContainerSlot() {
        return new VirtualInventoryContainerSlot(this, craftingWindow.getWindowData(), getSlotOverlay(), this::setStackUnchecked);
    }

    @Override
    public void onContentsChanged() {
        super.onContentsChanged();
        if (inputTypeChange != null) {
            if (current.isEmpty() != wasEmpty || current != lastCurrent && !ItemHandlerHelper.canItemStacksStack(current, lastCurrent)) {
                //If empty state changed, or they are not the same object, and they are not the same type, then mark our input type changed
                // Note: If they are the same object (growing or shrinking) then we know they are the same type given they are not empty
                lastCurrent = current;
                wasEmpty = current.isEmpty();
                inputTypeChange.onContentsChanged();
            }
        }
    }
}