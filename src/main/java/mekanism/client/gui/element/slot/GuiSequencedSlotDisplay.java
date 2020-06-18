package mekanism.client.gui.element.slot;

import java.util.List;
import java.util.function.Supplier;
import javax.annotation.Nonnull;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.GuiRelativeElement;
import net.minecraft.item.ItemStack;

public class GuiSequencedSlotDisplay extends GuiRelativeElement {

    private List<ItemStack> iterStacks;
    private int stackIndex;
    private int stackSwitchTicker;
    @Nonnull
    private ItemStack renderStack = ItemStack.EMPTY;
    private final Supplier<List<ItemStack>> stackListSupplier;
    private float zOffset;

    public GuiSequencedSlotDisplay(IGuiWrapper gui, int x, int y, Supplier<List<ItemStack>> stackListSupplier) {
        super(gui, x, y, 16, 16);
        this.stackListSupplier = stackListSupplier;
    }

    public GuiSequencedSlotDisplay setZOffset(float zOffset) {
        this.zOffset = zOffset;
        return this;
    }

    @Override
    public void tick() {
        super.tick();

        // Decrease timer for stack display rotation
        if (stackSwitchTicker > 0) {
            stackSwitchTicker--;
        }
        // Update displayed stacks
        if (stackSwitchTicker == 0) {
            setNextRenderStack();
            stackSwitchTicker = 20;
        } else if (iterStacks != null && iterStacks.isEmpty()) {
            renderStack = ItemStack.EMPTY;
        }
    }

    @Override
    public void drawButton(int mouseX, int mouseY) {
        guiObj.getItemRenderer().zLevel += zOffset;
        guiObj.renderItem(renderStack, x, y);
        guiObj.getItemRenderer().zLevel -= zOffset;
    }

    public void updateStackList() {
        iterStacks = stackListSupplier.get();
        stackSwitchTicker = 0;
        tick();
        stackIndex = -1;
    }

    private void setNextRenderStack() {
        if (iterStacks != null && !iterStacks.isEmpty()) {
            if (stackIndex == -1 || stackIndex == iterStacks.size() - 1) {
                stackIndex = 0;
            } else if (stackIndex < iterStacks.size() - 1) {
                stackIndex++;
            }
            stackIndex = Math.min(iterStacks.size() - 1, stackIndex);
            renderStack = iterStacks.get(stackIndex);
        }
    }

    @Nonnull
    public ItemStack getRenderStack() {
        return renderStack;
    }
}
