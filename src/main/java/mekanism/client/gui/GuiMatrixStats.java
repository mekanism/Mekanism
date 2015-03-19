package mekanism.client.gui;

import mekanism.api.energy.IStrictEnergyStorage;
import mekanism.client.gui.element.GuiEnergyGauge;
import mekanism.client.gui.element.GuiMatrixTab;
import mekanism.client.gui.element.GuiRateBar;
import mekanism.client.gui.element.GuiEnergyGauge.IEnergyInfoHandler;
import mekanism.client.gui.element.GuiMatrixTab.MatrixTab;
import mekanism.client.gui.element.GuiRateBar.IRateInfoHandler;
import mekanism.common.inventory.container.ContainerNull;
import mekanism.common.tile.TileEntityInductionCasing;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.entity.player.InventoryPlayer;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiMatrixStats extends GuiMekanism
{
	public TileEntityInductionCasing tileEntity;

	public GuiMatrixStats(InventoryPlayer inventory, TileEntityInductionCasing tentity)
	{
		super(tentity, new ContainerNull(inventory.player, tentity));
		tileEntity = tentity;
		guiElements.add(new GuiMatrixTab(this, tileEntity, MatrixTab.MAIN, 6, MekanismUtils.getResource(ResourceType.GUI, "GuiMatrixStats.png")));
		guiElements.add(new GuiEnergyGauge(new IEnergyInfoHandler()
		{
			@Override
			public IStrictEnergyStorage getEnergyStorage()
			{
				return tileEntity;
			}
		}, GuiEnergyGauge.Type.STANDARD, this, MekanismUtils.getResource(ResourceType.GUI, "GuiMatrixStats.png"), 6, 13));
		guiElements.add(new GuiRateBar(this, new IRateInfoHandler()
		{
			@Override
			public String getTooltip()
			{
				return MekanismUtils.localize("gui.outputting") + ": " + MekanismUtils.getEnergyDisplay(tileEntity.structure.lastOutput) + "/t";
			}
			
			@Override
			public double getLevel()
			{
				return tileEntity.structure.lastOutput/tileEntity.structure.outputCap;
			}
		}, MekanismUtils.getResource(ResourceType.GUI, "GuiMatrixStats.png"), 30, 13));
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY)
	{
		int xAxis = (mouseX - (width - xSize) / 2);
		int yAxis = (mouseY - (height - ySize) / 2);

		String stats = MekanismUtils.localize("gui.matrixStats");
		
		fontRendererObj.drawString(stats, (xSize/2)-(fontRendererObj.getStringWidth(stats)/2), 6, 0x404040);
		fontRendererObj.drawString(MekanismUtils.localize("gui.energy") + ":", 53, 26, 0x797979);
		fontRendererObj.drawString(MekanismUtils.getEnergyDisplay(tileEntity.getEnergy()) + "/" + MekanismUtils.getEnergyDisplay(tileEntity.getMaxEnergy()), 59, 35, 0x404040);
		fontRendererObj.drawString(MekanismUtils.localize("gui.output") + ":", 53, 46, 0x797979);
		fontRendererObj.drawString(MekanismUtils.getEnergyDisplay(tileEntity.structure.lastOutput) + "/" + MekanismUtils.getEnergyDisplay(tileEntity.structure.outputCap), 59, 55, 0x404040);
		
		fontRendererObj.drawString(MekanismUtils.localize("gui.dimensions") + ":", 8, 82, 0x797979);
		fontRendererObj.drawString(tileEntity.structure.volWidth + " x " + tileEntity.structure.volHeight + " x " + tileEntity.structure.volLength, 14, 91, 0x404040);
		fontRendererObj.drawString(MekanismUtils.localize("gui.constituents") + ":", 8, 102, 0x797979);
		fontRendererObj.drawString(tileEntity.clientCells + " " + MekanismUtils.localize("gui.cells"), 14, 111, 0x404040);
		fontRendererObj.drawString(tileEntity.clientProviders + " " + MekanismUtils.localize("gui.providers"), 14, 120, 0x404040);

		super.drawGuiContainerForegroundLayer(mouseX, mouseY);
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTick, int mouseX, int mouseY)
	{
		mc.renderEngine.bindTexture(MekanismUtils.getResource(ResourceType.GUI, "GuiMatrixStats.png"));
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		int guiWidth = (width - xSize) / 2;
		int guiHeight = (height - ySize) / 2;
		drawTexturedModalRect(guiWidth, guiHeight, 0, 0, xSize, ySize);
		
		super.drawGuiContainerBackgroundLayer(partialTick, mouseX, mouseY);
	}
}
