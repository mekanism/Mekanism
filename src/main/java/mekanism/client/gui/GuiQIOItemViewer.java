package mekanism.client.gui;

import java.util.ArrayList;
import java.util.List;
import mekanism.client.gui.element.GuiInnerScreen;
import mekanism.client.gui.element.GuiSlotScroll;
import mekanism.common.MekanismLang;
import mekanism.common.config.MekanismConfig;
import mekanism.common.frequency.Frequency.FrequencyIdentity;
import mekanism.common.inventory.container.QIOItemViewerContainer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.IHasContainer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.text.ITextComponent;

public abstract class GuiQIOItemViewer<CONTAINER extends QIOItemViewerContainer> extends GuiMekanism<CONTAINER> {

    protected GuiQIOItemViewer(CONTAINER container, PlayerInventory inv, ITextComponent title) {
        super(container, inv, title);
        xSize = 16 + MekanismConfig.client.qioItemViewerSlotsX.get() * 18 + 18;
        ySize = QIOItemViewerContainer.SLOTS_START_Y + MekanismConfig.client.qioItemViewerSlotsY.get() * 18 + 96;
    }

    @Override
    public void init() {
        super.init();
        addButton(new GuiInnerScreen(this, 8, 14, xSize - 16, 11, () -> {
            List<ITextComponent> list = new ArrayList<>();
            FrequencyIdentity freq = getFrequency();
            if (freq != null) {
                list.add(MekanismLang.FREQUENCY.translate(getFrequency().getKey()));
            } else {
                list.add(MekanismLang.NO_FREQUENCY.translate());
            }
            return list;
        }));
        addButton(new GuiSlotScroll(this, 8, QIOItemViewerContainer.SLOTS_START_Y, MekanismConfig.client.qioItemViewerSlotsX.get(), MekanismConfig.client.qioItemViewerSlotsY.get(),
              () -> container.getQIOItemList(), container));
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        drawString(MekanismLang.INVENTORY.translate(), 8, (getYSize() - 96) + 2, titleTextColor());
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
    }

    public abstract FrequencyIdentity getFrequency();

    private void resize(ResizeType type) {
        int sizeX = MekanismConfig.client.qioItemViewerSlotsX.get(), sizeY = MekanismConfig.client.qioItemViewerSlotsY.get();
        if (type == ResizeType.EXPAND_X && sizeX < QIOItemViewerContainer.SLOTS_X_MAX) {
            MekanismConfig.client.qioItemViewerSlotsX.set(sizeX + 1);
        } else if (type == ResizeType.EXPAND_Y && sizeY < QIOItemViewerContainer.SLOTS_Y_MAX) {
            MekanismConfig.client.qioItemViewerSlotsY.set(sizeY + 1);
        } else if (type == ResizeType.SHRINK_X && sizeX > QIOItemViewerContainer.SLOTS_X_MIN) {
            MekanismConfig.client.qioItemViewerSlotsX.set(sizeX - 1);
        } else if (type == ResizeType.SHRINK_Y && sizeY > QIOItemViewerContainer.SLOTS_Y_MIN) {
            MekanismConfig.client.qioItemViewerSlotsY.set(sizeY - 1);
        }
        // save the updated config info
        MekanismConfig.client.getConfigSpec().save();
        // here we subtly recreate the entire interface + container, maintaining the same window ID
        @SuppressWarnings("unchecked")
        CONTAINER c = (CONTAINER) container.recreate();
        Screen s = recreate(c);
        Minecraft.getInstance().player.openContainer = ((IHasContainer<?>)s).getContainer();
        Minecraft.getInstance().displayGuiScreen(recreate(c));
    }

    public abstract GuiQIOItemViewer<CONTAINER> recreate(CONTAINER container);

    private enum ResizeType {
        EXPAND_X,
        EXPAND_Y,
        SHRINK_X,
        SHRINK_Y;
    }
}
