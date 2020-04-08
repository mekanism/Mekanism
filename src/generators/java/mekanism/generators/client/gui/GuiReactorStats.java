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
import mekanism.generators.client.gui.element.GuiReactorTab;
import mekanism.generators.client.gui.element.GuiReactorTab.ReactorTab;
import mekanism.generators.common.GeneratorsLang;
import mekanism.generators.common.tile.reactor.TileEntityReactorController;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.text.ITextComponent;

public class GuiReactorStats extends GuiReactorInfo {

    private static final NumberFormat nf = NumberFormat.getIntegerInstance();

    public GuiReactorStats(EmptyTileContainer<TileEntityReactorController> container, PlayerInventory inv, ITextComponent title) {
        super(container, inv, title);
    }

    @Override
    public void init() {
        super.init();
        addButton(new GuiEnergyInfo(() -> tile.isFormed() ? Arrays.asList(MekanismLang.STORING.translate(EnergyDisplay.of(tile.energyContainer.getEnergy(), tile.energyContainer.getMaxEnergy())),
              GeneratorsLang.PRODUCING_AMOUNT.translate(EnergyDisplay.of(tile.getReactor().getPassiveGeneration(false, true)))) : Collections.emptyList(),
              this));
        addButton(new GuiReactorTab(this, tile, ReactorTab.HEAT));
        addButton(new GuiReactorTab(this, tile, ReactorTab.FUEL));
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        drawString(tile.getName(), 46, 6, 0x404040);
        if (tile.isFormed()) {
            drawString(GeneratorsLang.REACTOR_PASSIVE.translateColored(EnumColor.DARK_GREEN), 6, 26, 0x404040);
            renderScaledText(GeneratorsLang.REACTOR_MIN_INJECTION.translate(tile.getReactor().getMinInjectionRate(false)), 16, 36, 0x404040, 156);
            renderScaledText(GeneratorsLang.REACTOR_IGNITION.translate(MekanismUtils.getTemperatureDisplay(tile.getReactor().getIgnitionTemperature(false),
                  TemperatureUnit.AMBIENT)), 16, 46, 0x404040, 156);
            renderScaledText(GeneratorsLang.REACTOR_MAX_PLASMA.translate(MekanismUtils.getTemperatureDisplay(tile.getReactor().getMaxPlasmaTemperature(false),
                  TemperatureUnit.AMBIENT)), 16, 56, 0x404040, 156);
            renderScaledText(GeneratorsLang.REACTOR_MAX_CASING.translate(MekanismUtils.getTemperatureDisplay(tile.getReactor().getMaxCasingTemperature(false),
                  TemperatureUnit.AMBIENT)), 16, 66, 0x404040, 156);
            renderScaledText(GeneratorsLang.REACTOR_PASSIVE_RATE.translate(EnergyDisplay.of(tile.getReactor().getPassiveGeneration(false, false))),
                  16, 76, 0x404040, 156);
            drawString(GeneratorsLang.REACTOR_ACTIVE.translateColored(EnumColor.DARK_BLUE), 6, 92, 0x404040);
            renderScaledText(GeneratorsLang.REACTOR_MIN_INJECTION.translate(tile.getReactor().getMinInjectionRate(true)), 16, 102, 0x404040, 156);
            renderScaledText(GeneratorsLang.REACTOR_IGNITION.translate(MekanismUtils.getTemperatureDisplay(tile.getReactor().getIgnitionTemperature(true),
                  TemperatureUnit.AMBIENT)), 16, 112, 0x404040, 156);
            renderScaledText(GeneratorsLang.REACTOR_MAX_PLASMA.translate(MekanismUtils.getTemperatureDisplay(tile.getReactor().getMaxPlasmaTemperature(true),
                  TemperatureUnit.AMBIENT)), 16, 122, 0x404040, 156);
            renderScaledText(GeneratorsLang.REACTOR_MAX_CASING.translate(MekanismUtils.getTemperatureDisplay(tile.getReactor().getMaxCasingTemperature(true),
                  TemperatureUnit.AMBIENT)), 16, 132, 0x404040, 156);
            renderScaledText(GeneratorsLang.REACTOR_PASSIVE_RATE.translate(EnergyDisplay.of(tile.getReactor().getPassiveGeneration(true, false))),
                  16, 142, 0x404040, 156);
            renderScaledText(GeneratorsLang.REACTOR_STEAM_PRODUCTION.translate(nf.format(tile.getReactor().getSteamPerTick(false))), 16, 152, 0x404040, 156);
        }
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
    }
}