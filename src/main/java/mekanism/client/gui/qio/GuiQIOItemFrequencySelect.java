package mekanism.client.gui.qio;

import mekanism.client.gui.GuiMekanism;
import mekanism.client.gui.element.button.MekanismImageButton;
import mekanism.client.gui.element.custom.GuiFrequencySelector;
import mekanism.client.gui.element.custom.GuiFrequencySelector.IGuiColorFrequencySelector;
import mekanism.client.gui.element.custom.GuiFrequencySelector.IItemGuiFrequencySelector;
import mekanism.client.gui.tooltip.TooltipUtils;
import mekanism.common.content.qio.QIOFrequency;
import mekanism.common.inventory.container.item.QIOFrequencySelectItemContainer;
import mekanism.common.lib.frequency.FrequencyType;
import mekanism.common.network.PacketUtils;
import mekanism.common.network.to_server.button.PacketItemButtonPress;
import mekanism.common.network.to_server.button.PacketItemButtonPress.ClickedItemButton;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.NotNull;

public class GuiQIOItemFrequencySelect extends GuiMekanism<QIOFrequencySelectItemContainer> implements IGuiColorFrequencySelector<QIOFrequency>,
      IItemGuiFrequencySelector<QIOFrequency, QIOFrequencySelectItemContainer> {

    public GuiQIOItemFrequencySelect(QIOFrequencySelectItemContainer container, Inventory inv, Component title) {
        super(container, inv, title);
        imageHeight -= 11;
        titleLabelY = 5;
    }

    @Override
    protected void addGuiElements() {
        super.addGuiElements();
        addRenderableWidget(new GuiFrequencySelector<>(this, 17));
        addRenderableWidget(new MekanismImageButton(this, 6, 6, 14, getButtonLocation("back"),
              (element, mouseX, mouseY) -> PacketUtils.sendToServer(new PacketItemButtonPress(ClickedItemButton.BACK_BUTTON, ((GuiQIOItemFrequencySelect) element.gui()).menu.getHand()))))
              .setTooltip(TooltipUtils.BACK);
    }

    @Override
    protected void drawForegroundText(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY) {
        renderTitleTextWithOffset(guiGraphics, 17);//Adjust spacing for back button
        super.drawForegroundText(guiGraphics, mouseX, mouseY);
    }

    @Override
    public FrequencyType<QIOFrequency> getFrequencyType() {
        return FrequencyType.QIO;
    }

    @Override
    public QIOFrequencySelectItemContainer getFrequencyContainer() {
        return menu;
    }
}