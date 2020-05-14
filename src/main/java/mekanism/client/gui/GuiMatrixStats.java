package mekanism.client.gui;

import java.util.Arrays;
import mekanism.api.math.FloatingLong;
import mekanism.client.gui.element.bar.GuiBar.IBarInfoHandler;
import mekanism.client.gui.element.bar.GuiVerticalRateBar;
import mekanism.client.gui.element.gauge.GaugeType;
import mekanism.client.gui.element.gauge.GuiEnergyGauge;
import mekanism.client.gui.element.gauge.GuiEnergyGauge.IEnergyInfoHandler;
import mekanism.client.gui.element.tab.GuiEnergyTab;
import mekanism.client.gui.element.tab.GuiMatrixTab;
import mekanism.client.gui.element.tab.GuiMatrixTab.MatrixTab;
import mekanism.common.MekanismLang;
import mekanism.common.inventory.container.tile.EmptyTileContainer;
import mekanism.common.tile.TileEntityInductionCasing;
import mekanism.common.util.text.EnergyDisplay;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.text.ITextComponent;

public class GuiMatrixStats extends GuiMekanismTile<TileEntityInductionCasing, EmptyTileContainer<TileEntityInductionCasing>> {

    public GuiMatrixStats(EmptyTileContainer<TileEntityInductionCasing> container, PlayerInventory inv, ITextComponent title) {
        super(container, inv, title);
    }

    @Override
    public void init() {
        super.init();
        addButton(new GuiMatrixTab(this, tile, MatrixTab.MAIN));
        addButton(new GuiEnergyGauge(new IEnergyInfoHandler() {
            @Override
            public FloatingLong getEnergy() {
                return tile.getMultiblock().getEnergy();
            }

            @Override
            public FloatingLong getMaxEnergy() {
                return tile.getMultiblock().getStorageCap();
            }
        }, GaugeType.STANDARD, this, 6, 13));
        addButton(new GuiVerticalRateBar(this, new IBarInfoHandler() {
            @Override
            public ITextComponent getTooltip() {
                return MekanismLang.MATRIX_RECEIVING_RATE.translate(EnergyDisplay.of(tile.getMultiblock().getLastInput()));
            }

            @Override
            public double getLevel() {
                return !tile.getMultiblock().isFormed() ? 0 : tile.getMultiblock().getLastInput().divideToLevel(tile.getMultiblock().getTransferCap());
            }
        }, 30, 13));
        addButton(new GuiVerticalRateBar(this, new IBarInfoHandler() {
            @Override
            public ITextComponent getTooltip() {
                return MekanismLang.MATRIX_OUTPUTTING_RATE.translate(EnergyDisplay.of(tile.getMultiblock().getLastOutput()));
            }

            @Override
            public double getLevel() {
                if (!tile.getMultiblock().isFormed()) {
                    return 0;
                }
                return tile.getMultiblock().getLastOutput().divideToLevel(tile.getMultiblock().getTransferCap());
            }
        }, 38, 13));
        addButton(new GuiEnergyTab(() -> Arrays.asList(MekanismLang.STORING.translate(EnergyDisplay.of(tile.getMultiblock().getEnergy(), tile.getMultiblock().getStorageCap())),
              MekanismLang.MATRIX_INPUT_RATE.translate(EnergyDisplay.of(tile.getMultiblock().getLastInput())),
              MekanismLang.MATRIX_OUTPUT_RATE.translate(EnergyDisplay.of(tile.getMultiblock().getLastOutput()))),
              this));
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        drawTitleText(MekanismLang.MATRIX_STATS.translate(), 6);
        drawString(MekanismLang.MATRIX_INPUT_AMOUNT.translate(), 53, 26, 0x797979);
        drawString(EnergyDisplay.of(tile.getMultiblock().getLastInput(), tile.getMultiblock().getTransferCap()).getTextComponent(), 59, 35, titleTextColor());
        drawString(MekanismLang.MATRIX_OUTPUT_AMOUNT.translate(), 53, 46, 0x797979);
        drawString(EnergyDisplay.of(tile.getMultiblock().getLastOutput(), tile.getMultiblock().getTransferCap()).getTextComponent(), 59, 55, titleTextColor());
        drawString(MekanismLang.MATRIX_DIMENSIONS.translate(), 8, 82, 0x797979);
        if (tile.getMultiblock().isFormed()) {
            drawString(MekanismLang.MATRIX_DIMENSION_REPRESENTATION.translate(tile.getMultiblock().volWidth, tile.getMultiblock().volHeight, tile.getMultiblock().volLength), 14, 91, titleTextColor());
        }
        drawString(MekanismLang.MATRIX_CONSTITUENTS.translate(), 8, 102, 0x797979);
        drawString(MekanismLang.MATRIX_CELLS.translate(tile.getMultiblock().getCellCount()), 14, 111, titleTextColor());
        drawString(MekanismLang.MATRIX_PROVIDERS.translate(tile.getMultiblock().getProviderCount()), 14, 120, titleTextColor());
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
    }
}