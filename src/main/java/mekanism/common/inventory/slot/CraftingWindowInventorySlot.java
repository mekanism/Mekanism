package mekanism.common.inventory.slot;

import java.util.function.BiPredicate;
import mekanism.api.AutomationType;
import mekanism.api.IContentsListener;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.functions.ConstantPredicates;
import mekanism.api.resource.LargeResourceStack;
import mekanism.common.content.qio.QIOCraftingWindow;
import mekanism.common.inventory.container.slot.VirtualInventoryContainerSlot;
import net.neoforged.neoforge.transfer.item.ItemResource;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public class CraftingWindowInventorySlot extends BasicInventorySlot {

    public static CraftingWindowInventorySlot input(QIOCraftingWindow window, @Nullable IContentsListener saveListener) {
        return new CraftingWindowInventorySlot(ConstantPredicates.notExternal(), ConstantPredicates.alwaysTrueBi(), window, saveListener, window);
    }

    protected final QIOCraftingWindow craftingWindow;
    @Nullable
    private final IContentsListener inputTypeChange;

    protected CraftingWindowInventorySlot(BiPredicate<ItemResource, AutomationType> canExtract, BiPredicate<ItemResource, AutomationType> canInsert,
          QIOCraftingWindow craftingWindow, @Nullable IContentsListener saveListener, @Nullable IContentsListener inputTypeChange) {
        super(canExtract, canInsert, ConstantPredicates.alwaysTrue(), null, null, saveListener, 0, 0);
        this.craftingWindow = craftingWindow;
        this.inputTypeChange = inputTypeChange;
    }

    @Override
    public VirtualInventoryContainerSlot createContainerSlot() {
        return new VirtualInventoryContainerSlot(this, craftingWindow.getWindowData(), getSlotOverlay());
    }

    @Override
    public void onContentsChanged(LargeResourceStack<ItemResource> originalState) {
        super.onContentsChanged(originalState);
        if (inputTypeChange != null && !originalState.matches(resource())) {
            //If our type changed, mark that it changed
            inputTypeChange.onContentsChanged();
        }
    }
}