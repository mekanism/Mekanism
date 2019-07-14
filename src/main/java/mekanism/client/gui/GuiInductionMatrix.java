package mekanism.client.gui;

import java.util.Arrays;
import mekanism.client.gui.element.GuiEnergyInfo;
import mekanism.client.gui.element.tab.GuiMatrixTab;
import mekanism.client.gui.element.tab.GuiMatrixTab.MatrixTab;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.inventory.container.ContainerInductionMatrix;
import mekanism.common.tile.TileEntityInductionCasing;
import mekanism.common.util.LangUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiInductionMatrix extends GuiMekanismTile<TileEntityInductionCasing> {

    public GuiInductionMatrix(InventoryPlayer inventory, TileEntityInductionCasing tile) {
        super(tile, new ContainerInductionMatrix(inventory, tile));
        ResourceLocation resource = getGuiLocation();
        addGuiElement(new GuiMatrixTab(this, tileEntity, MatrixTab.STAT, resource));
        addGuiElement(new GuiEnergyInfo(() -> Arrays.asList(LangUtils.localize("gui.storing") + ": " + MekanismUtils.getEnergyDisplay(tileEntity.getEnergy(), tileEntity.getMaxEnergy()),
              LangUtils.localize("gui.input") + ": " + MekanismUtils.getEnergyDisplay(tileEntity.getLastInput()) + "/t",
              LangUtils.localize("gui.output") + ": " + MekanismUtils.getEnergyDisplay(tileEntity.getLastOutput()) + "/t"), this, resource));
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        fontRenderer.drawString(tileEntity.getName(), (xSize / 2) - (fontRenderer.getStringWidth(tileEntity.getName()) / 2), 6, 0x404040);
        fontRenderer.drawString(LangUtils.localize("container.inventory"), 8, (ySize - 94) + 2, 0x404040);
        fontRenderer.drawString(LangUtils.localize("gui.input") + ":", 53, 26, 0x00CD00);
        fontRenderer.drawString(MekanismUtils.getEnergyDisplay(tileEntity.getLastInput()) + "/t", 53, 35, 0x00CD00);
        fontRenderer.drawString(LangUtils.localize("gui.output") + ":", 53, 44, 0x00CD00);
        fontRenderer.drawString(MekanismUtils.getEnergyDisplay(tileEntity.getLastOutput()) + "/t", 53, 53, 0x00CD00);
        int xAxis = mouseX - guiLeft;
        int yAxis = mouseY - guiTop;
        if (xAxis >= 7 && xAxis <= 39 && yAxis >= 14 && yAxis <= 72) {
            displayTooltip(MekanismUtils.getEnergyDisplay(tileEntity.getEnergy(), tileEntity.getMaxEnergy()), xAxis, yAxis);
        }
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(int xAxis, int yAxis) {
        if (tileEntity.getScaledEnergyLevel(58) > 0) {
            displayGauge(7, 14, tileEntity.getScaledEnergyLevel(58), 0);
            displayGauge(23, 14, tileEntity.getScaledEnergyLevel(58), 1);
        }
    }

    @Override
    protected ResourceLocation getGuiLocation() {
        return MekanismUtils.getResource(ResourceType.GUI, "GuiInductionMatrix.png");
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
            mc.renderEngine.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
            drawTexturedModalRect(guiLeft + xPos, guiTop + yPos + 58 - renderRemaining - start, MekanismRenderer.energyIcon, 16, renderRemaining);
            start += 16;
            if (renderRemaining == 0 || scale == 0) {
                break;
            }
        }
        mc.renderEngine.bindTexture(getGuiLocation());
        drawTexturedModalRect(guiLeft + xPos, guiTop + yPos, 176, side == 0 ? 0 : 54, 16, 54);
    }
}