package mekanism.client.gui;

import java.util.List;
import mekanism.client.gui.element.gauge.GaugeType;
import mekanism.client.gui.element.gauge.GuiEnergyGauge;
import mekanism.client.gui.element.tab.GuiAmplifierTab;
import mekanism.client.gui.element.tab.GuiEnergyTab;
import mekanism.client.gui.element.text.GuiTextField;
import mekanism.common.MekanismLang;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import mekanism.common.network.PacketUtils;
import mekanism.common.network.to_server.PacketGuiInteract;
import mekanism.common.network.to_server.PacketGuiInteract.GuiInteraction;
import mekanism.common.tile.laser.TileEntityLaserAmplifier;
import mekanism.common.util.text.EnergyDisplay;
import mekanism.common.util.text.InputValidator;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.jspecify.annotations.Nullable;

public class GuiLaserAmplifier extends GuiMekanismTile<TileEntityLaserAmplifier, MekanismTileContainer<TileEntityLaserAmplifier>> {

    private static final int TEXT_BOX_START = 96;
    @Nullable
    private GuiEnergyGauge energyGauge;

    public GuiLaserAmplifier(MekanismTileContainer<TileEntityLaserAmplifier> container, Inventory inv, Component title) {
        super(container, inv, title);
        dynamicSlots = true;
    }

    @Override
    protected void addGuiElements() {
        super.addGuiElements();
        energyGauge = addRenderableWidget(new GuiEnergyGauge(tile.energyContainer(), GaugeType.STANDARD, this, 6, 10));
        addRenderableWidget(new GuiEnergyTab(this, () -> List.of(MekanismLang.STORING.translate(EnergyDisplay.of(tile.energyContainer())))));
        addRenderableWidget(new GuiAmplifierTab(this, tile));
        addRenderableWidget(new GuiTextField(this, TEXT_BOX_START, 28, 36, 11))
              .setMaxLength(4)
              .setEnterHandler(text -> setText(text, GuiInteraction.SET_TIME))
              .setInputValidator(InputValidator.DIGIT);
        addRenderableWidget(new GuiTextField(this, TEXT_BOX_START, 43, 72, 11))
              .setMaxLength(10)
              .setEnterHandler(text -> setText(text, GuiInteraction.MIN_THRESHOLD))
              .setInputValidator(InputValidator.DIGIT);
        addRenderableWidget(new GuiTextField(this, TEXT_BOX_START, 58, 72, 11))
              .setMaxLength(10)
              .setEnterHandler(text -> setText(text, GuiInteraction.MAX_THRESHOLD))
              .setInputValidator(InputValidator.DIGIT);
    }

    @Override
    protected void drawForegroundText(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY) {
        int start = energyGauge == null ? 0 : energyGauge.getRelativeRight();
        renderTitleTextWithOffset(guiGraphics, start);
        renderInventoryText(guiGraphics);
        Component delay;
        if (tile.getDelay() > 0) {
            delay = MekanismLang.DELAY.translate(tile.getDelay());
        } else {
            delay = MekanismLang.NO_DELAY.translate();
        }
        drawScrollingString(guiGraphics, delay, start, 30, TextAlignment.LEFT, titleTextColor(), TEXT_BOX_START - start, 2, false);
        drawScrollingString(guiGraphics, MekanismLang.MIN.translate(EnergyDisplay.of(tile.getMinThreshold())), start, 45, TextAlignment.LEFT, titleTextColor(), TEXT_BOX_START - start, 2, false);
        drawScrollingString(guiGraphics, MekanismLang.MAX.translate(EnergyDisplay.of(tile.getMaxThreshold())), start, 60, TextAlignment.LEFT, titleTextColor(), TEXT_BOX_START - start, 2, false);
        super.drawForegroundText(guiGraphics, mouseX, mouseY);
    }

    private void setText(GuiTextField text, GuiInteraction interaction) {
        if (!text.getText().isEmpty()) {
            try {
                int value = Math.max(0, Integer.parseInt(text.getText()));
                PacketUtils.sendToServer(new PacketGuiInteract(interaction, tile, value));
            } catch (NumberFormatException _) {
            }
            text.setText("");
        }
    }
}