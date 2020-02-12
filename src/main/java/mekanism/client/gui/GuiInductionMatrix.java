package mekanism.client.gui;

import java.util.Arrays;
import mekanism.client.gui.element.GuiEnergyInfo;
import mekanism.client.gui.element.tab.GuiMatrixTab;
import mekanism.client.gui.element.tab.GuiMatrixTab.MatrixTab;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.MekanismLang;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import mekanism.common.tile.TileEntityInductionCasing;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import mekanism.common.util.text.EnergyDisplay;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

public class GuiInductionMatrix extends GuiMekanismTile<TileEntityInductionCasing, MekanismTileContainer<TileEntityInductionCasing>> {

    public GuiInductionMatrix(MekanismTileContainer<TileEntityInductionCasing> container, PlayerInventory inv, ITextComponent title) {
        super(container, inv, title);
    }

    @Override
    public void init() {
        super.init();
        ResourceLocation resource = getGuiLocation();
        addButton(new GuiMatrixTab(this, tile, MatrixTab.STAT, resource));
        addButton(new GuiEnergyInfo(() -> Arrays.asList(MekanismLang.STORING.translate(EnergyDisplay.of(tile.getEnergy(), tile.getMaxEnergy())),
              MekanismLang.MATRIX_INPUT_RATE.translate(EnergyDisplay.of(tile.getLastInput())),
              MekanismLang.MATRIX_OUTPUT_RATE.translate(EnergyDisplay.of(tile.getLastOutput()))
        ), this, resource));
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        drawString(tile.getName(), (getXSize() / 2) - (getStringWidth(tile.getName()) / 2), 6, 0x404040);
        drawString(MekanismLang.INVENTORY.translate(), 8, (getYSize() - 94) + 2, 0x404040);
        drawString(MekanismLang.MATRIX_INPUT_AMOUNT.translate(), 53, 26, 0x00CD00);
        drawString(MekanismLang.GENERIC_PER_TICK.translate(EnergyDisplay.of(tile.getLastInput())), 53, 35, 0x00CD00);
        drawString(MekanismLang.MATRIX_OUTPUT_AMOUNT.translate(), 53, 44, 0x00CD00);
        drawString(MekanismLang.GENERIC_PER_TICK.translate(EnergyDisplay.of(tile.getLastOutput())), 53, 53, 0x00CD00);
        //TODO: 1.14 Convert to GuiElement
        int xAxis = mouseX - getGuiLeft();
        int yAxis = mouseY - getGuiTop();
        if (xAxis >= 7 && xAxis <= 39 && yAxis >= 14 && yAxis <= 72) {
            displayTooltip(EnergyDisplay.of(tile.getEnergy(), tile.getMaxEnergy()).getTextComponent(), xAxis, yAxis);
        }
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(int xAxis, int yAxis) {
        super.drawGuiContainerBackgroundLayer(xAxis, yAxis);
        if (tile.getScaledEnergyLevel(58) > 0) {
            displayGauge(7, 14, tile.getScaledEnergyLevel(58), 0);
            displayGauge(23, 14, tile.getScaledEnergyLevel(58), 1);
        }
    }

    @Override
    protected ResourceLocation getGuiLocation() {
        return MekanismUtils.getResource(ResourceType.GUI, "induction_matrix.png");
    }

    public void displayGauge(int xPos, int yPos, int scale, int side /*0-left, 1-right*/) {
        minecraft.textureManager.bindTexture(AtlasTexture.LOCATION_BLOCKS_TEXTURE);
        int start = 0;
        int x = getGuiLeft() + xPos;
        int y = getGuiTop() + yPos;
        while (scale > 0) {
            int renderRemaining;
            if (scale > 16) {
                renderRemaining = 16;
                scale -= 16;
            } else {
                renderRemaining = scale;
                scale = 0;
            }
            drawTexturedRectFromIcon(x, y + 58 - renderRemaining - start, MekanismRenderer.energyIcon, 16, renderRemaining);
            start += 16;
        }
        minecraft.textureManager.bindTexture(getGuiLocation());
        drawTexturedRect(x, y, 176, side == 0 ? 0 : 54, 16, 54);
    }
}