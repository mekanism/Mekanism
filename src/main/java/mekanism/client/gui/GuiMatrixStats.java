package mekanism.client.gui;

import mekanism.api.energy.IStrictEnergyStorage;
import mekanism.client.gui.GuiEnergyGauge.IEnergyInfoHandler;
import mekanism.client.gui.GuiMatrixTab.MatrixTab;
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
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY)
	{
		int xAxis = (mouseX - (width - xSize) / 2);
		int yAxis = (mouseY - (height - ySize) / 2);

		fontRendererObj.drawString(MekanismUtils.localize("gui.matrixStats"), 45, 6, 0x404040);
		fontRendererObj.drawString(MekanismUtils.localize("gui.energy") + ":", 53, 26, 0x00CD00);
		fontRendererObj.drawString(MekanismUtils.getEnergyDisplay(tileEntity.getEnergy()) + "/" + MekanismUtils.getEnergyDisplay(tileEntity.getMaxEnergy()), 53, 35, 0x00CD00);
		fontRendererObj.drawString(MekanismUtils.localize("gui.output") + ":", 53, 44, 0x00CD00);
		fontRendererObj.drawString(MekanismUtils.getEnergyDisplay(tileEntity.structure.lastOutput) + "/" + MekanismUtils.getEnergyDisplay(tileEntity.structure.outputCap), 53, 53, 0x00CD00);

		super.drawGuiContainerForegroundLayer(mouseX, mouseY);
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTick, int mouseX, int mouseY)
	{
		super.drawGuiContainerBackgroundLayer(partialTick, mouseX, mouseY);

		mc.renderEngine.bindTexture(MekanismUtils.getResource(ResourceType.GUI, "GuiMatrixStats.png"));
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		int guiWidth = (width - xSize) / 2;
		int guiHeight = (height - ySize) / 2;
		drawTexturedModalRect(guiWidth, guiHeight, 0, 0, xSize, ySize);
	}
}
