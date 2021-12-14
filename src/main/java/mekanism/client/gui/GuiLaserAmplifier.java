package mekanism.client.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import java.math.BigDecimal;
import javax.annotation.Nonnull;
import mekanism.api.math.FloatingLong;
import mekanism.client.gui.element.gauge.GaugeType;
import mekanism.client.gui.element.gauge.GuiEnergyGauge;
import mekanism.client.gui.element.tab.GuiAmplifierTab;
import mekanism.client.gui.element.text.GuiTextField;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import mekanism.common.network.to_server.PacketGuiInteract;
import mekanism.common.network.to_server.PacketGuiInteract.GuiInteraction;
import mekanism.common.network.to_server.PacketGuiSetEnergy;
import mekanism.common.network.to_server.PacketGuiSetEnergy.GuiEnergyValue;
import mekanism.common.tile.laser.TileEntityLaserAmplifier;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.text.EnergyDisplay;
import mekanism.common.util.text.InputValidator;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.text.ITextComponent;

public class GuiLaserAmplifier extends GuiMekanismTile<TileEntityLaserAmplifier, MekanismTileContainer<TileEntityLaserAmplifier>> {

    private GuiTextField minField, maxField, timerField;

    public GuiLaserAmplifier(MekanismTileContainer<TileEntityLaserAmplifier> container, PlayerInventory inv, ITextComponent title) {
        super(container, inv, title);
        dynamicSlots = true;
    }

    @Override
    protected void addGuiElements() {
        super.addGuiElements();
        addButton(new GuiEnergyGauge(tile.getEnergyContainer(), GaugeType.STANDARD, this, 6, 10));
        addButton(new GuiAmplifierTab(this, tile));
        timerField = addButton(new GuiTextField(this, 96, 28, 36, 11));
        timerField.setMaxStringLength(4);
        timerField.setEnterHandler(this::setTime);
        timerField.setInputValidator(InputValidator.DIGIT);
        minField = addButton(new GuiTextField(this, 96, 43, 72, 11));
        minField.setMaxStringLength(10);
        minField.setEnterHandler(this::setMinThreshold);
        minField.setInputValidator(InputValidator.SCI_NOTATION);
        maxField = addButton(new GuiTextField(this, 96, 58, 72, 11));
        maxField.setMaxStringLength(10);
        maxField.setEnterHandler(this::setMaxThreshold);
        maxField.setInputValidator(InputValidator.SCI_NOTATION);
    }

    @Override
    protected void drawForegroundText(@Nonnull MatrixStack matrix, int mouseX, int mouseY) {
        renderTitleText(matrix);
        drawString(matrix, inventory.getDisplayName(), inventoryLabelX, inventoryLabelY, titleTextColor());
        if (tile.getDelay() > 0) {
            drawTextScaledBound(matrix, MekanismLang.DELAY.translate(tile.getDelay()), 26, 30, titleTextColor(), 68);
        } else {
            drawTextScaledBound(matrix, MekanismLang.NO_DELAY.translate(), 26, 30, titleTextColor(), 68);
        }
        drawTextScaledBound(matrix, MekanismLang.MIN.translate(EnergyDisplay.of(tile.getMinThreshold())), 26, 45, titleTextColor(), 68);
        drawTextScaledBound(matrix, MekanismLang.MAX.translate(EnergyDisplay.of(tile.getMaxThreshold())), 26, 60, titleTextColor(), 68);
        super.drawForegroundText(matrix, mouseX, mouseY);
    }

    private FloatingLong parseFloatingLong(GuiTextField textField) {
        String text = textField.getText();
        if (text.contains("E")) {
            //TODO: Improve how we handle scientific notation, we currently create a big decimal and then
            // we parse it as a floating long, ideally we could skip the big decimal side of things
            text = new BigDecimal(text).toPlainString();
        }
        return FloatingLong.parseFloatingLong(text);
    }

    private void setMinThreshold() {
        if (!minField.getText().isEmpty()) {
            try {
                Mekanism.packetHandler.sendToServer(new PacketGuiSetEnergy(GuiEnergyValue.MIN_THRESHOLD, tile.getBlockPos(),
                      MekanismUtils.convertToJoules(parseFloatingLong(minField))));
            } catch (NumberFormatException ignored) {
            }
            minField.setText("");
        }
    }

    private void setMaxThreshold() {
        if (!maxField.getText().isEmpty()) {
            try {
                Mekanism.packetHandler.sendToServer(new PacketGuiSetEnergy(GuiEnergyValue.MAX_THRESHOLD, tile.getBlockPos(),
                      MekanismUtils.convertToJoules(parseFloatingLong(maxField))));
            } catch (NumberFormatException ignored) {
            }
            maxField.setText("");
        }
    }

    private void setTime() {
        if (!timerField.getText().isEmpty()) {
            try {
                Mekanism.packetHandler.sendToServer(new PacketGuiInteract(GuiInteraction.SET_TIME, tile, Integer.parseInt(timerField.getText())));
            } catch (NumberFormatException ignored) {
            }
            timerField.setText("");
        }
    }
}