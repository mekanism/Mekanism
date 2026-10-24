package mekanism.client.gui.element.slot;

import java.util.Optional;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.VirtualSlotContainerScreen;
import mekanism.common.inventory.container.IGUIWindow;
import mekanism.common.inventory.container.slot.IVirtualSlot;
import mekanism.common.inventory.container.slot.SlotOverlay;
import mekanism.common.inventory.container.slot.VirtualInventoryContainerSlot;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

public class GuiVirtualSlot extends GuiSlot {

    @Nullable
    private IVirtualSlot virtualSlot;

    public GuiVirtualSlot(@Nullable IGUIWindow window, SlotType type, IGuiWrapper gui, int x, int y, @Nullable VirtualInventoryContainerSlot containerSlot) {
        this(type, gui, x, y);
        if (containerSlot != null) {
            SlotOverlay slotOverlay = containerSlot.getSlotOverlay();
            if (slotOverlay != null) {
                with(slotOverlay);
            }
            updateVirtualSlot(window, containerSlot);
        }
    }

    public GuiVirtualSlot(SlotType type, IGuiWrapper gui, int x, int y) {
        super(type, gui, x, y);
        //Virtual slots need to render the hovered overlay as they don't let vanilla render it
        setRenderHover(true);
    }

    public boolean isElementForSlot(IVirtualSlot virtualSlot) {
        return this.virtualSlot == virtualSlot;
    }

    public void updateVirtualSlot(@Nullable IGUIWindow window, IVirtualSlot virtualSlot) {
        this.virtualSlot = virtualSlot;
        this.virtualSlot.updatePosition(window, () -> relativeX + 1, () -> relativeY + 1);
    }

    @Override
    protected void drawContents(GuiGraphicsExtractor guiGraphics) {
        if (!tryDrawGhost(guiGraphics) && virtualSlot != null) {
            int xPos = relativeX + 1;
            int yPos = relativeY + 1;
            if (virtualSlot.isHovering()) {
                //Copy of AbstractContainerScreen#extractSlotHighlightBack
                guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, AbstractContainerScreen.SLOT_HIGHLIGHT_BACK_SPRITE, xPos - 4, yPos - 4, 24, 24);
            }
            ItemStack stack = virtualSlot.getStackToRender();
            if (!stack.isEmpty()) {
                if (virtualSlot.quickCraftStack()) {
                    guiGraphics.fill(xPos, yPos, xPos + 16, yPos + 16, DEFAULT_HOVER_COLOR);
                }
                guiGraphics.item(stack, xPos, yPos);
                guiGraphics.itemDecorations(font(), stack, xPos, yPos, virtualSlot.getTooltipOverride());
            }
            if (virtualSlot.isHovering()) {
                //Copy of AbstractContainerScreen#extractSlotHighlightFront
                guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, AbstractContainerScreen.SLOT_HIGHLIGHT_FRONT_SPRITE, xPos - 4, yPos - 4, 24, 24);
            }
        }
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean isDoubleClick) {
        if (event.x() >= getX() && event.y() >= getY() && event.x() < getRight() && event.y() < getBottom()) {
            IGuiWrapper gui = gui();
            if (gui instanceof VirtualSlotContainerScreen<?> screen && virtualSlot != null) {
                //Redirect to a copy of vanilla logic
                return screen.slotClicked(virtualSlot.getSlot(), event, isDoubleClick);
            }
        }
        return super.mouseClicked(event, isDoubleClick);
    }

    @Override
    public Optional<?> getIngredient(double mouseX, double mouseY) {
        Optional<?> ingredient = super.getIngredient(mouseX, mouseY);
        if (ingredient.isPresent() || virtualSlot == null) {
            return ingredient;
        }
        //Note: We can get away with just using the stack to render
        return Optional.of(virtualSlot.getStackToRender());
    }
}