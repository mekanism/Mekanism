package mekanism.client.gui.element;

import java.util.ArrayList;
import java.util.List;
import mekanism.client.gui.IGuiWrapper;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class GuiScrollList extends GuiTexturedElement {

    private List<String> textEntries = new ArrayList<>();
    //TODO: Fix dragging
    private double dragOffset = 0;
    private int selected = -1;
    private double scroll;

    public GuiScrollList(IGuiWrapper gui, ResourceLocation def, int x, int y, int width, int height) {
        super(MekanismUtils.getResource(ResourceType.GUI_ELEMENT, "scroll_list.png"), gui, def, x, y, width, height);
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
        return textEntries.size() > getElementCount();
    }

    @Override
    public void renderButton(int mouseX, int mouseY, float partialTicks) {
        minecraft.textureManager.bindTexture(getResource());
        //Draw Black
        guiObj.drawModalRectWithCustomSizedTexture(x, y, width, height, 0, 0, 10, 10, 20, 20);
        //Draw Selected
        int scroll = getScrollIndex();
        if (selected != -1 && selected >= scroll && selected <= scroll + getElementCount() - 1) {
            guiObj.drawModalRectWithCustomSizedTexture(x, y + (selected - scroll) * 10, width, 10, 0, 10, 10, 10, 20, 20);
        }
        //Draw Scroll
        drawScroll();
        minecraft.textureManager.bindTexture(defaultLocation);
        //Render the text into the entries
        if (!textEntries.isEmpty()) {
            for (int i = 0; i < getElementCount(); i++) {
                int index = getScrollIndex() + i;
                if (index <= textEntries.size() - 1) {
                    renderScaledText(textEntries.get(index), x + 1, y + 1 + (10 * i), 0x00CD00, width - 6);
                }
            }
        }
    }

    private void drawScroll() {
        int xStart = x + width - 6;
        //Top
        guiObj.drawModalRectWithCustomSizedTexture(xStart, y, 10, 0, 6, 1, 20, 20);
        //Middle
        guiObj.drawModalRectWithCustomSizedTexture(xStart, y + 1, 6, height - 2, 10, 1, 6, 10, 20, 20);
        //Bottom
        guiObj.drawModalRectWithCustomSizedTexture(xStart, y + height - 1, 10, 0, 6, 1, 20, 20);

        guiObj.drawModalRectWithCustomSizedTexture(xStart + 1, y + 1 + getScroll(), 16, 0, 4, 4, 20, 20);
    }

    private int getMaxScroll() {
        return height - 2;
    }

    private int getScroll() {
        int max = getMaxScroll() - 4;
        return Math.max(Math.min((int) (scroll * max), max), 0);
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
                    if (mouseY >= (y + i * 10) && mouseY <= (y + i * 10 + 10)) {
                        selected = index + i;
                        break;
                    }
                }
            }
        }
    }

    @Override
    protected void onDrag(double mouseX, double mouseY, double mouseXOld, double mouseYOld) {
        if (canScroll()) {
            scroll = Math.min(Math.max((mouseY - (y + 1) - dragOffset) / (float) (getMaxScroll() - 4), 0), 1);
        }
    }

    @Override
    public void onRelease(double mouseX, double mouseY) {
        dragOffset = 0;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (canScroll() && mouseX > x && mouseX < x + width && mouseY > y && mouseY < y + height) {
            scroll = Math.min(Math.max(scroll - delta / textEntries.size(), 0), 1);
            return true;
        }
        return false;
    }
}