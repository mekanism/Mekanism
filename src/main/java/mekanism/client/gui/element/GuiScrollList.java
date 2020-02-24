package mekanism.client.gui.element;

import java.util.ArrayList;
import java.util.List;
import mekanism.client.gui.IGuiWrapper;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.util.ResourceLocation;

public class GuiScrollList extends GuiScrollableElement {

    private static final ResourceLocation SCROLL_LIST = MekanismUtils.getResource(ResourceType.GUI_ELEMENT, "scroll_list.png");
    private static int TEXTURE_WIDTH = 6;
    private static int TEXTURE_HEIGHT = 6;

    private final GuiInnerScreen innerScreen;
    private List<String> textEntries = new ArrayList<>();
    private int selected = -1;

    public GuiScrollList(IGuiWrapper gui, int x, int y, int width, int height) {
        super(SCROLL_LIST, gui, x, y, width, height, width - 6, 2, 4, 4, height - 4);
        innerScreen = new GuiInnerScreen(gui, x, y, width, height);
    }

    @Override
    protected int getMaxElements() {
        return textEntries.size();
    }

    @Override
    protected int getFocusedElements() {
        return (height - 2) / 10;
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
        int focusedElements = getFocusedElements();
        if (selected != -1 && selected >= scrollIndex && selected <= scrollIndex + focusedElements - 1) {
            blit(x + 1, y + 1 + (selected - scrollIndex) * 10, barX - x, 10, 4, 2, 2, 2, TEXTURE_WIDTH, TEXTURE_HEIGHT);
        }
        //Draw Scroll
        //Top border
        blit(barX - 1, barY - 1, 0, 0, TEXTURE_WIDTH, 1, TEXTURE_WIDTH, TEXTURE_HEIGHT);
        //Middle border
        blit(barX - 1, barY, 6, maxBarHeight, 0, 1, TEXTURE_WIDTH, 1, TEXTURE_WIDTH, TEXTURE_HEIGHT);
        //Bottom border
        blit(barX - 1, y + maxBarHeight + 2, 0, 0, TEXTURE_WIDTH, 1, TEXTURE_WIDTH, TEXTURE_HEIGHT);
        //Scroll bar
        blit(barX, barY + getScroll(), 0, 2, barWidth, barHeight, TEXTURE_WIDTH, TEXTURE_HEIGHT);
        //Render the text into the entries
        if (!textEntries.isEmpty()) {
            for (int i = 0; i < focusedElements; i++) {
                int index = scrollIndex + i;
                if (index <= textEntries.size() - 1) {
                    renderScaledText(textEntries.get(index), x + 2, y + 2 + 10 * i, 0x00CD00, barX - x - 2);
                }
            }
        }
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        if (mouseX >= x + 1 && mouseX < barX - 1 && mouseY >= y + 1 && mouseY < y + height - 1) {
            int index = getCurrentSelection();
            clearSelection();
            for (int i = 0; i < getFocusedElements(); i++) {
                if (index + i <= textEntries.size() - 1) {
                    int shiftedY = y + 10 * i;
                    if (mouseY >= shiftedY + 1 && mouseY <= shiftedY + 11) {
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
        return isMouseOver(mouseX, mouseY) && adjustScroll(delta);
    }
}