package mekanism.common.inventory.slot;

import java.util.function.BiPredicate;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.api.IContentsListener;
import mekanism.api.annotations.FieldsAreNonnullByDefault;
import mekanism.api.annotations.NonNull;
import mekanism.api.inventory.AutomationType;
import mekanism.common.content.qio.QIOCraftingWindow;
import mekanism.common.inventory.container.slot.VirtualInventoryContainerSlot;
import net.minecraft.item.ItemStack;

@FieldsAreNonnullByDefault
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class CraftingWindowInventorySlot extends BasicInventorySlot {

    public static CraftingWindowInventorySlot input(QIOCraftingWindow window) {
        return new CraftingWindowInventorySlot(notExternal, alwaysTrueBi, window, window);
    }

    protected final QIOCraftingWindow craftingWindow;

    protected CraftingWindowInventorySlot(BiPredicate<@NonNull ItemStack, @NonNull AutomationType> canExtract,
          BiPredicate<@NonNull ItemStack, @NonNull AutomationType> canInsert, QIOCraftingWindow craftingWindow, @Nullable IContentsListener listener) {
        super(canExtract, canInsert, alwaysTrue, listener, 0, 0);
        this.craftingWindow = craftingWindow;
    }

    @Nonnull
    @Override
    public VirtualInventoryContainerSlot createContainerSlot() {
        return new VirtualInventoryContainerSlot(this, getSlotOverlay(), this::setStackUnchecked);
    }
}