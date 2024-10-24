package mekanism.client.gui;

import java.util.List;
import mekanism.api.math.MathUtils;
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
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.NotNull;

public class GuiMatrixStats extends GuiMekanismTile<TileEntityInductionCasing, EmptyTileContainer<TileEntityInductionCasing>> {

    public GuiMatrixStats(EmptyTileContainer<TileEntityInductionCasing> container, Inventory inv, Component title) {
        super(container, inv, title);
        titleLabelY = 5;
    }

    @Override
    protected void addGuiElements() {
        super.addGuiElements();
        addRenderableWidget(new GuiMatrixTab(this, tile, MatrixTab.MAIN));
        addRenderableWidget(new GuiEnergyGauge(new IEnergyInfoHandler() {
            @Override
            public long getEnergy() {
                return tile.getMultiblock().getEnergy();
            }

            @Override
            public long getMaxEnergy() {
                return tile.getMultiblock().getStorageCap();
            }
        }, GaugeType.STANDARD, this, 6, 13));
        addRenderableWidget(new GuiVerticalRateBar(this, new IBarInfoHandler() {
            @Override
            public Component getTooltip() {
                return MekanismLang.MATRIX_RECEIVING_RATE.translate(EnergyDisplay.of(tile.getMultiblock().getLastInput()));
            }

            @Override
            public double getLevel() {
                MatrixMultiblockData multiblock = tile.getMultiblock();
                return multiblock.isFormed() ? MathUtils.divideToLevel(multiblock.getLastInput(), multiblock.getTransferCap()) : 0;
            }
        }, 30, 13));
        addRenderableWidget(new GuiVerticalRateBar(this, new IBarInfoHandler() {
            @Override
            public Component getTooltip() {
                return MekanismLang.MATRIX_OUTPUTTING_RATE.translate(EnergyDisplay.of(tile.getMultiblock().getLastOutput()));
            }

            @Override
            public double getLevel() {
                MatrixMultiblockData multiblock = tile.getMultiblock();
                if (!multiblock.isFormed()) {
                    return 0;
                }
                return MathUtils.divideToLevel(multiblock.getLastOutput(), multiblock.getTransferCap());
            }
        }, 38, 13));
        addRenderableWidget(new GuiEnergyTab(this, () -> {
            MatrixMultiblockData multiblock = tile.getMultiblock();
            return List.of(MekanismLang.STORING.translate(EnergyDisplay.of(multiblock.getEnergy(), multiblock.getStorageCap())),
                  MekanismLang.MATRIX_INPUT_RATE.translate(EnergyDisplay.of(multiblock.getLastInput())),
                  MekanismLang.MATRIX_OUTPUT_RATE.translate(EnergyDisplay.of(multiblock.getLastOutput())));
        }));
    }

    @Override
    protected void drawForegroundText(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY) {
        renderTitleText(guiGraphics);
        MatrixMultiblockData multiblock = tile.getMultiblock();
        drawScrollingString(guiGraphics, MekanismLang.INPUT.translate(), 45, 26, TextAlignment.LEFT, subheadingTextColor(), getXSize() - 54, 8, false);
        drawScrollingString(guiGraphics, EnergyDisplay.of(multiblock.getLastInput(), multiblock.getTransferCap()).getTextComponent(), 51, 35, TextAlignment.LEFT, titleTextColor(), getXSize() - 60, 8, false);
        drawScrollingString(guiGraphics, MekanismLang.OUTPUT.translate(), 45, 46, TextAlignment.LEFT, subheadingTextColor(), getXSize() - 54, 8, false);
        drawScrollingString(guiGraphics, EnergyDisplay.of(multiblock.getLastOutput(), multiblock.getTransferCap()).getTextComponent(), 51, 55, TextAlignment.LEFT, titleTextColor(), getXSize() - 60, 8, false);
        drawScrollingString(guiGraphics, MekanismLang.MATRIX_DIMENSIONS.translate(), 0, 82, TextAlignment.LEFT, subheadingTextColor(), 8, false);
        if (multiblock.isFormed()) {
            drawScrollingString(guiGraphics, MekanismLang.MATRIX_DIMENSION_REPRESENTATION.translate(multiblock.width(), multiblock.height(), multiblock.length()), 6, 91, TextAlignment.LEFT, titleTextColor(), getXSize() - 6, 8, false);
        }
        drawScrollingString(guiGraphics, MekanismLang.MATRIX_CONSTITUENTS.translate(), 0, 102, TextAlignment.LEFT, subheadingTextColor(), 8, false);
        drawScrollingString(guiGraphics, MekanismLang.MATRIX_CELLS.translate(multiblock.getCellCount()), 6, 111, TextAlignment.LEFT, titleTextColor(), getXSize() - 6, 8, false);
        drawScrollingString(guiGraphics, MekanismLang.MATRIX_PROVIDERS.translate(multiblock.getProviderCount()), 6, 120, TextAlignment.LEFT, titleTextColor(), getXSize() - 6, 8, false);
        super.drawForegroundText(guiGraphics, mouseX, mouseY);
    }
}