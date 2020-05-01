package mekanism.generators.client.gui;

import java.text.NumberFormat;
import java.util.Arrays;
import java.util.Collections;
import mekanism.api.text.EnumColor;
import mekanism.client.gui.element.GuiEnergyInfo;
import mekanism.common.MekanismLang;
import mekanism.common.inventory.container.tile.EmptyTileContainer;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.UnitDisplayUtils.TemperatureUnit;
import mekanism.common.util.text.EnergyDisplay;
import mekanism.generators.client.gui.element.GuiFusionReactorTab;
import mekanism.generators.client.gui.element.GuiFusionReactorTab.FusionReactorTab;
import mekanism.generators.common.GeneratorsLang;
import mekanism.generators.common.tile.fusion.TileEntityFusionReactorController;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.text.ITextComponent;

public class GuiFusionReactorStats extends GuiFusionReactorInfo {

    private static final NumberFormat nf = NumberFormat.getIntegerInstance();

    public GuiFusionReactorStats(EmptyTileContainer<TileEntityFusionReactorController> container, PlayerInventory inv, ITextComponent title) {
        super(container, inv, title);
    }

    @Override
    public void init() {
        super.init();
        addButton(new GuiEnergyInfo(() -> tile.isFormed() ? Arrays.asList(MekanismLang.STORING.translate(EnergyDisplay.of(tile.energyContainer.getEnergy(), tile.energyContainer.getMaxEnergy())),
              GeneratorsLang.PRODUCING_AMOUNT.translate(EnergyDisplay.of(tile.getReactor().getPassiveGeneration(false, true)))) : Collections.emptyList(),
              this));
        addButton(new GuiFusionReactorTab(this, tile, FusionReactorTab.HEAT));
        addButton(new GuiFusionReactorTab(this, tile, FusionReactorTab.FUEL));
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        renderTitleText(5);
        if (tile.isFormed()) {
            drawString(GeneratorsLang.REACTOR_PASSIVE.translateColored(EnumColor.DARK_GREEN), 6, 26, titleTextColor());
            drawScaledText(GeneratorsLang.REACTOR_MIN_INJECTION.translate(tile.getReactor().getMinInjectionRate(false)), 16, 36, titleTextColor(), 156);
            drawScaledText(GeneratorsLang.REACTOR_IGNITION.translate(MekanismUtils.getTemperatureDisplay(tile.getReactor().getIgnitionTemperature(false),
                  TemperatureUnit.KELVIN, true)), 16, 46, titleTextColor(), 156);
            drawScaledText(GeneratorsLang.REACTOR_MAX_PLASMA.translate(MekanismUtils.getTemperatureDisplay(tile.getReactor().getMaxPlasmaTemperature(false),
                  TemperatureUnit.KELVIN, true)), 16, 56, titleTextColor(), 156);
            drawScaledText(GeneratorsLang.REACTOR_MAX_CASING.translate(MekanismUtils.getTemperatureDisplay(tile.getReactor().getMaxCasingTemperature(false),
                  TemperatureUnit.KELVIN, true)), 16, 66, titleTextColor(), 156);
            drawScaledText(GeneratorsLang.REACTOR_PASSIVE_RATE.translate(EnergyDisplay.of(tile.getReactor().getPassiveGeneration(false, false))),
                  16, 76, titleTextColor(), 156);

            drawString(GeneratorsLang.REACTOR_ACTIVE.translateColored(EnumColor.DARK_BLUE), 6, 92, titleTextColor());
            drawScaledText(GeneratorsLang.REACTOR_MIN_INJECTION.translate(tile.getReactor().getMinInjectionRate(true)), 16, 102, titleTextColor(), 156);
            drawScaledText(GeneratorsLang.REACTOR_IGNITION.translate(MekanismUtils.getTemperatureDisplay(tile.getReactor().getIgnitionTemperature(true),
                  TemperatureUnit.KELVIN, true)), 16, 112, titleTextColor(), 156);
            drawScaledText(GeneratorsLang.REACTOR_MAX_PLASMA.translate(MekanismUtils.getTemperatureDisplay(tile.getReactor().getMaxPlasmaTemperature(true),
                  TemperatureUnit.KELVIN, true)), 16, 122, titleTextColor(), 156);
            drawScaledText(GeneratorsLang.REACTOR_MAX_CASING.translate(MekanismUtils.getTemperatureDisplay(tile.getReactor().getMaxCasingTemperature(true),
                  TemperatureUnit.KELVIN, true)), 16, 132, titleTextColor(), 156);
            drawScaledText(GeneratorsLang.REACTOR_PASSIVE_RATE.translate(EnergyDisplay.of(tile.getReactor().getPassiveGeneration(true, false))),
                  16, 142, titleTextColor(), 156);
            drawScaledText(GeneratorsLang.REACTOR_STEAM_PRODUCTION.translate(nf.format(tile.getReactor().getSteamPerTick(false))), 16, 152, titleTextColor(), 156);
        }
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
    }
}