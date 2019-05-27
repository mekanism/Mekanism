package mekanism.client.gui;

import java.util.Arrays;
import mekanism.client.gui.element.GuiEnergyGauge;
import mekanism.client.gui.element.GuiEnergyInfo;
import mekanism.client.gui.element.GuiMatrixTab;
import mekanism.client.gui.element.GuiMatrixTab.MatrixTab;
import mekanism.client.gui.element.GuiRateBar;
import mekanism.client.gui.element.GuiRateBar.IRateInfoHandler;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.inventory.container.ContainerNull;
import mekanism.common.tile.TileEntityInductionCasing;
import mekanism.common.util.LangUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiMatrixStats extends GuiMekanismTile<TileEntityInductionCasing> {

    public GuiMatrixStats(InventoryPlayer inventory, TileEntityInductionCasing tile) {
        super(tile, new ContainerNull(inventory.player, tile));
        ResourceLocation resource = getGuiLocation();
        addGuiElement(new GuiMatrixTab(this, tileEntity, MatrixTab.MAIN, 6, resource));
        addGuiElement(new GuiEnergyGauge(() -> tileEntity, GuiEnergyGauge.Type.STANDARD, this, resource, 6, 13));
        addGuiElement(new GuiRateBar(this, new IRateInfoHandler() {
            @Override
            public String getTooltip() {
                return LangUtils.localize("gui.receiving") + ": " + MekanismUtils.getEnergyDisplay(tileEntity.structure.getLastInput()) + "/t";
            }

            @Override
            public double getLevel() {
                return tileEntity.structure.getLastInput() / tileEntity.structure.getTransferCap();
            }
        }, resource, 30, 13));
        addGuiElement(new GuiRateBar(this, new IRateInfoHandler() {
            @Override
            public String getTooltip() {
                return LangUtils.localize("gui.outputting") + ": " + MekanismUtils.getEnergyDisplay(tileEntity.structure.getLastOutput()) + "/t";
            }

            @Override
            public double getLevel() {
                return tileEntity.structure.getLastOutput() / tileEntity.structure.getTransferCap();
            }
        }, resource, 38, 13));
        addGuiElement(new GuiEnergyInfo(() -> Arrays.asList(
              LangUtils.localize("gui.storing") + ": " + MekanismUtils.getEnergyDisplay(tileEntity.getEnergy(), tileEntity.getMaxEnergy()),
              LangUtils.localize("gui.input") + ": " + MekanismUtils.getEnergyDisplay(tileEntity.structure.getLastInput()) + "/t",
              LangUtils.localize("gui.output") + ": " + MekanismUtils.getEnergyDisplay(tileEntity.structure.getLastOutput()) + "/t"), this, resource));
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        String stats = LangUtils.localize("gui.matrixStats");
        fontRenderer.drawString(stats, (xSize / 2) - (fontRenderer.getStringWidth(stats) / 2), 6, 0x404040);
        fontRenderer.drawString(LangUtils.localize("gui.input") + ":", 53, 26, 0x797979);
        fontRenderer.drawString(MekanismUtils.getEnergyDisplay(tileEntity.structure.getLastInput()) + "/" +
                                MekanismUtils.getEnergyDisplay(tileEntity.structure.getTransferCap()), 59, 35, 0x404040);
        fontRenderer.drawString(LangUtils.localize("gui.output") + ":", 53, 46, 0x797979);
        fontRenderer.drawString(MekanismUtils.getEnergyDisplay(tileEntity.structure.getLastOutput()) + "/" +
                                MekanismUtils.getEnergyDisplay(tileEntity.structure.getTransferCap()), 59, 55, 0x404040);
        fontRenderer.drawString(LangUtils.localize("gui.dimensions") + ":", 8, 82, 0x797979);
        fontRenderer.drawString(tileEntity.structure.volWidth + " x " + tileEntity.structure.volHeight + " x " + tileEntity.structure.volLength, 14, 91, 0x404040);
        fontRenderer.drawString(LangUtils.localize("gui.constituents") + ":", 8, 102, 0x797979);
        fontRenderer.drawString(tileEntity.structure.getCellCount() + " " + LangUtils.localize("gui.cells"), 14, 111, 0x404040);
        fontRenderer.drawString(tileEntity.structure.getProviderCount() + " " + LangUtils.localize("gui.providers"), 14, 120, 0x404040);
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTick, int mouseX, int mouseY) {
        mc.renderEngine.bindTexture(getGuiLocation());
        MekanismRenderer.resetColor();
        int guiWidth = (width - xSize) / 2;
        int guiHeight = (height - ySize) / 2;
        drawTexturedModalRect(guiWidth, guiHeight, 0, 0, xSize, ySize);
        super.drawGuiContainerBackgroundLayer(partialTick, mouseX, mouseY);
    }

    @Override
    protected ResourceLocation getGuiLocation() {
        return MekanismUtils.getResource(ResourceType.GUI, "GuiNull.png");
    }
}