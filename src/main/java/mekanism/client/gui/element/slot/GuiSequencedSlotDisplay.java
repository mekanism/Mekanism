package mekanism.client.gui.element.slot;

import com.mojang.blaze3d.matrix.MatrixStack;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nonnull;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.GuiRelativeElement;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.util.NonNullSupplier;

public class GuiSequencedSlotDisplay extends GuiRelativeElement {

    private List<ItemStack> iterStacks = Collections.emptyList();
    private int stackIndex;
    private int stackSwitchTicker;
    @Nonnull
    private ItemStack renderStack = ItemStack.EMPTY;
    private final NonNullSupplier<List<ItemStack>> stackListSupplier;
    private float zOffset;

    public GuiSequencedSlotDisplay(IGuiWrapper gui, int x, int y, NonNullSupplier<List<ItemStack>> stackListSupplier) {
        super(gui, x, y, 16, 16);
        this.stackListSupplier = stackListSupplier;
        //Mark it as false for active so that it doesn't intercept click events and ensures that it properly clears it
        active = false;
    }

    public GuiSequencedSlotDisplay setZOffset(float zOffset) {
        this.zOffset = zOffset;
        return this;
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
    public void drawBackground(@Nonnull MatrixStack matrix, int mouseX, int mouseY, float partialTicks) {
        super.drawBackground(matrix, mouseX, mouseY, partialTicks);
        if (!renderStack.isEmpty()) {
            gui().getItemRenderer().zLevel += zOffset;
            gui().renderItem(matrix, renderStack, x, y);
            gui().getItemRenderer().zLevel -= zOffset;
        }
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
