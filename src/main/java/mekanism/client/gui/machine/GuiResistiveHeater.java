package mekanism.client.gui.machine;

import com.mojang.blaze3d.matrix.MatrixStack;
import java.util.Arrays;
import java.util.Collections;
import javax.annotation.Nonnull;
import mekanism.api.math.FloatingLong;
import mekanism.client.gui.GuiMekanismTile;
import mekanism.client.gui.element.GuiInnerScreen;
import mekanism.client.gui.element.bar.GuiVerticalPowerBar;
import mekanism.client.gui.element.tab.GuiEnergyTab;
import mekanism.client.gui.element.tab.GuiHeatTab;
import mekanism.client.gui.element.tab.GuiRedstoneControlTab;
import mekanism.client.gui.element.tab.GuiSecurityTab;
import mekanism.client.gui.element.text.GuiTextField;
import mekanism.client.gui.element.text.InputValidator;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import mekanism.common.network.PacketGuiSetEnergy;
import mekanism.common.network.PacketGuiSetEnergy.GuiEnergyValue;
import mekanism.common.tile.machine.TileEntityResistiveHeater;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.UnitDisplayUtils.TemperatureUnit;
import mekanism.common.util.text.EnergyDisplay;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.text.ITextComponent;

public class GuiResistiveHeater extends GuiMekanismTile<TileEntityResistiveHeater, MekanismTileContainer<TileEntityResistiveHeater>> {

    private GuiTextField energyUsageField;

    public GuiResistiveHeater(MekanismTileContainer<TileEntityResistiveHeater> container, PlayerInventory inv, ITextComponent title) {
        super(container, inv, title);
        dynamicSlots = true;
    }

    @Override
    public void init() {
        super.init();
        addButton(new GuiInnerScreen(this, 48, 23, 80, 42, () -> Arrays.asList(
              MekanismLang.TEMPERATURE.translate(MekanismUtils.getTemperatureDisplay(tile.getTotalTemperature(), TemperatureUnit.KELVIN, true)),
              MekanismLang.RESISTIVE_HEATER_USAGE.translate(EnergyDisplay.of(tile.getEnergyContainer().getEnergyPerTick()))
        )).clearFormat());
        addButton(new GuiVerticalPowerBar(this, tile.getEnergyContainer(), 164, 15));
        addButton(new GuiSecurityTab<>(this, tile));
        addButton(new GuiRedstoneControlTab(this, tile));
        addButton(new GuiEnergyTab(tile.getEnergyContainer(), this));
        addButton(new GuiHeatTab(() -> {
            ITextComponent environment = MekanismUtils.getTemperatureDisplay(tile.lastEnvironmentLoss, TemperatureUnit.KELVIN, false);
            return Collections.singletonList(MekanismLang.DISSIPATED_RATE.translate(environment));
        }, this));

        addButton(energyUsageField = new GuiTextField(this, 50, 51, 76, 12));
        energyUsageField.setText(energyUsageField.getText());
        energyUsageField.setMaxStringLength(7);
        energyUsageField.setInputValidator(InputValidator.DIGIT);
        energyUsageField.configureDigitalInput(this::setEnergyUsage);
        energyUsageField.setFocused(true);
    }

    @Override
    protected void drawForegroundText(@Nonnull MatrixStack matrix, int mouseX, int mouseY) {
        renderTitleText(matrix);
        drawString(matrix, MekanismLang.INVENTORY.translate(), 8, (getYSize() - 94) + 2, titleTextColor());
        super.drawForegroundText(matrix, mouseX, mouseY);
    }

    private void setEnergyUsage() {
        if (!energyUsageField.getText().isEmpty()) {
            try {
                Mekanism.packetHandler.sendToServer(new PacketGuiSetEnergy(GuiEnergyValue.ENERGY_USAGE, tile.getPos(),
                      MekanismUtils.convertToJoules(FloatingLong.parseFloatingLong(energyUsageField.getText()))));
            } catch (NumberFormatException ignored) {
            }
            energyUsageField.setText("");
        }
    }
}