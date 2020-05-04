package mekanism.client.gui.element.slot;

import java.util.List;
import java.util.function.Supplier;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.GuiTexturedElement;
import net.minecraft.item.ItemStack;

public class GuiSequencedSlotDisplay extends GuiTexturedElement {

    private List<ItemStack> iterStacks;
    private int stackIndex;
    private int stackSwitchTicker;
    private ItemStack renderStack = ItemStack.EMPTY;
    private Supplier<List<ItemStack>> stackListSupplier;

    public GuiSequencedSlotDisplay(IGuiWrapper gui, int x, int y, Supplier<List<ItemStack>> stackListSupplier) {
        super(null, gui, x, y, 16, 16);
        this.stackListSupplier = stackListSupplier;
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
    public void drawButton(int mouseX, int mouseY) {}

    @Override
    public void renderForeground(int mouseX, int mouseY) {
        super.renderForeground(mouseX, mouseY);
        if (renderStack != null) {
            guiObj.renderItem(renderStack, relativeX, relativeY);
        }
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
}
