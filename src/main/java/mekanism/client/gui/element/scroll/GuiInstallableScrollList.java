package mekanism.client.gui.element.scroll;

import java.util.List;
import mekanism.api.text.EnumColor;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.render.MekanismRenderer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class GuiInstallableScrollList<TYPE> extends GuiScrollList {

    private final ResourceLocation texture;
    private final int textureWidth;
    private final int textureHeight;
    @Nullable
    protected TYPE selectedType;
    @Nullable
    protected ScreenRectangle cachedTooltipRect;

    protected GuiInstallableScrollList(IGuiWrapper gui, int x, int y, int height, ResourceLocation background, int backgroundSideSize,
          ResourceLocation texture, int textureWidth, int textureHeight) {
        super(gui, x, y, textureWidth + 8, height, textureHeight / 3, background, backgroundSideSize);
        this.texture = texture;
        this.textureWidth = textureWidth;
        this.textureHeight = textureHeight;
    }

    protected abstract List<TYPE> getCurrentInstalled();

    protected abstract void drawName(GuiGraphics guiGraphics, TYPE type, int multipliedElement);

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

    protected abstract void setSelected(TYPE newType);

    @Override
    public void clearSelection() {
        setSelected(null);
    }

    @Override
    public void renderForeground(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        super.renderForeground(guiGraphics, mouseX, mouseY);
        int currentSelection = getCurrentSelection();
        List<TYPE> currentInstalled = getCurrentInstalled();
        int max = Math.min(getFocusedElements(), currentInstalled.size());
        for (int i = 0; i < max; i++) {
            drawName(guiGraphics, currentInstalled.get(currentSelection + i), 3 + i * elementHeight);
        }
    }

    protected void drawNameText(GuiGraphics guiGraphics, int y, Component name, int color, float scale) {
        drawScaledScrollingString(guiGraphics, name, 13, y, TextAlignment.LEFT, color, barXShift - 16, 0, false, scale);
    }

    @NotNull
    @Override
    protected ScreenRectangle getTooltipRectangle(int mouseX, int mouseY) {
        return cachedTooltipRect == null ? super.getTooltipRectangle(mouseX, mouseY) : cachedTooltipRect;
    }

    @Nullable
    protected EnumColor getColor(TYPE type) {
        return null;
    }

    @Override
    public void renderElements(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        //Draw elements
        List<TYPE> currentInstalled = getCurrentInstalled();
        int currentSelection = getCurrentSelection();
        int max = Math.min(getFocusedElements(), currentInstalled.size());
        for (int i = 0; i < max; i++) {
            TYPE type = currentInstalled.get(currentSelection + i);
            int multipliedElement = i * elementHeight;
            int shiftedY = getY() + 1 + multipliedElement;
            int j = 1;
            if (type == getSelection()) {
                j = 2;
            } else if (mouseX >= getX() + 1 && mouseX < getX() + barXShift - 1 && mouseY >= shiftedY && mouseY < shiftedY + elementHeight) {
                j = 0;
            }
            EnumColor color = getColor(type);
            if (color != null) {
                MekanismRenderer.color(guiGraphics, color);
            }
            guiGraphics.blit(texture, relativeX + 1, relativeY + 1 + multipliedElement, 0, elementHeight * j, textureWidth,
                  elementHeight, textureWidth, textureHeight);
            if (color != null) {
                MekanismRenderer.resetColor(guiGraphics);
            }
        }
        //Note: This needs to be in its own loop as rendering the items is likely to cause the texture manager to be bound to a different texture
        // and thus would make the selection area background get all screwed up
        for (int i = 0; i < max; i++) {
            TYPE type = currentInstalled.get(currentSelection + i);
            gui().renderItem(guiGraphics, getRenderStack(type), relativeX + 3, relativeY + 3 + i * elementHeight, 0.5F);
        }
    }
}