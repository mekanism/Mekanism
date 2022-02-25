package mekanism.client.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.Collections;
import javax.annotation.Nonnull;
import mekanism.api.math.MathUtils;
import mekanism.client.gui.element.graph.GuiLongGraph;
import mekanism.client.gui.element.tab.GuiBoilerTab;
import mekanism.client.gui.element.tab.GuiBoilerTab.BoilerTab;
import mekanism.client.gui.element.tab.GuiHeatTab;
import mekanism.common.MekanismLang;
import mekanism.common.config.MekanismConfig;
import mekanism.common.content.boiler.BoilerMultiblockData;
import mekanism.common.inventory.container.tile.EmptyTileContainer;
import mekanism.common.tile.multiblock.TileEntityBoilerCasing;
import mekanism.common.util.HeatUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.UnitDisplayUtils.TemperatureUnit;
import mekanism.common.util.text.TextUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

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
    protected void drawForegroundText(@Nonnull PoseStack matrix, int mouseX, int mouseY) {
        drawCenteredText(matrix, title, 0, imageWidth, titleLabelY, titleTextColor());
        BoilerMultiblockData multiblock = tile.getMultiblock();
        drawString(matrix, MekanismLang.BOILER_MAX_WATER.translate(TextUtils.format(multiblock.waterTank.getCapacity())), 8, 26, titleTextColor());
        drawString(matrix, MekanismLang.BOILER_MAX_STEAM.translate(TextUtils.format(multiblock.steamTank.getCapacity())), 8, 35, titleTextColor());
        drawString(matrix, MekanismLang.BOILER_HEAT_TRANSFER.translate(), 8, 49, subheadingTextColor());
        drawString(matrix, MekanismLang.BOILER_HEATERS.translate(multiblock.superheatingElements), 14, 58, titleTextColor());
        drawString(matrix, MekanismLang.BOILER_CAPACITY.translate(TextUtils.format(multiblock.getBoilCapacity())), 8, 72, titleTextColor());
        super.drawForegroundText(matrix, mouseX, mouseY);
    }

    @Override
    public void containerTick() {
        super.containerTick();
        BoilerMultiblockData multiblock = tile.getMultiblock();
        boilGraph.addData(multiblock.lastBoilRate);
        maxGraph.addData(multiblock.lastMaxBoil);
    }
}