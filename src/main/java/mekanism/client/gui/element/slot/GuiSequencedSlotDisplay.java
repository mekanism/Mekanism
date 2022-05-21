package mekanism.client.gui.element.slot;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nonnull;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.GuiElement;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.util.NonNullSupplier;

public class GuiSequencedSlotDisplay extends GuiElement {

    private List<ItemStack> iterStacks = Collections.emptyList();
    private int stackIndex;
    private int stackSwitchTicker;
    @Nonnull
    private ItemStack renderStack = ItemStack.EMPTY;
    private final NonNullSupplier<List<ItemStack>> stackListSupplier;

    public GuiSequencedSlotDisplay(IGuiWrapper gui, int x, int y, NonNullSupplier<List<ItemStack>> stackListSupplier) {
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
            stackSwitchTicker = 20;
        }
    }

    @Override
    public void drawBackground(@Nonnull PoseStack matrix, int mouseX, int mouseY, float partialTicks) {
        super.drawBackground(matrix, mouseX, mouseY, partialTicks);
        gui().renderItem(matrix, renderStack, x, y);
    }

    public void updateStackList() {
        iterStacks = stackListSupplier.get();
        stackSwitchTicker = 0;
        tick();
        stackIndex = -1;
    }

    @Nonnull
    public ItemStack getRenderStack() {
        return renderStack;
    }
}
