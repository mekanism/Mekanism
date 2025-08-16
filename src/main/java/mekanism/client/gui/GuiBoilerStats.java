package mekanism.client.gui;

import java.util.Collections;
import mekanism.api.math.MathUtils;
import mekanism.client.gui.element.graph.GuiLongGraph;
import mekanism.client.gui.element.tab.GuiBoilerTab;
import mekanism.client.gui.element.tab.GuiBoilerTab.BoilerTab;
import mekanism.client.gui.element.tab.GuiHeatTab;
import mekanism.client.gui.element.tab.GuiWarningTab;
import mekanism.common.MekanismLang;
import mekanism.common.config.MekanismConfig;
import mekanism.common.content.boiler.BoilerMultiblockData;
import mekanism.common.inventory.container.tile.EmptyTileContainer;
import mekanism.common.inventory.warning.IWarningTracker;
import mekanism.common.tile.multiblock.TileEntityBoilerCasing;
import mekanism.common.util.HeatUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.UnitDisplayUtils.TemperatureUnit;
import mekanism.common.util.text.TextUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.NotNull;

public class GuiBoilerStats extends GuiMekanismTile<TileEntityBoilerCasing, EmptyTileContainer<TileEntityBoilerCasing>> {

    private GuiLongGraph boilGraph;
    private GuiLongGraph maxGraph;

    public GuiBoilerStats(EmptyTileContainer<TileEntityBoilerCasing> container, Inventory inv, Component title) {
        super(container, inv, title);
    }

    @Override
    protected void addGuiElements() {
        super.addGuiElements();
        addRenderableWidget(new GuiBoilerTab(this, tile, BoilerTab.MAIN));
        addRenderableWidget(new GuiHeatTab(this, () -> {
            Component environment = MekanismUtils.getTemperatureDisplay(tile.getMultiblock().lastEnvironmentLoss, TemperatureUnit.KELVIN, false);
            return Collections.singletonList(MekanismLang.DISSIPATED_RATE.translate(environment));
        }));
        boilGraph = addRenderableWidget(new GuiLongGraph(this, 7, 82, 162, 38, MekanismLang.BOIL_RATE::translate));
        maxGraph = addRenderableWidget(new GuiLongGraph(this, 7, 121, 162, 38, MekanismLang.MAX_BOIL_RATE::translate));
        maxGraph.enableFixedScale(MathUtils.clampToLong((MekanismConfig.general.superheatingHeatTransfer.get() * tile.getMultiblock().superheatingElements) / HeatUtils.getWaterThermalEnthalpy()));
    }

    @Override
    protected void drawForegroundText(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY) {
        renderTitleText(guiGraphics);
        BoilerMultiblockData multiblock = tile.getMultiblock();
        drawScrollingString(guiGraphics, MekanismLang.BOILER_MAX_WATER.translate(TextUtils.format(multiblock.waterTank.getCapacity())), 0, 26, TextAlignment.LEFT, titleTextColor(), 8, false);
        drawScrollingString(guiGraphics, MekanismLang.BOILER_MAX_STEAM.translate(TextUtils.format(multiblock.steamTank.getCapacity())), 0, 35, TextAlignment.LEFT, titleTextColor(), 8, false);
        drawScrollingString(guiGraphics, MekanismLang.BOILER_HEAT_TRANSFER.translate(), 0, 49, TextAlignment.LEFT, subheadingTextColor(), 8, false);
        drawScrollingString(guiGraphics, MekanismLang.BOILER_HEATERS.translate(multiblock.superheatingElements), 6, 58, TextAlignment.LEFT, titleTextColor(), getXSize() - 6, 8, false);
        drawScrollingString(guiGraphics, MekanismLang.BOILER_CAPACITY.translate(TextUtils.format(multiblock.getBoilCapacity())), 0, 72, TextAlignment.LEFT, titleTextColor(), 8, false);
        super.drawForegroundText(guiGraphics, mouseX, mouseY);
    }

    @Override
    public void containerTick() {
        super.containerTick();
        BoilerMultiblockData multiblock = tile.getMultiblock();
        boilGraph.addData(multiblock.lastBoilRate);
        maxGraph.addData(multiblock.lastMaxBoil);
    }

    @Override
    protected void addWarningTab(IWarningTracker warningTracker) {
        //Move the tab to the right side of the gui so it doesn't intersect the heat tab
        addRenderableWidget(new GuiWarningTab(this, warningTracker, false));
    }
}