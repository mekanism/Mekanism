package mekanism.client.gui.element.slot;

import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.GuiElement;
import net.minecraft.SharedConstants;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class GuiSequencedSlotDisplay extends GuiElement {

    private List<ItemStack> iterStacks = Collections.emptyList();
    private int stackIndex;
    private int stackSwitchTicker;
    @NotNull
    private ItemStack renderStack = ItemStack.EMPTY;
    private final Supplier<List<ItemStack>> stackListSupplier;

    public GuiSequencedSlotDisplay(IGuiWrapper gui, int x, int y, Supplier<List<ItemStack>> stackListSupplier) {
        super(gui, x, y, 16, 16);
        this.stackListSupplier = stackListSupplier;
        //Mark it as false for active so that it doesn't intercept click events and ensures that it properly clears it
        active = false;
    }

    @Override
    public void tick() {
        super.tick();
        //Decrease timer for stack display rotation
        if (stackSwitchTicker > 0) {
            stackSwitchTicker--;
        }
        //Update displayed stacks
        if (iterStacks.isEmpty()) {
            renderStack = ItemStack.EMPTY;
        } else if (stackSwitchTicker == 0) {
            int size = iterStacks.size();
            if (stackIndex == -1 || stackIndex == size - 1) {
                stackIndex = 0;
            } else if (stackIndex < size - 1) {
                stackIndex++;
            }
            stackIndex = Math.min(size - 1, stackIndex);
            renderStack = iterStacks.get(stackIndex);
            stackSwitchTicker = SharedConstants.TICKS_PER_SECOND;
        }
    }

    @Override
    public void drawBackground(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        super.drawBackground(guiGraphics, mouseX, mouseY, partialTicks);
        gui().renderItem(guiGraphics, renderStack, relativeX, relativeY);
    }

    public void updateStackList() {
        iterStacks = stackListSupplier.get();
        stackSwitchTicker = 0;
        tick();
        stackIndex = -1;
    }

    @NotNull
    public ItemStack getRenderStack() {
        return renderStack;
    }
}
