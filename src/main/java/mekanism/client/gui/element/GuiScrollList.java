package mekanism.client.gui.element;

import java.util.ArrayList;
import java.util.List;
import mekanism.client.gui.IGuiWrapper;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;

public class GuiScrollList extends GuiScrollableElement {

    private static int TEXTURE_WIDTH = 6;
    private static int TEXTURE_HEIGHT = 6;

    private final GuiInnerScreen innerScreen;
    private List<String> textEntries = new ArrayList<>();
    private int selected = -1;

    public GuiScrollList(IGuiWrapper gui, int x, int y, int width, int height) {
        super(MekanismUtils.getResource(ResourceType.GUI_ELEMENT, "scroll_list.png"), gui, x, y, width, height, width - 5, 1, 4, 4);
        //TODO: Include the proper values in our actual thing
        innerScreen = new GuiInnerScreen(gui, x - 1, y - 1, width + 2, height + 2);
    }

    @Override
    protected int getMaxElements() {
        return textEntries.size();
    }

    @Override
    protected int getFocusedElements() {
        return height / 10;
    }

    public boolean hasSelection() {
        return selected != -1;
    }

    public int getSelection() {
        return selected;
    }

    public void clearSelection() {
        this.selected = -1;
    }

    public void setText(List<String> text) {
        if (text == null) {
            textEntries.clear();
            return;
        }
        if (selected > text.size() - 1) {
            clearSelection();
        }
        textEntries = text;
        if (!needsScrollBars()) {
            scroll = 0;
        }
    }

    @Override
    public void renderButton(int mouseX, int mouseY, float partialTicks) {
        //Draw Black and border
        innerScreen.renderButton(mouseX, mouseY, partialTicks);
        minecraft.textureManager.bindTexture(getResource());
        //Draw Selected
        int scrollIndex = getCurrentSelection();
        if (selected != -1 && selected >= scrollIndex && selected <= scrollIndex + getFocusedElements() - 1) {
            blit(x, y + (selected - scrollIndex) * 10, width, 10, 4, 2, 2, 2, TEXTURE_WIDTH, TEXTURE_HEIGHT);
        }
        //Draw Scroll
        int xStart = x + width - 6;
        //Top border
        blit(xStart, y, 0, 0, 6, 1, TEXTURE_WIDTH, TEXTURE_HEIGHT);
        //Middle border
        blit(xStart, y + 1, 6, height - 2, 0, 1, 6, 1, TEXTURE_WIDTH, TEXTURE_HEIGHT);
        //Bottom border
        blit(xStart, y + height - 1, 0, 0, 6, 1, TEXTURE_WIDTH, TEXTURE_HEIGHT);
        //Scroll bar
        blit(xStart + 1, y + 1 + getScroll(), 0, 2, 4, 4, TEXTURE_WIDTH, TEXTURE_HEIGHT);
        //Render the text into the entries
        if (!textEntries.isEmpty()) {
            for (int i = 0; i < getFocusedElements(); i++) {
                int index = scrollIndex + i;
                if (index <= textEntries.size() - 1) {
                    renderScaledText(textEntries.get(index), x + 1, y + 1 + 10 * i, 0x00CD00, width - 6);
                }
            }
        }
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        if (mouseX >= x && mouseX <= x + width - 6 && mouseY >= y && mouseY <= y + height) {
            int index = getCurrentSelection();
            clearSelection();
            for (int i = 0; i < getFocusedElements(); i++) {
                if (index + i <= textEntries.size() - 1) {
                    if (mouseY >= y + 10 * i && mouseY <= y + 10 + 10 * i) {
                        selected = index + i;
                        break;
                    }
                }
            }
        } else {
            super.onClick(mouseX, mouseY);
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (isMouseOver(mouseX, mouseY)) {
            return adjustScroll(delta);
        }
        return false;
    }
}