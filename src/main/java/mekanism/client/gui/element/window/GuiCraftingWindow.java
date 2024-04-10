package mekanism.client.gui.element.window;

import java.util.ArrayList;
import java.util.List;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.GuiRightArrow;
import mekanism.client.gui.element.button.MekanismImageButton;
import mekanism.client.gui.element.slot.GuiVirtualSlot;
import mekanism.client.gui.element.slot.SlotType;
import mekanism.common.MekanismLang;
import mekanism.common.content.qio.IQIOCraftingWindowHolder;
import mekanism.common.inventory.container.QIOItemViewerContainer;
import mekanism.common.inventory.container.SelectedWindowData;
import mekanism.common.inventory.container.SelectedWindowData.WindowType;
import mekanism.common.network.PacketUtils;
import mekanism.common.network.to_server.qio.PacketQIOClearCraftingWindow;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;

public class GuiCraftingWindow extends GuiWindow {

    private final List<GuiVirtualSlot> slots;
    private final byte index;
    private QIOItemViewerContainer container;

    public GuiCraftingWindow(IGuiWrapper gui, int x, int y, QIOItemViewerContainer container, SelectedWindowData windowData) {
        super(gui, x, y, 124, 80, windowData);
        if (windowData.type != WindowType.CRAFTING) {
            throw new IllegalArgumentException("Crafting windows must have a crafting window type");
        } else if (windowData.extraData >= IQIOCraftingWindowHolder.MAX_CRAFTING_WINDOWS) {
            throw new IllegalArgumentException("Crafting window index is too high");
        }
        this.index = windowData.extraData;
        this.container = container;
        interactionStrategy = InteractionStrategy.ALL;
        slots = new ArrayList<>();
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 3; column++) {
                slots.add(addChild(new GuiVirtualSlot(this, SlotType.NORMAL, gui, relativeX + 8 + column * 18, relativeY + 18 + row * 18,
                      this.container.getCraftingWindowSlot(this.index, row * 3 + column))));
            }
        }
        addChild(new GuiRightArrow(gui, relativeX + 70, relativeY + 38).recipeViewerCrafting());
        slots.add(addChild(new GuiVirtualSlot(this, SlotType.NORMAL, gui, relativeX + 100, relativeY + 36,
              this.container.getCraftingWindowSlot(this.index, 9))));
        addChild(new MekanismImageButton(gui, relativeX + width - 20, relativeY + height - 20, 14, getButtonLocation("clear_sides"),
              (element, mouseX, mouseY) -> PacketUtils.sendToServer(new PacketQIOClearCraftingWindow(index, Screen.hasShiftDown()))))
              .setTooltip(MekanismLang.CRAFTING_WINDOW_CLEAR);
    }

    public void updateContainer(QIOItemViewerContainer container) {
        //Update container so that it can properly proxy the call to the container so that it can be synced properly based on tile vs item impl
        this.container = container;
        //Lookup the slots again and update the stored lookup method
        for (int i = 0; i < slots.size(); i++) {
            slots.get(i).updateVirtualSlot(this, this.container.getCraftingWindowSlot(index, i));
        }
    }

    public byte getIndex() {
        return index;
    }

    @Override
    public void renderForeground(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        super.renderForeground(guiGraphics, mouseX, mouseY);
        //Increment index by one, so we show: 1, 2, and 3 instead of 0, 1, and 2
        drawTitleText(guiGraphics, MekanismLang.CRAFTING_WINDOW.translate(index + 1), 6);
    }
}