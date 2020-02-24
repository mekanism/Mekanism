package mekanism.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import java.util.function.Supplier;
import javax.annotation.Nonnull;
import mekanism.api.text.EnumColor;
import mekanism.client.gui.element.GuiElement;
import mekanism.client.gui.element.GuiElement.IHoverable;
import mekanism.client.gui.element.slot.GuiSlot;
import mekanism.client.gui.element.slot.SlotType;
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
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

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
        initPreSlots();
        if (dynamicSlots) {
            addSlots();
        }
    }

    protected void initPreSlots() {
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
        for (Widget widget : this.buttons) {
            if (widget instanceof GuiElement) {
                //We let our gui element handle drawing any tooltip it may have when it is drawing the foreground
                ((GuiElement) widget).renderForeground(mouseX, mouseY, xAxis, yAxis);
            }
        }
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double mouseXOld, double mouseYOld) {
        //TODO: mouseXOld and mouseYOld are just guessed mappings I couldn't find any usage from a quick glance. look closer
        super.mouseDragged(mouseX, mouseY, button, mouseXOld, mouseYOld);
        return getFocused() != null && isDragging() && button == 0 && getFocused().mouseDragged(mouseX, mouseY, button, mouseXOld, mouseYOld);
    }

    protected boolean isMouseOverSlot(Slot slot, double mouseX, double mouseY) {
        return isPointInRegion(slot.xPos, slot.yPos, 16, 16, mouseX, mouseY);
    }

    protected void drawGuiContainerBackgroundLayer(int xAxis, int yAxis) {
    }

    private void addSlots() {
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

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTick, int mouseX, int mouseY) {
        //Ensure the GL color is white as mods adding an overlay (such as JEI for bookmarks), might have left
        // it in an unexpected state.
        MekanismRenderer.resetColor();
        if (width < 8 || height < 8) {
            Mekanism.logger.warn("Gui: {}, was too small to draw the background of. Unable to draw a background for a gui smaller than 8 by 8.", getClass().getSimpleName());
            return;
        }
        GuiUtils.renderExtendedTexture(BASE_BACKGROUND, 4, 4, getGuiLeft(), getGuiTop(), getXSize(), getYSize());
        int xAxis = mouseX - getGuiLeft();
        int yAxis = mouseY - getGuiTop();
        drawGuiContainerBackgroundLayer(xAxis, yAxis);
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

    //Some blit param namings
    //blit(int x, int y, int textureX, int textureY, int width, int height);
    //blit(int x, int y, TextureAtlasSprite icon, int width, int height);
    //blit(int x, int y, int textureX, int textureY, int width, int height, int textureWidth, int textureHeight);
    //blit(int x, int y, int zLevel, float textureX, float textureY, int width, int height, int textureWidth, int textureHeight);
    //blit(int x, int y, int desiredWidth, int desiredHeight, int textureX, int textureY, int width, int height, int textureWidth, int textureHeight);
    //innerBlit(int x, int endX, int y, int endY, int zLevel, int width, int height, float textureX, float textureY, int textureWidth, int textureHeight);
}