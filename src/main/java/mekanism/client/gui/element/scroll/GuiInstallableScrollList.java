package mekanism.client.gui.element.scroll;

import java.util.List;
import mekanism.client.gui.IGuiWrapper;
import mekanism.common.Mekanism;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.CommonColors;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

public abstract class GuiInstallableScrollList<TYPE> extends GuiScrollList {

    public static final Identifier BASE = Mekanism.rl("list/base");
    public static final Identifier HOVERED = Mekanism.rl("list/hovered");
    public static final Identifier SELECTED = Mekanism.rl("list/selected");
    private static final Identifier BASE_SLOT = Mekanism.rl("list/slot");
    private static final Identifier HOVERED_SLOT = Mekanism.rl("list/slot_hovered");
    private static final Identifier SELECTED_SLOT = Mekanism.rl("list/slot_selected");

    @Nullable
    protected TYPE selectedType;
    @Nullable
    protected ScreenRectangle cachedTooltipRect;

    protected GuiInstallableScrollList(IGuiWrapper gui, int x, int y, int width, int height, Identifier background) {
        super(gui, x, y, width, height, 12, background);
    }

    protected abstract List<TYPE> getCurrentInstalled();

    protected abstract void drawName(GuiGraphicsExtractor guiGraphics, TYPE type, int multipliedElement);

    protected abstract ItemStack getRenderStack(TYPE type);

    @Nullable
    public TYPE getSelection() {
        return selectedType;
    }

    @Override
    public boolean hasSelection() {
        return selectedType != null;
    }

    @Override
    protected int getMaxElements() {
        return getCurrentInstalled().size();
    }

    @Override
    protected void setSelected(int index) {
        if (index >= 0) {
            List<TYPE> currentInstalled = getCurrentInstalled();
            if (index < currentInstalled.size()) {
                setSelected(currentInstalled.get(index));
            }
        }
    }

    protected abstract void setSelected(@Nullable TYPE newType);

    @Override
    public void clearSelection() {
        setSelected(null);
    }

    @Override
    public void renderForeground(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY) {
        super.renderForeground(guiGraphics, mouseX, mouseY);
        int currentSelection = getCurrentSelection();
        List<TYPE> currentInstalled = getCurrentInstalled();
        int max = Math.min(getFocusedElements(), currentInstalled.size());
        for (int i = 0; i < max; i++) {
            drawName(guiGraphics, currentInstalled.get(currentSelection + i), 3 + i * elementHeight);
        }
    }

    protected void drawNameText(GuiGraphicsExtractor guiGraphics, int y, Component name, int color, float scale) {
        drawScaledScrollingString(guiGraphics, name, 13, y, TextAlignment.LEFT, color, barXShift - 16, 0, false, scale);
    }

    @Override
    protected ScreenRectangle getTooltipRectangle(int mouseX, int mouseY) {
        return cachedTooltipRect == null ? super.getTooltipRectangle(mouseX, mouseY) : cachedTooltipRect;
    }

    protected int getColor(TYPE type) {
        return CommonColors.WHITE;
    }

    @Override
    public void renderElements(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTicks) {
        //Draw elements
        List<TYPE> currentInstalled = getCurrentInstalled();
        int currentSelection = getCurrentSelection();
        int max = Math.min(getFocusedElements(), currentInstalled.size());
        for (int i = 0; i < max; i++) {
            TYPE type = currentInstalled.get(currentSelection + i);
            int multipliedElement = i * elementHeight;
            int shiftedY = getY() + 1 + multipliedElement;
            Identifier texture;
            Identifier slotTexture;
            if (type == getSelection()) {
                texture = SELECTED;
                slotTexture = SELECTED_SLOT;
            } else if (mouseX >= getX() + 1 && mouseX < getX() + barXShift - 1 && mouseY >= shiftedY && mouseY < shiftedY + elementHeight) {
                texture = HOVERED;
                slotTexture = HOVERED_SLOT;
            } else {
                texture = BASE;
                slotTexture = BASE_SLOT;
            }
            int color = getColor(type);
            int targetX = relativeX + 1;
            int targetY = relativeY + 1 + multipliedElement;
            guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, texture, targetX, targetY, width - 8, elementHeight, color);
            guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, slotTexture, targetX + 1, targetY + 1, 10, 10, color);
            gui().renderItem(guiGraphics, getRenderStack(type), relativeX + 3, relativeY + 3 + i * elementHeight, 0.5F);
        }
    }
}