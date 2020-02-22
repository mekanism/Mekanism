package mekanism.client.gui;

import java.util.Collections;
import mekanism.client.gui.element.GuiGraph;
import mekanism.client.gui.element.GuiHeatInfo;
import mekanism.client.gui.element.tab.GuiBoilerTab;
import mekanism.client.gui.element.tab.GuiBoilerTab.BoilerTab;
import mekanism.common.MekanismLang;
import mekanism.common.config.MekanismConfig;
import mekanism.common.content.boiler.BoilerUpdateProtocol;
import mekanism.common.content.boiler.SynchronizedBoilerData;
import mekanism.common.inventory.container.tile.EmptyTileContainer;
import mekanism.common.tile.TileEntityBoilerCasing;
import mekanism.common.util.EnumUtils;
import mekanism.common.util.UnitDisplayUtils;
import mekanism.common.util.UnitDisplayUtils.TemperatureUnit;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.text.ITextComponent;

public class GuiBoilerStats extends GuiMekanismTile<TileEntityBoilerCasing, EmptyTileContainer<TileEntityBoilerCasing>> {

    private GuiGraph boilGraph;
    private GuiGraph maxGraph;

    public GuiBoilerStats(EmptyTileContainer<TileEntityBoilerCasing> container, PlayerInventory inv, ITextComponent title) {
        super(container, inv, title);
    }

    @Override
    public void init() {
        super.init();
        addButton(new GuiBoilerTab(this, tile, BoilerTab.MAIN));
        addButton(new GuiHeatInfo(() -> {
            TemperatureUnit unit = EnumUtils.TEMPERATURE_UNITS[MekanismConfig.general.tempUnit.get().ordinal()];
            ITextComponent environment = UnitDisplayUtils.getDisplayShort(tile.getLastEnvironmentLoss() * unit.intervalSize, unit, false);
            return Collections.singletonList(MekanismLang.DISSIPATED_RATE.translate(environment));
        }, this));
        addButton(boilGraph = new GuiGraph(this, 8, 83, 160, 36, MekanismLang.BOIL_RATE::translate));
        addButton(maxGraph = new GuiGraph(this, 8, 122, 160, 36, MekanismLang.MAX_BOIL_RATE::translate));
        maxGraph.enableFixedScale((int) ((tile.getSuperheatingElements() * MekanismConfig.general.superheatingHeatTransfer.get()) / SynchronizedBoilerData.getHeatEnthalpy()));
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        drawCenteredText(MekanismLang.BOILER_STATS.translate(), 0, getXSize(), 6, 0x404040);
        drawString(MekanismLang.BOILER_MAX_WATER.translate(tile.structure == null ? 0 : tile.structure.waterVolume * BoilerUpdateProtocol.WATER_PER_TANK), 8, 26, 0x404040);
        drawString(MekanismLang.BOILER_MAX_STEAM.translate(tile.structure == null ? 0 : tile.structure.steamVolume * BoilerUpdateProtocol.STEAM_PER_TANK), 8, 35, 0x404040);
        drawString(MekanismLang.BOILER_HEAT_TRANSFER.translate(), 8, 49, 0x797979);
        drawString(MekanismLang.BOILER_HEATERS.translate(tile.getSuperheatingElements()), 14, 58, 0x404040);
        int boilCapacity = (int) (tile.getSuperheatingElements() * MekanismConfig.general.superheatingHeatTransfer.get() / SynchronizedBoilerData.getHeatEnthalpy());
        drawString(MekanismLang.BOILER_CAPACITY.translate(boilCapacity), 8, 72, 0x404040);
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
    }

    @Override
    public void tick() {
        super.tick();
        boilGraph.addData(tile.getLastBoilRate());
        maxGraph.addData(tile.getLastMaxBoil());
    }
}