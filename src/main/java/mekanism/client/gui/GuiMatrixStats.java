package mekanism.client.gui;

import java.util.Arrays;
import mekanism.client.gui.element.GuiEnergyInfo;
import mekanism.client.gui.element.bar.GuiBar.IBarInfoHandler;
import mekanism.client.gui.element.bar.GuiVerticalRateBar;
import mekanism.client.gui.element.gauge.GuiEnergyGauge;
import mekanism.client.gui.element.tab.GuiMatrixTab;
import mekanism.client.gui.element.tab.GuiMatrixTab.MatrixTab;
import mekanism.common.MekanismLang;
import mekanism.common.inventory.container.tile.MatrixStatsContainer;
import mekanism.common.tile.TileEntityInductionCasing;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import mekanism.common.util.text.EnergyDisplay;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

public class GuiMatrixStats extends GuiMekanismTile<TileEntityInductionCasing, MatrixStatsContainer> {

    public GuiMatrixStats(MatrixStatsContainer container, PlayerInventory inv, ITextComponent title) {
        super(container, inv, title);
    }

    @Override
    public void init() {
        super.init();
        ResourceLocation resource = getGuiLocation();
        addButton(new GuiMatrixTab(this, tile, MatrixTab.MAIN, resource));
        addButton(new GuiEnergyGauge(() -> tile, GuiEnergyGauge.Type.STANDARD, this, resource, 6, 13));
        addButton(new GuiVerticalRateBar(this, new IBarInfoHandler() {
            @Override
            public ITextComponent getTooltip() {
                return MekanismLang.MATRIX_RECEIVING_RATE.translate(EnergyDisplay.of(tile.getLastInput()));
            }

            @Override
            public double getLevel() {
                return tile.structure == null ? 0 : tile.getLastInput() / tile.structure.getTransferCap();
            }
        }, resource, 30, 13));
        addButton(new GuiVerticalRateBar(this, new IBarInfoHandler() {
            @Override
            public ITextComponent getTooltip() {
                return MekanismLang.OUTPUTTING_RATE.translate(EnergyDisplay.of(tile.getLastOutput()));
            }

            @Override
            public double getLevel() {
                return tile.structure == null ? 0 : tile.getLastOutput() / tile.structure.getTransferCap();
            }
        }, resource, 38, 13));
        addButton(new GuiEnergyInfo(() -> Arrays.asList(MekanismLang.STORING.translate(EnergyDisplay.of(tile.getEnergy(), tile.getMaxEnergy())),
              MekanismLang.INPUT_RATE.translate(EnergyDisplay.of(tile.getLastInput())),
              MekanismLang.OUTPUT_RATE.translate(EnergyDisplay.of(tile.getLastOutput()))),
              this, resource));
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        drawCenteredText(MekanismLang.MATRIX_STATS.translate(), 0, xSize, 6, 0x404040);
        drawString(MekanismLang.INPUT_AMOUNT.translate(), 53, 26, 0x797979);
        drawString(EnergyDisplay.of(tile.getLastInput(), tile.getTransferCap()).getTextComponent(), 59, 35, 0x404040);
        drawString(MekanismLang.OUTPUT_AMOUNT.translate(), 53, 46, 0x797979);
        drawString(EnergyDisplay.of(tile.getLastOutput(), tile.getTransferCap()).getTextComponent(), 59, 55, 0x404040);
        drawString(MekanismLang.DIMENSIONS.translate(), 8, 82, 0x797979);
        if (tile.structure != null) {
            drawString(MekanismLang.DIMENSION_REPRESENTATION.translate(tile.structure.volWidth, tile.structure.volHeight, tile.structure.volLength), 14, 91, 0x404040);
        }
        drawString(MekanismLang.CONSTITUENTS.translate(), 8, 102, 0x797979);
        drawString(MekanismLang.MATRIX_CELLS.translate(tile.getCellCount()), 14, 111, 0x404040);
        drawString(MekanismLang.MATRIX_PROVIDERS.translate(tile.getProviderCount()), 14, 120, 0x404040);
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
    }

    @Override
    protected ResourceLocation getGuiLocation() {
        return MekanismUtils.getResource(ResourceType.GUI, "null.png");
    }
}