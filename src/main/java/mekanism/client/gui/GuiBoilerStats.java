package mekanism.client.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import java.util.Collections;
import javax.annotation.Nonnull;
import mekanism.client.gui.element.GuiGraph;
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
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.text.ITextComponent;

public class GuiBoilerStats extends GuiMekanismTile<TileEntityBoilerCasing, EmptyTileContainer<TileEntityBoilerCasing>> {

    private GuiGraph boilGraph;
    private GuiGraph maxGraph;

    public GuiBoilerStats(EmptyTileContainer<TileEntityBoilerCasing> container, PlayerInventory inv, ITextComponent title) {
        super(container, inv, title);
    }

    @Override
    protected void addGuiElements() {
        super.addGuiElements();
        addButton(new GuiBoilerTab(this, tile, BoilerTab.MAIN));
        addButton(new GuiHeatTab(this, () -> {
            ITextComponent environment = MekanismUtils.getTemperatureDisplay(tile.getMultiblock().lastEnvironmentLoss, TemperatureUnit.KELVIN, false);
            return Collections.singletonList(MekanismLang.DISSIPATED_RATE.translate(environment));
        }));
        boilGraph = addButton(new GuiGraph(this, 7, 82, 162, 38, MekanismLang.BOIL_RATE::translate));
        maxGraph = addButton(new GuiGraph(this, 7, 121, 162, 38, MekanismLang.MAX_BOIL_RATE::translate));
        maxGraph.enableFixedScale((long) ((MekanismConfig.general.superheatingHeatTransfer.get() * tile.getMultiblock().superheatingElements) / HeatUtils.getWaterThermalEnthalpy()));
    }

    @Override
    protected void drawForegroundText(@Nonnull MatrixStack matrix, int mouseX, int mouseY) {
        drawCenteredText(matrix, MekanismLang.BOILER_STATS.translate(), 0, imageWidth, titleLabelY, titleTextColor());
        BoilerMultiblockData multiblock = tile.getMultiblock();
        drawString(matrix, MekanismLang.BOILER_MAX_WATER.translate(TextUtils.format(multiblock.waterTank.getCapacity())), 8, 26, titleTextColor());
        drawString(matrix, MekanismLang.BOILER_MAX_STEAM.translate(TextUtils.format(multiblock.steamTank.getCapacity())), 8, 35, titleTextColor());
        drawString(matrix, MekanismLang.BOILER_HEAT_TRANSFER.translate(), 8, 49, subheadingTextColor());
        drawString(matrix, MekanismLang.BOILER_HEATERS.translate(multiblock.superheatingElements), 14, 58, titleTextColor());
        drawString(matrix, MekanismLang.BOILER_CAPACITY.translate(TextUtils.format(multiblock.getBoilCapacity())), 8, 72, titleTextColor());
        super.drawForegroundText(matrix, mouseX, mouseY);
    }

    @Override
    public void tick() {
        super.tick();
        BoilerMultiblockData multiblock = tile.getMultiblock();
        boilGraph.addData(multiblock.lastBoilRate);
        maxGraph.addData(multiblock.lastMaxBoil);
    }
}