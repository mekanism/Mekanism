package mekanism.client.gui;

import java.util.Arrays;
import mekanism.client.gui.element.GuiEnergyInfo;
import mekanism.client.gui.element.tab.GuiMatrixTab;
import mekanism.client.gui.element.tab.GuiMatrixTab.MatrixTab;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.inventory.container.tile.InductionMatrixContainer;
import mekanism.common.tile.TileEntityInductionCasing;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import mekanism.common.util.text.EnergyDisplay;
import mekanism.common.util.text.TextComponentUtil;
import mekanism.common.util.text.Translation;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.PlayerContainer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

public class GuiInductionMatrix extends GuiMekanismTile<TileEntityInductionCasing, InductionMatrixContainer> {

    public GuiInductionMatrix(InductionMatrixContainer container, PlayerInventory inv, ITextComponent title) {
        super(container, inv, title);
    }

    @Override
    public void init() {
        super.init();
        ResourceLocation resource = getGuiLocation();
        addButton(new GuiMatrixTab(this, tile, MatrixTab.STAT, resource));
        addButton(new GuiEnergyInfo(() -> Arrays.asList(
              TextComponentUtil.build(Translation.of("gui.mekanism.storing"), ": ", EnergyDisplay.of(tile.getEnergy(), tile.getMaxEnergy())),
              TextComponentUtil.build(Translation.of("gui.mekanism.input"), ": ", EnergyDisplay.of(tile.getLastInput()), "/t"),
              TextComponentUtil.build(Translation.of("gui.mekanism.output"), ": ", EnergyDisplay.of(tile.getLastOutput()), "/t")
        ), this, resource));
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        drawString(tile.getName(), (xSize / 2) - (getStringWidth(tile.getName()) / 2), 6, 0x404040);
        drawString(TextComponentUtil.translate("container.inventory"), 8, (ySize - 94) + 2, 0x404040);
        drawString(TextComponentUtil.build(Translation.of("gui.mekanism.input"), ":"), 53, 26, 0x00CD00);
        drawString(TextComponentUtil.build(EnergyDisplay.of(tile.getLastInput()), "/t"), 53, 35, 0x00CD00);
        drawString(TextComponentUtil.build(Translation.of("gui.mekanism.output"), ":"), 53, 44, 0x00CD00);
        drawString(TextComponentUtil.build(EnergyDisplay.of(tile.getLastOutput()), "/t"), 53, 53, 0x00CD00);
        //TODO: 1.14 Convert to GuiElement
        int xAxis = mouseX - guiLeft;
        int yAxis = mouseY - guiTop;
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
        int start = 0;
        while (true) {
            int renderRemaining;
            if (scale > 16) {
                renderRemaining = 16;
                scale -= 16;
            } else {
                renderRemaining = scale;
                scale = 0;
            }
            minecraft.textureManager.bindTexture(PlayerContainer.field_226615_c_);
            drawTexturedRectFromIcon(guiLeft + xPos, guiTop + yPos + 58 - renderRemaining - start, MekanismRenderer.energyIcon, 16, renderRemaining);
            start += 16;
            if (renderRemaining == 0 || scale == 0) {
                break;
            }
        }
        minecraft.textureManager.bindTexture(getGuiLocation());
        drawTexturedRect(guiLeft + xPos, guiTop + yPos, 176, side == 0 ? 0 : 54, 16, 54);
    }
}