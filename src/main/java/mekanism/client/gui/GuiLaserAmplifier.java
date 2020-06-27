package mekanism.client.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import javax.annotation.Nonnull;
import mekanism.api.math.FloatingLong;
import mekanism.client.gui.element.gauge.GaugeType;
import mekanism.client.gui.element.gauge.GuiEnergyGauge;
import mekanism.client.gui.element.tab.GuiAmplifierTab;
import mekanism.client.gui.element.tab.GuiRedstoneControlTab;
import mekanism.client.gui.element.tab.GuiSecurityTab;
import mekanism.client.gui.element.text.GuiTextField;
import mekanism.client.gui.element.text.InputValidator;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import mekanism.common.network.PacketGuiInteract;
import mekanism.common.network.PacketGuiInteract.GuiInteraction;
import mekanism.common.network.PacketGuiSetEnergy;
import mekanism.common.network.PacketGuiSetEnergy.GuiEnergyValue;
import mekanism.common.tile.laser.TileEntityLaserAmplifier;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.text.EnergyDisplay;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.text.ITextComponent;

public class GuiLaserAmplifier extends GuiMekanismTile<TileEntityLaserAmplifier, MekanismTileContainer<TileEntityLaserAmplifier>> {

    private GuiTextField minField, maxField, timerField;

    public GuiLaserAmplifier(MekanismTileContainer<TileEntityLaserAmplifier> container, PlayerInventory inv, ITextComponent title) {
        super(container, inv, title);
        dynamicSlots = true;
    }

    @Override
    public void func_231160_c_() {
        super.func_231160_c_();
        func_230480_a_(new GuiEnergyGauge(tile.getEnergyContainer(), GaugeType.STANDARD, this, 6, 10));
        func_230480_a_(new GuiSecurityTab<>(this, tile));
        func_230480_a_(new GuiRedstoneControlTab(this, tile));
        func_230480_a_(new GuiAmplifierTab(this, tile));
        func_230480_a_(timerField = new GuiTextField(this, 96, 28, 36, 11));
        timerField.setMaxStringLength(4);
        timerField.setEnterHandler(this::setTime);
        timerField.setInputValidator(InputValidator.DIGIT);
        func_230480_a_(minField = new GuiTextField(this, 96, 43, 72, 11));
        minField.setMaxStringLength(10);
        minField.setEnterHandler(this::setMinThreshold);
        minField.setInputValidator(InputValidator.SCI_NOTATION);
        func_230480_a_(maxField = new GuiTextField(this, 96, 58, 72, 11));
        maxField.setMaxStringLength(10);
        maxField.setEnterHandler(this::setMaxThreshold);
        maxField.setInputValidator(InputValidator.SCI_NOTATION);
    }

    @Override
    protected void func_230451_b_(@Nonnull MatrixStack matrix, int mouseX, int mouseY) {
        renderTitleText(matrix);
        drawString(matrix, MekanismLang.INVENTORY.translate(), 8, (getYSize() - 96) + 2, titleTextColor());
        if (tile.time > 0) {
            drawString(matrix, MekanismLang.DELAY.translate(tile.time), 26, 30, titleTextColor());
        } else {
            drawString(matrix, MekanismLang.NO_DELAY.translate(), 26, 30, titleTextColor());
        }
        drawString(matrix, MekanismLang.MIN.translate(EnergyDisplay.of(tile.minThreshold)), 26, 45, titleTextColor());
        drawString(matrix, MekanismLang.MAX.translate(EnergyDisplay.of(tile.maxThreshold)), 26, 60, titleTextColor());
        super.func_230451_b_(matrix, mouseX, mouseY);
    }

    private void setMinThreshold() {
        if (!minField.getText().isEmpty()) {
            try {
                Mekanism.packetHandler.sendToServer(new PacketGuiSetEnergy(GuiEnergyValue.MIN_THRESHOLD, tile.getPos(),
                      MekanismUtils.convertToJoules(FloatingLong.parseFloatingLong(minField.getText()))));
            } catch (Exception ignored) {
            }
            minField.setText("");
        }
    }

    private void setMaxThreshold() {
        if (!maxField.getText().isEmpty()) {
            try {
                Mekanism.packetHandler.sendToServer(new PacketGuiSetEnergy(GuiEnergyValue.MAX_THRESHOLD, tile.getPos(),
                      MekanismUtils.convertToJoules(FloatingLong.parseFloatingLong(maxField.getText()))));
            } catch (NumberFormatException ignored) {
            }
            maxField.setText("");
        }
    }

    private void setTime() {
        if (!timerField.getText().isEmpty()) {
            Mekanism.packetHandler.sendToServer(new PacketGuiInteract(GuiInteraction.SET_TIME, tile, Integer.parseInt(timerField.getText())));
            timerField.setText("");
        }
    }
}