package mekanism.client.gui.element.scroll;

import java.util.ArrayList;
import java.util.List;
import mekanism.api.text.TextComponentUtil;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.GuiElement;
import mekanism.client.gui.element.GuiInnerScreen;
import net.minecraft.client.gui.GuiGraphics;
import org.jetbrains.annotations.Nullable;

public class GuiTextScrollList extends GuiScrollList {

    private List<String> textEntries = new ArrayList<>();
    private int selected = -1;

    public GuiTextScrollList(IGuiWrapper gui, int x, int y, int width, int height) {
        super(gui, x, y, width, height, 10, GuiInnerScreen.SCREEN, GuiInnerScreen.SCREEN_SIZE);
    }

    @Override
    protected int getMaxElements() {
        return textEntries.size();
    }

    @Override
    public boolean hasSelection() {
        return selected != -1;
    }

    @Override
    protected void setSelected(int index) {
        selected = index;
    }

    public int getSelection() {
        return selected;
    }

    @Override
    public void clearSelection() {
        this.selected = -1;
    }

    public void setText(@Nullable List<String> text) {
        if (text == null) {
            textEntries.clear();
        } else {
            if (selected > text.size() - 1) {
                clearSelection();
            }
            textEntries = text;
        }
        if (!needsScrollBars()) {
            scroll = 0;
        }
    }

    @Override
    public void renderForeground(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        super.renderForeground(guiGraphics, mouseX, mouseY);
        if (!textEntries.isEmpty()) {
            //Render the text into the entries
            int scrollIndex = getCurrentSelection();
            int focusedElements = getFocusedElements();
            int maxElements = getMaxElements();
            for (int i = 0; i < focusedElements; i++) {
                int index = scrollIndex + i;
                if (index < maxElements) {
                    drawScaledScrollingString(guiGraphics, TextComponentUtil.getString(textEntries.get(index)), 0, 2 + elementHeight * i, TextAlignment.LEFT,
                          screenTextColor(), barXShift, 2, false, 0.8F);
                }
            }
        }
    }

    @Override
    public void renderElements(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        //Draw Selected
        int scrollIndex = getCurrentSelection();
        if (selected != -1 && selected >= scrollIndex && selected <= scrollIndex + getFocusedElements() - 1) {
            guiGraphics.blit(getResource(), relativeX + 1, relativeY + 1 + (selected - scrollIndex) * elementHeight, barXShift - 2, elementHeight,
                  4, 2, 2, 2, TEXTURE_WIDTH, TEXTURE_HEIGHT);
        }
    }

    @Override
    public void syncFrom(GuiElement element) {
        GuiTextScrollList old = (GuiTextScrollList) element;
        setText(old.textEntries);
        setSelected(old.getSelection());
        super.syncFrom(element);
    }
}