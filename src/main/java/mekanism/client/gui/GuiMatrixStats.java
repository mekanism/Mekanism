package mekanism.client.gui;

import java.util.Arrays;
import mekanism.api.math.FloatingLong;
import mekanism.client.gui.element.GuiEnergyInfo;
import mekanism.client.gui.element.bar.GuiBar.IBarInfoHandler;
import mekanism.client.gui.element.bar.GuiVerticalRateBar;
import mekanism.client.gui.element.gauge.GaugeType;
import mekanism.client.gui.element.gauge.GuiEnergyGauge;
import mekanism.client.gui.element.gauge.GuiEnergyGauge.IEnergyInfoHandler;
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
                return tile.getEnergy();
            }

            @Override
            public FloatingLong getMaxEnergy() {
                return tile.getMaxEnergy();
            }
        }, GaugeType.STANDARD, this, 6, 13));
        addButton(new GuiVerticalRateBar(this, new IBarInfoHandler() {
            @Override
            public ITextComponent getTooltip() {
                return MekanismLang.MATRIX_RECEIVING_RATE.translate(EnergyDisplay.of(tile.getLastInput()));
            }

            @Override
            public double getLevel() {
                return tile.structure == null ? 0 : tile.getLastInput().divideToLevel(tile.structure.getTransferCap());
            }
        }, 30, 13));
        addButton(new GuiVerticalRateBar(this, new IBarInfoHandler() {
            @Override
            public ITextComponent getTooltip() {
                return MekanismLang.MATRIX_OUTPUTTING_RATE.translate(EnergyDisplay.of(tile.getLastOutput()));
            }

            @Override
            public double getLevel() {
                if (tile.structure == null) {
                    return 0;
                }
                return tile.getLastOutput().divideToLevel(tile.structure.getTransferCap());
            }
        }, 38, 13));
        addButton(new GuiEnergyInfo(() -> Arrays.asList(MekanismLang.STORING.translate(EnergyDisplay.of(tile.getEnergy(), tile.getMaxEnergy())),
              MekanismLang.MATRIX_INPUT_RATE.translate(EnergyDisplay.of(tile.getLastInput())),
              MekanismLang.MATRIX_OUTPUT_RATE.translate(EnergyDisplay.of(tile.getLastOutput()))),
              this));
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        renderTitleText(MekanismLang.MATRIX_STATS.translate(), 6);
        drawString(MekanismLang.MATRIX_INPUT_AMOUNT.translate(), 53, 26, 0x797979);
        drawString(EnergyDisplay.of(tile.getLastInput(), tile.getTransferCap()).getTextComponent(), 59, 35, titleTextColor());
        drawString(MekanismLang.MATRIX_OUTPUT_AMOUNT.translate(), 53, 46, 0x797979);
        drawString(EnergyDisplay.of(tile.getLastOutput(), tile.getTransferCap()).getTextComponent(), 59, 55, titleTextColor());
        drawString(MekanismLang.MATRIX_DIMENSIONS.translate(), 8, 82, 0x797979);
        if (tile.structure != null) {
            drawString(MekanismLang.MATRIX_DIMENSION_REPRESENTATION.translate(tile.structure.volWidth, tile.structure.volHeight, tile.structure.volLength), 14, 91, titleTextColor());
        }
        drawString(MekanismLang.MATRIX_CONSTITUENTS.translate(), 8, 102, 0x797979);
        drawString(MekanismLang.MATRIX_CELLS.translate(tile.getCellCount()), 14, 111, titleTextColor());
        drawString(MekanismLang.MATRIX_PROVIDERS.translate(tile.getProviderCount()), 14, 120, titleTextColor());
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
    }
}