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
public class GuiScrollList extends GuiElement {

    private List<String> textEntries = new ArrayList<>();
    private boolean isDragging;
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

    public void setText(List<String> text) {
        if (text == null) {
            textEntries.clear();
            return;
        }

        if (selected > text.size() - 1) {
            clearSelection();
        }

        textEntries = text;

        if (textEntries.size() <= height) {
            scroll = 0;
        }
    }

    @Override
    public void renderButton(int mouseX, int mouseY, float partialTicks) {
        minecraft.textureManager.bindTexture(RESOURCE);
        drawBlack();
        drawSelected(selected);
        minecraft.textureManager.bindTexture(defaultLocation);
    }

    public void drawBlack() {
        int xDisplays = width / 10 + (width % 10 > 0 ? 1 : 0);

        for (int yIter = 0; yIter < height; yIter++) {
            for (int xIter = 0; xIter < xDisplays; xIter++) {
                int widthCalculated = width % 10 > 0 && xIter == xDisplays - 1 ? width % 10 : 10;
                guiObj.drawTexturedRect(x + (xIter * 10), y + (yIter * 10), 0, 0, widthCalculated, 10);
            }
        }
    }

    public void drawSelected(int index) {
        int scroll = getScrollIndex();

        if (selected != -1 && index >= scroll && index <= scroll + height - 1) {
            int xDisplays = width / 10 + (width % 10 > 0 ? 1 : 0);

            for (int xIter = 0; xIter < xDisplays; xIter++) {
                int widthCalculated = width % 10 > 0 && xIter == xDisplays - 1 ? width % 10 : 10;
                guiObj.drawTexturedRect(x + (xIter * 10), y + (index - scroll) * 10, 0, 10, widthCalculated, 10);
            }
        }
    }

    public void drawScroll() {
        int xStart = x + width - 6;
        int yStart = y;

        for (int i = 0; i < height; i++) {
            guiObj.drawTexturedRect(xStart, yStart + (i * 10), 10, 1, 6, 10);
        }

        guiObj.drawTexturedRect(xStart, yStart, 10, 0, 6, 1);
        guiObj.drawTexturedRect(xStart, yStart + (height * 10) - 1, 10, 0, 6, 1);

        guiObj.drawTexturedRect(xStart + 1, yStart + 1 + getScroll(), 16, 0, 4, 4);
    }

    public int getMaxScroll() {
        return (height * 10) - 2;
    }

    public int getScroll() {
        return Math.max(Math.min((int) (scroll * (getMaxScroll() - 4)), getMaxScroll() - 4), 0);
    }

    public int getScrollIndex() {
        if (textEntries.size() <= height) {
            return 0;
        }
        return (int) ((textEntries.size() * scroll) - ((float) height / (float) textEntries.size()) * scroll);
    }

    @Override
    public void renderToolTip(int mouseX, int mouseY) {
        //TODO: Some of this should be in renderButton
        if (!textEntries.isEmpty()) {
            for (int i = 0; i < height; i++) {
                int index = getScrollIndex() + i;
                if (index <= textEntries.size() - 1) {
                    renderScaledText(textEntries.get(index), x + 1, y + 1 + (10 * i), 0x00CD00, width - 6);
                }
            }
        }

        minecraft.textureManager.bindTexture(RESOURCE);
        drawScroll();
        minecraft.textureManager.bindTexture(defaultLocation);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            int xStart = x + width - 5;

            if (mouseX >= xStart && mouseX <= xStart + 4 && mouseY >= getScroll() + y + 1 && mouseY <= getScroll() + 4 + y + 1) {
                if (textEntries.size() > height) {
                    dragOffset = mouseY - (getScroll() + y + 1);
                    isDragging = true;
                    return true;
                }
            } else if (mouseX >= x && mouseX <= x + width - 6 && mouseY >= y && mouseY <= y + height * 10) {
                int index = getScrollIndex();
                clearSelection();
                for (int i = 0; i < height; i++) {
                    if (index + i <= textEntries.size() - 1) {
                        if (mouseY >= (y + i * 10) && mouseY <= (y + i * 10 + 10)) {
                            selected = index + i;
                            break;
                        }
                    }
                }
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double mouseXOld, double mouseYOld) {
        //TODO: mouseXOld and mouseYOld are just guessed mappings I couldn't find any usage from a quick glance. look closer
        super.mouseDragged(mouseX, mouseY, button, mouseXOld, mouseYOld);
        if (isDragging) {
            scroll = Math.min(Math.max((mouseY - (y + 1) - dragOffset) / (float) (getMaxScroll() - 4), 0), 1);
        }
        return true;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int type) {
        super.mouseReleased(mouseX, mouseY, type);
        if (type == 0) {
            if (isDragging) {
                dragOffset = 0;
                isDragging = false;
            }
        }
        return true;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (mouseX > x && mouseX < x + width && mouseY > y && mouseY < y + height * 10) {
            // 120 = DirectInput factor for one notch. Linux/OSX LWGL scale accordingly
            scroll = Math.min(Math.max(scroll - (delta / 120F) * (1F / textEntries.size()), 0), 1);
            drawScroll();
            return true;
        }
        return false;
    }
}