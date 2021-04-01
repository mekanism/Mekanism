package mekanism.client.gui.element.window;

import com.mojang.blaze3d.matrix.MatrixStack;
import java.util.ArrayList;
import java.util.List;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.GuiRightArrow;
import mekanism.client.gui.element.slot.GuiVirtualSlot;
import mekanism.client.gui.element.slot.SlotType;
import mekanism.common.MekanismLang;
import mekanism.common.inventory.container.QIOItemViewerContainer;
import mekanism.common.inventory.container.SelectedWindowData;
import mekanism.common.inventory.container.SelectedWindowData.WindowType;
import net.minecraft.util.text.ITextComponent;

public class GuiCraftingWindow extends GuiWindow {

    private final List<GuiVirtualSlot> slots;
    private final byte index;

    public GuiCraftingWindow(IGuiWrapper gui, int x, int y, QIOItemViewerContainer container, byte index) {
        super(gui, x, y, 118, 80, new SelectedWindowData(WindowType.CRAFTING, index));
        this.index = index;
        interactionStrategy = InteractionStrategy.ALL;
        slots = new ArrayList<>();
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 3; column++) {
                slots.add(addChild(new GuiVirtualSlot(SlotType.NORMAL, gui, relativeX + 8 + column * 18, relativeY + 18 + row * 18,
                      container.getCraftingWindowSlot(this.index, row * 3 + column))));
            }
        }
        addChild(new GuiRightArrow(gui, relativeX + 66, relativeY + 38).jeiCrafting());
        slots.add(addChild(new GuiVirtualSlot(SlotType.NORMAL, gui, relativeX + 92, relativeY + 36, container.getCraftingWindowSlot(this.index, 9))));
    }

    public void updateContainer(QIOItemViewerContainer container) {
        //Lookup the slots again and update the stored lookup method
        for (int i = 0; i < slots.size(); i++) {
            slots.get(i).updateVirtualSlot(container.getCraftingWindowSlot(index, i));
        }
    }

    public byte getIndex() {
        return index;
    }

    @Override
    public void renderForeground(MatrixStack matrix, int mouseX, int mouseY) {
        super.renderForeground(matrix, mouseX, mouseY);
        //Increment index by one so we show: 1, 2, and 3 instead of 0, 1, and 2
        // Note: We do some of the math here locally instead of using drawTitleText so that we
        // can shift it slightly further away from the button and make it look slightly better
        ITextComponent title = MekanismLang.CRAFTING_WINDOW.translate(index + 1);
        int maxLength = getXSize() - 10;
        float scale = Math.min(1, maxLength / getStringWidth(title));
        float left = relativeX + getXSize() / 2F;
        drawScaledCenteredText(matrix, title, left + 2, relativeY + 6, titleTextColor(), scale);
    }
}