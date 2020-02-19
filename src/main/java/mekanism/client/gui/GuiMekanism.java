package mekanism.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.text.EnumColor;
import mekanism.client.gui.button.MekanismButton.IHoverable;
import mekanism.client.gui.element.GuiElement;
import mekanism.client.gui.element.GuiSlot;
import mekanism.client.gui.element.GuiSlot.SlotType;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.Mekanism;
import mekanism.common.base.ILangEntry;
import mekanism.common.inventory.container.slot.ContainerSlotType;
import mekanism.common.inventory.container.slot.InventoryContainerSlot;
import mekanism.common.inventory.container.slot.SlotOverlay;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.fml.client.gui.GuiUtils;

//TODO: Add our own "addButton" type thing for elements that are just "drawn" but don't actually have any logic behind them
public abstract class GuiMekanism<CONTAINER extends Container> extends ContainerScreen<CONTAINER> implements IGuiWrapper {

    private static final ResourceLocation BASE_BACKGROUND = MekanismUtils.getResource(ResourceType.GUI, "base.png");
    //TODO: Either remove the need for this or at the very least default it to true
    protected boolean dynamicSlots;

    protected GuiMekanism(CONTAINER container, PlayerInventory inv, ITextComponent title) {
        super(container, inv, title);
    }

    @Override
    public void init() {
        super.init();
        if (dynamicSlots) {
            addSlots();
        }
    }

    protected IHoverable getOnHover(ILangEntry translationHelper) {
        return getOnHover((Supplier<ITextComponent>) translationHelper::translate);
    }

    protected IHoverable getOnHover(Supplier<ITextComponent> componentSupplier) {
        return (onHover, xAxis, yAxis) -> displayTooltip(componentSupplier.get(), xAxis, yAxis);
    }

    protected ResourceLocation getButtonLocation(String name) {
        return MekanismUtils.getResource(ResourceType.GUI_BUTTON, name + ".png");
    }

    public int getStringWidth(ITextComponent component) {
        //TODO: See if this should be calculated in a different way
        return getStringWidth(component.getFormattedText());
    }

    public int getStringWidth(String text) {
        //TODO: Replace uses of this/the other getStringWidth with drawCenteredText where appropriate
        return font.getStringWidth(text);
    }

    public int drawString(ITextComponent component, int x, int y, int color) {
        //TODO: Check if color actually does anything
        return drawString(component.getFormattedText(), x, y, color);
    }

    public int drawString(String text, int x, int y, int color) {
        //TODO: Eventually make drawString(ITextComponent) be what gets used in places
        return font.drawString(text, x, y, color);
    }

    protected void drawCenteredText(ITextComponent component, int y, int color) {
        //TODO: Go back and evaluate to make sure that the left margin is correct
        drawCenteredText(component, 0, y, color);
    }

    protected void drawCenteredText(ITextComponent component, int leftMargin, int y, int color) {
        drawCenteredText(component, leftMargin, 0, y, color);
    }

    protected void drawCenteredText(ITextComponent component, int leftMargin, int areaWidth, int y, int color) {
        int textWidth = getStringWidth(component);
        int centerX = leftMargin + (areaWidth / 2) - (textWidth / 2);
        drawString(component, centerX, y, color);
    }

    public void renderScaledText(ITextComponent component, int x, int y, int color, int maxX) {
        renderScaledText(component.getFormattedText(), x, y, color, maxX);
    }

    /**
     * returns scale
     */
    public void renderScaledText(String text, int x, int y, int color, int maxX) {
        int length = getStringWidth(text);
        if (length <= maxX) {
            drawString(text, x, y, color);
        } else {
            float scale = (float) maxX / length;
            float reverse = 1 / scale;
            float yAdd = 4 - (scale * 8) / 2F;
            RenderSystem.pushMatrix();
            RenderSystem.scalef(scale, scale, scale);
            drawString(text, (int) (x * reverse), (int) ((y * reverse) + yAdd), color);
            RenderSystem.popMatrix();
        }
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        int xAxis = mouseX - getGuiLeft();
        int yAxis = mouseY - getGuiTop();
        //TODO: Does color need to be reset before this
        for (Widget widget : this.buttons) {
            if (widget instanceof GuiElement) {
                //We let our gui element handle drawing any tooltip it may have when it is drawing the foreground
                ((GuiElement) widget).renderForeground(mouseX, mouseY, xAxis, yAxis);
            }
        }
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double mouseXOld, double mouseYOld) {
        super.mouseDragged(mouseX, mouseY, button, mouseXOld, mouseYOld);
        return getFocused() != null && isDragging() && button == 0 && getFocused().mouseDragged(mouseX, mouseY, button, mouseXOld, mouseYOld);
    }

    protected boolean isMouseOverSlot(Slot slot, double mouseX, double mouseY) {
        return isPointInRegion(slot.xPos, slot.yPos, 16, 16, mouseX, mouseY);
    }

    protected void drawGuiContainerBackgroundLayer(int xAxis, int yAxis) {
    }

    private void addSlots() {
        //TODO: Evaluate if we want any of the things that are currently colored due to being "extra" to not
        // be colored, for example fluid input slot on the electrolytic separator
        for (Slot slot : container.inventorySlots) {
            if (slot instanceof InventoryContainerSlot) {
                InventoryContainerSlot containerSlot = (InventoryContainerSlot) slot;
                ContainerSlotType slotType = containerSlot.getSlotType();
                //Shift the slots by one as the elements include the border of the slot
                SlotType type;
                if (slotType == ContainerSlotType.INPUT) {
                    type = SlotType.INPUT;
                } else if (slotType == ContainerSlotType.OUTPUT) {
                    type = SlotType.OUTPUT;
                } else if (slotType == ContainerSlotType.POWER) {
                    type = SlotType.POWER;
                } else if (slotType == ContainerSlotType.EXTRA) {
                    type = SlotType.EXTRA;
                } else if (slotType == ContainerSlotType.NORMAL) {
                    type = SlotType.NORMAL;
                } else {//slotType == ContainerSlotType.IGNORED: don't do anything
                    continue;
                }
                GuiSlot guiSlot = new GuiSlot(type, this, slot.xPos - 1, slot.yPos - 1);
                SlotOverlay slotOverlay = containerSlot.getSlotOverlay();
                if (slotOverlay != null) {
                    guiSlot.with(slotOverlay);
                }
                addButton(guiSlot);
            } else {
                addButton(new GuiSlot(SlotType.NORMAL, this, slot.xPos - 1, slot.yPos - 1));
            }
        }
    }

    //TODO: Inline into drawGuiContainerBackgroundLayer
    private void drawBackground() {
        int width = getXSize();
        int height = getYSize();
        if (width < 8 || height < 8) {
            Mekanism.logger.warn("Gui: {}, was too small to draw the background of. Unable to draw a background for a gui smaller than 8 by 8.", getClass().getSimpleName());
            return;
        }
        int top = getGuiTop();
        int left = getGuiLeft();
        int sideWidth = 4;
        int sideHeight = 4;
        int textureWidth = 2 * sideWidth + 1;
        int textureHeight = 2 * sideHeight + 1;
        int centerWidth = width - 2 * sideWidth;
        int centerHeight = height - 2 * sideHeight;
        int leftEdgeEnd = left + sideHeight;
        int rightEdgeStart = leftEdgeEnd + centerWidth;
        int topEdgeEnd = top + sideWidth;
        int bottomEdgeStart = topEdgeEnd + centerHeight;
        minecraft.textureManager.bindTexture(BASE_BACKGROUND);
        //Left Side
        //Top Left Corner
        drawModalRectWithCustomSizedTexture(left, top, 0, 0, sideWidth, sideHeight, textureWidth, textureHeight);
        //Left Middle
        if (centerHeight > 0) {
            drawModalRectWithCustomSizedTexture(left, topEdgeEnd, sideWidth, centerHeight, 0, sideHeight, sideWidth, 1, textureWidth, textureHeight);
        }
        //Bottom Left Corner
        drawModalRectWithCustomSizedTexture(left, bottomEdgeStart, 0, sideHeight + 1, sideWidth, sideHeight, textureWidth, textureHeight);

        //Middle
        if (centerWidth > 0) {
            //Top Middle
            drawModalRectWithCustomSizedTexture(leftEdgeEnd, top, centerWidth, sideHeight, sideWidth, 0, 1, sideHeight, textureWidth, textureHeight);
            if (centerHeight > 0) {
                //Center
                drawModalRectWithCustomSizedTexture(leftEdgeEnd, topEdgeEnd, centerWidth, centerHeight, sideWidth, sideHeight, 1, 1, textureWidth, textureHeight);
            }
            //Bottom Middle
            drawModalRectWithCustomSizedTexture(leftEdgeEnd, bottomEdgeStart, centerWidth, sideHeight, sideWidth, sideHeight + 1, 1, sideHeight, textureWidth, textureHeight);
        }

        //Right side
        //Top Right Corner
        drawModalRectWithCustomSizedTexture(rightEdgeStart, top, sideWidth + 1, 0, sideWidth, sideHeight, textureWidth, textureHeight);
        //Right Middle
        if (centerHeight > 0) {
            drawModalRectWithCustomSizedTexture(rightEdgeStart, topEdgeEnd, sideWidth, centerHeight, sideWidth + 1, sideHeight, sideWidth, 1, textureWidth, textureHeight);
        }
        //Bottom Right Corner
        drawModalRectWithCustomSizedTexture(rightEdgeStart, bottomEdgeStart, sideWidth + 1, sideHeight + 1, sideWidth, sideHeight, textureWidth, textureHeight);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTick, int mouseX, int mouseY) {
        //Ensure the GL color is white as mods adding an overlay (such as JEI for bookmarks), might have left
        // it in an unexpected state.
        MekanismRenderer.resetColor();
        ResourceLocation guiLocation = getGuiLocation();
        if (guiLocation == null) {
            drawBackground();
        } else {
            minecraft.textureManager.bindTexture(guiLocation);
            drawTexturedRect(getGuiLeft(), getGuiTop(), 0, 0, getXSize(), getYSize());
        }
        int xAxis = mouseX - getGuiLeft();
        int yAxis = mouseY - getGuiTop();
        drawGuiContainerBackgroundLayer(xAxis, yAxis);
    }

    @Override
    public void drawTexturedRect(int x, int y, int textureX, int textureY, int width, int height) {
        blit(x, y, textureX, textureY, width, height);
    }

    @Override
    public void drawTexturedRectFromIcon(int x, int y, TextureAtlasSprite icon, int width, int height) {
        blit(x, y, getBlitOffset(), width, height, icon);
    }

    @Override
    public void drawModalRectWithCustomSizedTexture(int x, int y, int textureX, int textureY, int width, int height, int textureWidth, int textureHeight) {
        blit(x, y, textureX, textureY, width, height, textureWidth, textureHeight);
    }

    @Override
    public void drawModalRectWithCustomSizedTexture(int x, int y, int desiredWidth, int desiredHeight, int textureX, int textureY, int width, int height, int textureWidth, int textureHeight) {
        blit(x, y, desiredWidth, desiredHeight, textureX, textureY, width, height, textureWidth, textureHeight);
    }

    @Override
    public void displayTooltip(ITextComponent component, int x, int y) {
        this.displayTooltips(Collections.singletonList(component), x, y);
    }

    @Override
    public void displayTooltips(List<ITextComponent> components, int xAxis, int yAxis) {
        //TODO: Evaluate if we want to use this for splitting the text
        List<String> toolTips = components.stream().map(ITextComponent::getFormattedText).collect(Collectors.toList());
        GuiUtils.drawHoveringText(toolTips, xAxis, yAxis, width, height, -1, font);
        //Fix unwanted lighting changes made by drawHoveringText
        RenderHelper.disableStandardItemLighting();
    }

    @Override
    public FontRenderer getFont() {
        return font;
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        this.renderBackground();
        super.render(mouseX, mouseY, partialTicks);
        this.renderHoveredToolTip(mouseX, mouseY);
    }

    protected void drawColorIcon(int x, int y, EnumColor color, float alpha) {
        if (color != null) {
            fill(x, y, x + 16, y + 16, MekanismRenderer.getColorARGB(color, alpha));
            MekanismRenderer.resetColor();
        }
    }

    @Override
    public void renderItem(@Nonnull ItemStack stack, int xAxis, int yAxis, float scale) {
        if (!stack.isEmpty()) {
            try {
                RenderSystem.pushMatrix();
                RenderSystem.enableDepthTest();
                RenderHelper.enableStandardItemLighting();
                if (scale != 1) {
                    RenderSystem.scalef(scale, scale, scale);
                }
                itemRenderer.renderItemAndEffectIntoGUI(stack, xAxis, yAxis);
                RenderHelper.disableStandardItemLighting();
                RenderSystem.disableDepthTest();
                RenderSystem.popMatrix();
            } catch (Exception e) {
                Mekanism.logger.error("Failed to render stack into gui: " + stack, e);
            }
        }
    }

    @Nullable
    protected ResourceLocation getGuiLocation() {
        //TODO: eventually remove this, for now we are just defaulting to null,
        // which means to fallback and use the dynamic background
        return null;
    }
}