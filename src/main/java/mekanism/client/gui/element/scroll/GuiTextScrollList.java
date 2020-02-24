package mekanism.client.gui.element.scroll;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.GuiInnerScreen;
import net.minecraft.client.gui.AbstractGui;

public class GuiTextScrollList extends GuiScrollList {

    private List<String> textEntries = new ArrayList<>();
    private int selected = -1;

    public GuiTextScrollList(IGuiWrapper gui, int x, int y, int width, int height) {
        super(gui, x, y, width, height, 10, new GuiInnerScreen(gui, x, y, width, height));
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
    public void renderForeground(int mouseX, int mouseY, int xAxis, int yAxis) {
        super.renderForeground(mouseX, mouseY, xAxis, yAxis);
        if (!textEntries.isEmpty()) {
            //Render the text into the entries
            int scrollIndex = getCurrentSelection();
            int focusedElements = getFocusedElements();
            int maxElements = getMaxElements();
            for (int i = 0; i < focusedElements; i++) {
                int index = scrollIndex + i;
                if (index < maxElements) {
                    renderScaledText(textEntries.get(index), relativeX + 2, relativeY + 2 + elementHeight * i, 0x00CD00, barX - x - 2);
                }
            }
        }
    }

    @Override
    public void renderElements(int mouseX, int mouseY, float partialTicks) {
        //Draw Selected
        int scrollIndex = getCurrentSelection();
        if (selected != -1 && selected >= scrollIndex && selected <= scrollIndex + getFocusedElements() - 1) {
            AbstractGui.blit(x + 1, y + 1 + (selected - scrollIndex) * elementHeight, barX - x - 2, elementHeight,
                  4, 2, 2, 2, TEXTURE_WIDTH, TEXTURE_HEIGHT);
        }
    }
}