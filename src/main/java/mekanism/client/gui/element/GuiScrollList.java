package mekanism.client.gui.element;

import java.util.ArrayList;
import java.util.List;
import mekanism.client.gui.IGuiWrapper;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;

//TODO: Potentially rewrite how various parts of this work to be cleaner, because as it stands right now
// how the scroll bar is handled is a mess, even though it does "work" properly now
public class GuiScrollList extends GuiTexturedElement {

    private static int textureWidth = 6;
    private static int textureHeight = 6;

    private final GuiInnerScreen innerScreen;
    private List<String> textEntries = new ArrayList<>();
    private double dragOffset = 0;
    private int selected = -1;
    private double scroll;

    public GuiScrollList(IGuiWrapper gui, int x, int y, int width, int height) {
        super(MekanismUtils.getResource(ResourceType.GUI_ELEMENT, "scroll_list.png"), gui, x, y, width, height);
        innerScreen = new GuiInnerScreen(gui, x - 1, y - 1, width + 2, height + 2);
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

    private int getElementCount() {
        //TODO: Maybe come up with a better name, basically is a method that gets how many elements can be rendered at once
        return height / 10;
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
        if (!canScroll()) {
            scroll = 0;
        }
    }

    private boolean canScroll() {
        return !textEntries.isEmpty() && textEntries.size() > getElementCount();
    }

    private double setScroll(double scroll) {
        int elements = getElementCount();
        int size = textEntries.size();
        int nextScrollIndex = (int) ((size * scroll) - ((float) elements / (float) size) * scroll);
        if (nextScrollIndex < 0) {
            scroll = 0;
        } else if (nextScrollIndex + elements > size) {
            //TODO: Do this better??
            return this.scroll;
        }
        this.scroll = scroll;
        return this.scroll;
    }

    @Override
    public void renderButton(int mouseX, int mouseY, float partialTicks) {
        //Draw Black and border
        innerScreen.renderButton(mouseX, mouseY, partialTicks);
        minecraft.textureManager.bindTexture(getResource());
        //Draw Selected
        int scroll = getScrollIndex();
        if (selected != -1 && selected >= scroll && selected <= scroll + getElementCount() - 1) {
            guiObj.drawModalRectWithCustomSizedTexture(x, y + (selected - scroll) * 10, width, 10, 4, 2, 2, 2, textureWidth, textureHeight);
        }
        //Draw Scroll
        drawScroll();
        //Render the text into the entries
        if (!textEntries.isEmpty()) {
            for (int i = 0; i < getElementCount(); i++) {
                int index = scroll + i;
                if (index <= textEntries.size() - 1) {
                    renderScaledText(textEntries.get(index), x + 1, y + 1 + 10 * i, 0x00CD00, width - 6);
                }
            }
        }
    }

    private void drawScroll() {
        int xStart = x + width - 6;
        //Top
        guiObj.drawModalRectWithCustomSizedTexture(xStart, y, 0, 0, 6, 1, textureWidth, textureHeight);
        //Middle
        guiObj.drawModalRectWithCustomSizedTexture(xStart, y + 1, 6, height - 2, 0, 1, 6, 1, textureWidth, textureHeight);
        //Bottom
        guiObj.drawModalRectWithCustomSizedTexture(xStart, y + height - 1, 0, 0, 6, 1, textureWidth, textureHeight);
        //Scroll bar
        guiObj.drawModalRectWithCustomSizedTexture(xStart + 1, y + 1 + getScroll(), 0, 2, 4, 4, textureWidth, textureHeight);
    }

    private int getMaxScroll() {
        return height - 2;
    }

    private int getScroll() {
        if (!canScroll()) {
            return 0;
        }
        int elements = getElementCount();
        int size = textEntries.size();
        double scrollIndex = size * scroll - elements * scroll / size;
        int max = getMaxScroll() - 4;
        return Math.max(Math.min((int) Math.round(max * scrollIndex / (size - elements)), max), 0);
    }

    private int getScrollIndex() {
        if (!canScroll()) {
            return 0;
        }
        return (int) ((textEntries.size() * scroll) - ((float) getElementCount() / (float) textEntries.size()) * scroll);
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        int xStart = x + width - 5;
        int yStart = y + 1 + getScroll();
        if (mouseX >= xStart && mouseX <= xStart + 4 && mouseY >= yStart && mouseY <= yStart + 4) {
            if (canScroll()) {
                dragOffset = mouseY - yStart;
            }
        } else if (mouseX >= x && mouseX <= x + width - 6 && mouseY >= y && mouseY <= y + height) {
            int index = getScrollIndex();
            clearSelection();
            for (int i = 0; i < getElementCount(); i++) {
                if (index + i <= textEntries.size() - 1) {
                    if (mouseY >= y + 10 * i && mouseY <= y + 10 + 10 * i) {
                        selected = index + i;
                        break;
                    }
                }
            }
        }
    }

    @Override
    public void onDrag(double mouseX, double mouseY, double mouseXOld, double mouseYOld) {
        if (canScroll()) {
            setScroll(Math.min(Math.max((mouseY - (y + 1) - dragOffset) / (float) (getMaxScroll() - 4), 0), 1));
        }
    }

    @Override
    public void onRelease(double mouseX, double mouseY) {
        dragOffset = 0;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (canScroll() && mouseX > x && mouseX < x + width && mouseY > y && mouseY < y + height) {
            double newScroll = Math.min(Math.max(scroll - delta / textEntries.size(), 0), 1);
            return setScroll(newScroll) == newScroll;
        }
        return false;
    }
}