package mekanism.generators.client.gui;

import java.util.List;

import mekanism.api.ListUtils;
import mekanism.api.gas.GasTank;
import mekanism.client.gui.GuiEnergyInfo;
import mekanism.client.gui.GuiEnergyInfo.IInfoHandler;
import mekanism.client.gui.GuiGasGauge;
import mekanism.client.gui.GuiGasGauge.IGasInfoHandler;
import mekanism.client.gui.GuiGasGauge.Type;
import mekanism.client.gui.GuiMekanism;
import mekanism.client.gui.GuiRedstoneControl;
import mekanism.common.Mekanism;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import mekanism.generators.common.inventory.container.ContainerHydrogenGenerator;
import mekanism.generators.common.tile.TileEntityHydrogenGenerator;
import net.minecraft.entity.player.InventoryPlayer;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiHydrogenGenerator extends GuiMekanism
{
	public TileEntityHydrogenGenerator tileEntity;

	public GuiHydrogenGenerator(InventoryPlayer inventory, TileEntityHydrogenGenerator tentity)
	{
		super(new ContainerHydrogenGenerator(inventory, tentity));
		tileEntity = tentity;
		guiElements.add(new GuiRedstoneControl(this, tileEntity, MekanismUtils.getResource(ResourceType.GUI, "GuiHydrogenGenerator.png")));
		guiElements.add(new GuiEnergyInfo(new IInfoHandler() {
			@Override
			public List<String> getInfo()
			{
				String production = MekanismUtils.getEnergyDisplay(tileEntity.generationRate);
				return ListUtils.asList("Producing: " + production + "/t", "Storing: " + MekanismUtils.getEnergyDisplay(tileEntity.getEnergy()));
			}
		}, this, tileEntity, MekanismUtils.getResource(ResourceType.GUI, "GuiHydrogenGenerator.png")));
		guiElements.add(new GuiGasGauge(new IGasInfoHandler() {
			@Override
			public GasTank getTank()
			{
				return tileEntity.fuelTank;
			}
		}, Type.WIDE, this, tileEntity, MekanismUtils.getResource(ResourceType.GUI, "GuiHydrogenGenerator.png"), 20, 20));
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY)
	{
		super.drawGuiContainerForegroundLayer(mouseX, mouseY);

		int xAxis = (mouseX - (width - xSize) / 2);
		int yAxis = (mouseY - (height - ySize) / 2);

		fontRenderer.drawString(tileEntity.getInvName(), 45, 6, 0x404040);
		fontRenderer.drawString(MekanismUtils.localize("container.inventory"), 8, (ySize - 96) + 2, 0x404040);
		fontRenderer.drawString(MekanismUtils.getEnergyDisplay(tileEntity.getEnergy()), 51, 26, 0x00CD00);
		fontRenderer.drawString("H: " + tileEntity.fuelTank.getStored(), 51, 35, 0x00CD00);
		fontRenderer.drawString(MekanismUtils.localize("gui.out") + ": " + MekanismUtils.getEnergyDisplay(tileEntity.getMaxOutput()) + "/t", 51, 44, 0x00CD00);

		if(xAxis >= 165 && xAxis <= 169 && yAxis >= 17 && yAxis <= 69)
		{
			drawCreativeTabHoveringText(MekanismUtils.getEnergyDisplay(tileEntity.getEnergy()), xAxis, yAxis);
		}
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTick, int mouseX, int mouseY)
	{
		mc.renderEngine.bindTexture(MekanismUtils.getResource(ResourceType.GUI, "GuiHydrogenGenerator.png"));
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		int guiWidth = (width - xSize) / 2;
		int guiHeight = (height - ySize) / 2;
		drawTexturedModalRect(guiWidth, guiHeight, 0, 0, xSize, ySize);

		int xAxis = mouseX - guiWidth;
		int yAxis = mouseY - guiHeight;

		super.drawGuiContainerBackgroundLayer(partialTick, mouseX, mouseY);
	}
}
