package mekanism.client.gui;

import java.math.BigDecimal;
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
import mekanism.common.network.to_server.PacketGuiSetEnergy;
import mekanism.common.network.to_server.PacketGuiSetEnergy.GuiEnergyValue;
import mekanism.common.tile.laser.TileEntityLaserAmplifier;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.text.EnergyDisplay;
import mekanism.common.util.text.InputValidator;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.NotNull;

public class GuiLaserAmplifier extends GuiMekanismTile<TileEntityLaserAmplifier, MekanismTileContainer<TileEntityLaserAmplifier>> {

    private GuiTextField minField, maxField, timerField;
    private GuiEnergyGauge energyGauge;

    public GuiLaserAmplifier(MekanismTileContainer<TileEntityLaserAmplifier> container, Inventory inv, Component title) {
        super(container, inv, title);
        dynamicSlots = true;
    }

    @Override
    protected void addGuiElements() {
        super.addGuiElements();
        energyGauge = addRenderableWidget(new GuiEnergyGauge(tile.getEnergyContainer(), GaugeType.STANDARD, this, 6, 10));
        addRenderableWidget(new GuiEnergyTab(this, () -> List.of(MekanismLang.STORING.translate(EnergyDisplay.of(tile.getEnergyContainer())))));
        addRenderableWidget(new GuiAmplifierTab(this, tile));
        timerField = addRenderableWidget(new GuiTextField(this, 96, 28, 36, 11));
        timerField.setMaxLength(4);
        timerField.setEnterHandler(this::setTime);
        timerField.setInputValidator(InputValidator.DIGIT);
        minField = addRenderableWidget(new GuiTextField(this, 96, 43, 72, 11));
        minField.setMaxLength(10);
        minField.setEnterHandler(this::setMinThreshold);
        minField.setInputValidator(InputValidator.SCI_NOTATION);
        maxField = addRenderableWidget(new GuiTextField(this, 96, 58, 72, 11));
        maxField.setMaxLength(10);
        maxField.setEnterHandler(this::setMaxThreshold);
        maxField.setInputValidator(InputValidator.SCI_NOTATION);
    }

    @Override
    protected void drawForegroundText(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY) {
        renderTitleTextWithOffset(guiGraphics, energyGauge.getRelativeRight());
        renderInventoryText(guiGraphics);
        int start = energyGauge.getRelativeRight();
        Component delay;
        if (tile.getDelay() > 0) {
            delay = MekanismLang.DELAY.translate(tile.getDelay());
        } else {
            delay = MekanismLang.NO_DELAY.translate();
        }
        drawScrollingString(guiGraphics, delay, start, 30, TextAlignment.LEFT, titleTextColor(), timerField.getRelativeX() - start, 2, false);
        drawScrollingString(guiGraphics, MekanismLang.MIN.translate(EnergyDisplay.of(tile.getMinThreshold())), start, 45, TextAlignment.LEFT, titleTextColor(), minField.getRelativeX() - start, 2, false);
        drawScrollingString(guiGraphics, MekanismLang.MAX.translate(EnergyDisplay.of(tile.getMaxThreshold())), start, 60, TextAlignment.LEFT, titleTextColor(), maxField.getRelativeX() - start, 2, false);
        super.drawForegroundText(guiGraphics, mouseX, mouseY);
    }

    private long parseLong(GuiTextField textField) throws NumberFormatException {
        String text = textField.getText();
        if (text.contains("E")) {
            //TODO: Improve how we handle scientific notation, we currently create a big decimal and then
            // we parse it as a floating long, ideally we could skip the big decimal side of things
            text = new BigDecimal(text).toPlainString();
        }
        return Math.max(0L, Long.parseLong(text));
    }

    private void setMinThreshold() {
        if (!minField.getText().isEmpty()) {
            try {
                PacketUtils.sendToServer(new PacketGuiSetEnergy(GuiEnergyValue.MIN_THRESHOLD, tile.getBlockPos(),
                      MekanismUtils.convertToJoules(parseLong(minField))));
            } catch (NumberFormatException ignored) {
            }
            minField.setText("");
        }
    }

    private void setMaxThreshold() {
        if (!maxField.getText().isEmpty()) {
            try {
                PacketUtils.sendToServer(new PacketGuiSetEnergy(GuiEnergyValue.MAX_THRESHOLD, tile.getBlockPos(),
                      MekanismUtils.convertToJoules(parseLong(maxField))));
            } catch (NumberFormatException ignored) {
            }
            maxField.setText("");
        }
    }

    private void setTime() {
        if (!timerField.getText().isEmpty()) {
            try {
                PacketUtils.sendToServer(new PacketGuiInteract(GuiInteraction.SET_TIME, tile, Integer.parseInt(timerField.getText())));
            } catch (NumberFormatException ignored) {
            }
            timerField.setText("");
        }
    }
}