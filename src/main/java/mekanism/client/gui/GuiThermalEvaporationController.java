package mekanism.client.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import java.util.Arrays;
import java.util.Collections;
import javax.annotation.Nonnull;
import mekanism.api.text.ILangEntry;
import mekanism.client.gui.element.GuiDownArrow;
import mekanism.client.gui.element.GuiInnerScreen;
import mekanism.client.gui.element.bar.GuiBar.IBarInfoHandler;
import mekanism.client.gui.element.bar.GuiHorizontalRateBar;
import mekanism.client.gui.element.gauge.GaugeType;
import mekanism.client.gui.element.gauge.GuiFluidGauge;
import mekanism.client.gui.element.tab.GuiHeatTab;
import mekanism.common.MekanismLang;
import mekanism.common.content.evaporation.EvaporationMultiblockData;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import mekanism.common.tile.multiblock.TileEntityThermalEvaporationController;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.UnitDisplayUtils.TemperatureUnit;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.text.ITextComponent;

public class GuiThermalEvaporationController extends GuiMekanismTile<TileEntityThermalEvaporationController, MekanismTileContainer<TileEntityThermalEvaporationController>> {

    public GuiThermalEvaporationController(MekanismTileContainer<TileEntityThermalEvaporationController> container, PlayerInventory inv, ITextComponent title) {
        super(container, inv, title);
        playerInventoryTitleY += 2;
        titleY = 4;
        dynamicSlots = true;
    }

    @Override
    public void init() {
        super.init();
        addButton(new GuiInnerScreen(this, 48, 19, 80, 40, () -> {
            EvaporationMultiblockData multiblock = tile.getMultiblock();
            return Arrays.asList(getStruct().translate(), MekanismLang.EVAPORATION_HEIGHT.translate(multiblock.height()),
                  MekanismLang.TEMPERATURE.translate(MekanismUtils.getTemperatureDisplay(multiblock.getTemp(), TemperatureUnit.KELVIN, true)),
                  MekanismLang.FLUID_PRODUCTION.translate(Math.round(multiblock.lastGain * 100D) / 100D));
        }).spacing(1).jeiCategory(tile));
        addButton(new GuiDownArrow(this, 32, 39));
        addButton(new GuiDownArrow(this, 136, 39));
        addButton(new GuiHorizontalRateBar(this, new IBarInfoHandler() {
            @Override
            public ITextComponent getTooltip() {
                return MekanismUtils.getTemperatureDisplay(tile.getMultiblock().getTemp(), TemperatureUnit.KELVIN, true);
            }

            @Override
            public double getLevel() {
                return Math.min(1, tile.getMultiblock().getTemp() / EvaporationMultiblockData.MAX_MULTIPLIER_TEMP);
            }
        }, 48, 63));
        addButton(new GuiFluidGauge(() -> tile.getMultiblock().inputTank, () -> tile.getMultiblock().getFluidTanks(null), GaugeType.STANDARD, this, 6, 13));
        addButton(new GuiFluidGauge(() -> tile.getMultiblock().outputTank, () -> tile.getMultiblock().getFluidTanks(null), GaugeType.STANDARD, this, 152, 13));
        addButton(new GuiHeatTab(() -> {
            ITextComponent environment = MekanismUtils.getTemperatureDisplay(tile.getMultiblock().totalLoss, TemperatureUnit.KELVIN, false);
            return Collections.singletonList(MekanismLang.DISSIPATED_RATE.translate(environment));
        }, this));
    }

    @Override
    protected void drawForegroundText(@Nonnull MatrixStack matrix, int mouseX, int mouseY) {
        drawTitleText(matrix, MekanismLang.EVAPORATION_PLANT.translate(), titleY);
        drawString(matrix, playerInventory.getDisplayName(), playerInventoryTitleX, playerInventoryTitleY, titleTextColor());
        super.drawForegroundText(matrix, mouseX, mouseY);
    }

    private ILangEntry getStruct() {
        return MekanismLang.MULTIBLOCK_FORMED;
    }
}