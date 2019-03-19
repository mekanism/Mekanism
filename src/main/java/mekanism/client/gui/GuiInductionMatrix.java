package mekanism.client.gui;

import java.util.Arrays;
import mekanism.client.gui.element.GuiEnergyInfo;
import mekanism.client.gui.element.GuiMatrixTab;
import mekanism.client.gui.element.GuiMatrixTab.MatrixTab;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.inventory.container.ContainerInductionMatrix;
import mekanism.common.tile.TileEntityInductionCasing;
import mekanism.common.util.LangUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

@SideOnly(Side.CLIENT)
public class GuiInductionMatrix extends GuiMekanismTile<TileEntityInductionCasing> {

    public GuiInductionMatrix(InventoryPlayer inventory, TileEntityInductionCasing tile) {
        super(tile, new ContainerInductionMatrix(inventory, tile));
        ResourceLocation resource = getGuiLocation();
        addGuiElement(new GuiMatrixTab(this, tileEntity, MatrixTab.STAT, 6, resource));
        addGuiElement(new GuiEnergyInfo(() -> Arrays.asList(
              LangUtils.localize("gui.storing") + ": " + MekanismUtils
                    .getEnergyDisplay(tileEntity.getEnergy(), tileEntity.getMaxEnergy()),
              LangUtils.localize("gui.input") + ": " + MekanismUtils.getEnergyDisplay(tileEntity.structure.lastInput)
                    + "/t",
              LangUtils.localize("gui.output") + ": " + MekanismUtils.getEnergyDisplay(tileEntity.structure.lastOutput)
                    + "/t"), this, resource));
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        fontRenderer
              .drawString(tileEntity.getName(), (xSize / 2) - (fontRenderer.getStringWidth(tileEntity.getName()) / 2),
                    6, 0x404040);
        fontRenderer.drawString(LangUtils.localize("container.inventory"), 8, (ySize - 94) + 2, 0x404040);
        fontRenderer.drawString(LangUtils.localize("gui.input") + ":", 53, 26, 0x00CD00);
        fontRenderer
              .drawString(MekanismUtils.getEnergyDisplay(tileEntity.structure.lastInput) + "/t", 53, 35, 0x00CD00);
        fontRenderer.drawString(LangUtils.localize("gui.output") + ":", 53, 44, 0x00CD00);
        fontRenderer
              .drawString(MekanismUtils.getEnergyDisplay(tileEntity.structure.lastOutput) + "/t", 53, 53, 0x00CD00);
        int xAxis = (mouseX - (width - xSize) / 2);
        int yAxis = (mouseY - (height - ySize) / 2);
        if (xAxis >= 7 && xAxis <= 39 && yAxis >= 14 && yAxis <= 72) {
            drawHoveringText(MekanismUtils.getEnergyDisplay(tileEntity.getEnergy(), tileEntity.getMaxEnergy()), xAxis,
                  yAxis);
        }
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTick, int mouseX, int mouseY) {
        super.drawGuiContainerBackgroundLayer(partialTick, mouseX, mouseY);
        mc.renderEngine.bindTexture(getGuiLocation());
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        int guiWidth = (width - xSize) / 2;
        int guiHeight = (height - ySize) / 2;
        drawTexturedModalRect(guiWidth, guiHeight, 0, 0, xSize, ySize);
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
        int guiWidth = (width - xSize) / 2;
        int guiHeight = (height - ySize) / 2;

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

            mc.renderEngine.bindTexture(MekanismRenderer.getBlocksTexture());
            drawTexturedModalRect(guiWidth + xPos, guiHeight + yPos + 58 - renderRemaining - start,
                  MekanismRenderer.energyIcon, 16, 16 - (16 - renderRemaining));
            start += 16;

            if (renderRemaining == 0 || scale == 0) {
                break;
            }
        }

        mc.renderEngine.bindTexture(getGuiLocation());
        drawTexturedModalRect(guiWidth + xPos, guiHeight + yPos, 176, side == 0 ? 0 : 54, 16, 54);
    }
}