package mekanism.client.gui.machine;

import java.util.Arrays;
import java.util.Collections;
import mekanism.api.math.FloatingLong;
import mekanism.client.gui.GuiMekanismTile;
import mekanism.client.gui.element.GuiInnerScreen;
import mekanism.client.gui.element.GuiTextField;
import mekanism.client.gui.element.GuiTextField.InputValidator;
import mekanism.client.gui.element.bar.GuiVerticalPowerBar;
import mekanism.client.gui.element.button.MekanismImageButton;
import mekanism.client.gui.element.tab.GuiEnergyTab;
import mekanism.client.gui.element.tab.GuiHeatTab;
import mekanism.client.gui.element.tab.GuiRedstoneControlTab;
import mekanism.client.gui.element.tab.GuiSecurityTab;
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
        addButton(new GuiInnerScreen(this, 48, 23, 80, 28, () -> Arrays.asList(
            MekanismLang.TEMPERATURE.translate(MekanismUtils.getTemperatureDisplay(tile.getTotalTemperature(), TemperatureUnit.KELVIN, true)),
            MekanismLang.RESISTIVE_HEATER_USAGE.translate(EnergyDisplay.of(tile.getEnergyContainer().getEnergyPerTick()))
        )));
        addButton(new GuiInnerScreen(this, 48, 50, 68, 13));
        addButton(new GuiInnerScreen(this, 115, 50, 13, 13));
        addButton(new GuiVerticalPowerBar(this, tile.getEnergyContainer(), 164, 15));
        addButton(new GuiSecurityTab<>(this, tile));
        addButton(new GuiRedstoneControlTab(this, tile));
        addButton(new GuiEnergyTab(tile.getEnergyContainer(), this));
        addButton(new GuiHeatTab(() -> {
            ITextComponent environment = MekanismUtils.getTemperatureDisplay(tile.lastEnvironmentLoss, TemperatureUnit.KELVIN, false);
            return Collections.singletonList(MekanismLang.DISSIPATED_RATE.translate(environment));
        }, this));

        addButton(energyUsageField = new GuiTextField(this, 49, 51, 78, 11));
        energyUsageField.setText(energyUsageField != null ? energyUsageField.getText() : "");
        energyUsageField.setMaxStringLength(7);
        energyUsageField.setInputValidator(InputValidator.DIGIT);
        energyUsageField.setEnterHandler(this::setEnergyUsage);
        addButton(new MekanismImageButton(this, getGuiLeft() + 116, getGuiTop() + 51, 11, 12, getButtonLocation("checkmark"), this::setEnergyUsage));
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        renderTitleText();
        drawString(MekanismLang.INVENTORY.translate(), 8, (getYSize() - 94) + 2, titleTextColor());
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
    }

    private void setEnergyUsage() {
        if (!energyUsageField.getText().isEmpty()) {
            try {
                Mekanism.packetHandler.sendToServer(new PacketGuiSetEnergy(GuiEnergyValue.ENERGY_USAGE, tile.getPos(),
                      MekanismUtils.convertToJoules(FloatingLong.parseFloatingLong(energyUsageField.getText()))));
            } catch (NumberFormatException ignored) {}
            energyUsageField.setText("");
        }
    }
}