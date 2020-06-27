package mekanism.client.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import java.util.Arrays;
import javax.annotation.Nonnull;
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
import mekanism.common.tile.multiblock.TileEntityInductionCasing;
import mekanism.common.util.text.EnergyDisplay;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.text.ITextComponent;

public class GuiMatrixStats extends GuiMekanismTile<TileEntityInductionCasing, EmptyTileContainer<TileEntityInductionCasing>> {

    public GuiMatrixStats(EmptyTileContainer<TileEntityInductionCasing> container, PlayerInventory inv, ITextComponent title) {
        super(container, inv, title);
    }

    @Override
    public void func_231160_c_() {
        super.func_231160_c_();
        func_230480_a_(new GuiMatrixTab(this, tile, MatrixTab.MAIN));
        func_230480_a_(new GuiEnergyGauge(new IEnergyInfoHandler() {
            @Override
            public FloatingLong getEnergy() {
                return tile.getMultiblock().getEnergy();
            }

            @Override
            public FloatingLong getMaxEnergy() {
                return tile.getMultiblock().getStorageCap();
            }
        }, GaugeType.STANDARD, this, 6, 13));
        func_230480_a_(new GuiVerticalRateBar(this, new IBarInfoHandler() {
            @Override
            public ITextComponent getTooltip() {
                return MekanismLang.MATRIX_RECEIVING_RATE.translate(EnergyDisplay.of(tile.getMultiblock().getLastInput()));
            }

            @Override
            public double getLevel() {
                return !tile.getMultiblock().isFormed() ? 0 : tile.getMultiblock().getLastInput().divideToLevel(tile.getMultiblock().getTransferCap());
            }
        }, 30, 13));
        func_230480_a_(new GuiVerticalRateBar(this, new IBarInfoHandler() {
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
        func_230480_a_(new GuiEnergyTab(() -> Arrays.asList(MekanismLang.STORING.translate(EnergyDisplay.of(tile.getMultiblock().getEnergy(), tile.getMultiblock().getStorageCap())),
              MekanismLang.MATRIX_INPUT_RATE.translate(EnergyDisplay.of(tile.getMultiblock().getLastInput())),
              MekanismLang.MATRIX_OUTPUT_RATE.translate(EnergyDisplay.of(tile.getMultiblock().getLastOutput()))),
              this));
    }

    @Override
    protected void func_230451_b_(@Nonnull MatrixStack matrix, int mouseX, int mouseY) {
        drawTitleText(matrix, MekanismLang.MATRIX_STATS.translate(), 6);
        drawString(matrix, MekanismLang.MATRIX_INPUT_AMOUNT.translate(), 53, 26, 0x797979);
        drawString(matrix, EnergyDisplay.of(tile.getMultiblock().getLastInput(), tile.getMultiblock().getTransferCap()).getTextComponent(), 59, 35, titleTextColor());
        drawString(matrix, MekanismLang.MATRIX_OUTPUT_AMOUNT.translate(), 53, 46, 0x797979);
        drawString(matrix, EnergyDisplay.of(tile.getMultiblock().getLastOutput(), tile.getMultiblock().getTransferCap()).getTextComponent(), 59, 55, titleTextColor());
        drawString(matrix, MekanismLang.MATRIX_DIMENSIONS.translate(), 8, 82, 0x797979);
        if (tile.getMultiblock().isFormed()) {
            drawString(matrix, MekanismLang.MATRIX_DIMENSION_REPRESENTATION.translate(tile.getMultiblock().width(), tile.getMultiblock().height(), tile.getMultiblock().length()), 14, 91, titleTextColor());
        }
        drawString(matrix, MekanismLang.MATRIX_CONSTITUENTS.translate(), 8, 102, 0x797979);
        drawString(matrix, MekanismLang.MATRIX_CELLS.translate(tile.getMultiblock().getCellCount()), 14, 111, titleTextColor());
        drawString(matrix, MekanismLang.MATRIX_PROVIDERS.translate(tile.getMultiblock().getProviderCount()), 14, 120, titleTextColor());
        super.func_230451_b_(matrix, mouseX, mouseY);
    }
}