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
import mekanism.common.content.matrix.MatrixMultiblockData;
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
                MatrixMultiblockData multiblock = tile.getMultiblock();
                return !multiblock.isFormed() ? 0 : multiblock.getLastInput().divideToLevel(multiblock.getTransferCap());
            }
        }, 30, 13));
        addButton(new GuiVerticalRateBar(this, new IBarInfoHandler() {
            @Override
            public ITextComponent getTooltip() {
                return MekanismLang.MATRIX_OUTPUTTING_RATE.translate(EnergyDisplay.of(tile.getMultiblock().getLastOutput()));
            }

            @Override
            public double getLevel() {
                MatrixMultiblockData multiblock = tile.getMultiblock();
                if (!multiblock.isFormed()) {
                    return 0;
                }
                return multiblock.getLastOutput().divideToLevel(multiblock.getTransferCap());
            }
        }, 38, 13));
        addButton(new GuiEnergyTab(() -> {
            MatrixMultiblockData multiblock = tile.getMultiblock();
            return Arrays.asList(MekanismLang.STORING.translate(EnergyDisplay.of(multiblock.getEnergy(), multiblock.getStorageCap())),
                  MekanismLang.MATRIX_INPUT_RATE.translate(EnergyDisplay.of(multiblock.getLastInput())),
                  MekanismLang.MATRIX_OUTPUT_RATE.translate(EnergyDisplay.of(multiblock.getLastOutput())));
        }, this));
    }

    @Override
    protected void drawForegroundText(@Nonnull MatrixStack matrix, int mouseX, int mouseY) {
        drawTitleText(matrix, MekanismLang.MATRIX_STATS.translate(), 6);
        MatrixMultiblockData multiblock = tile.getMultiblock();
        drawString(matrix, MekanismLang.MATRIX_INPUT_AMOUNT.translate(), 53, 26, subheadingTextColor());
        drawString(matrix, EnergyDisplay.of(multiblock.getLastInput(), multiblock.getTransferCap()).getTextComponent(), 59, 35, titleTextColor());
        drawString(matrix, MekanismLang.MATRIX_OUTPUT_AMOUNT.translate(), 53, 46, subheadingTextColor());
        drawString(matrix, EnergyDisplay.of(multiblock.getLastOutput(), multiblock.getTransferCap()).getTextComponent(), 59, 55, titleTextColor());
        drawString(matrix, MekanismLang.MATRIX_DIMENSIONS.translate(), 8, 82, subheadingTextColor());
        if (multiblock.isFormed()) {
            drawString(matrix, MekanismLang.MATRIX_DIMENSION_REPRESENTATION.translate(multiblock.width(), multiblock.height(), multiblock.length()), 14, 91, titleTextColor());
        }
        drawString(matrix, MekanismLang.MATRIX_CONSTITUENTS.translate(), 8, 102, subheadingTextColor());
        drawString(matrix, MekanismLang.MATRIX_CELLS.translate(multiblock.getCellCount()), 14, 111, titleTextColor());
        drawString(matrix, MekanismLang.MATRIX_PROVIDERS.translate(multiblock.getProviderCount()), 14, 120, titleTextColor());
        super.drawForegroundText(matrix, mouseX, mouseY);
    }
}