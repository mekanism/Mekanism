package mekanism.client.gui;

import java.util.List;

import mekanism.api.gas.GasStack;
import mekanism.api.util.ListUtils;
import mekanism.client.gui.element.GuiEnergyInfo;
import mekanism.client.gui.element.GuiEnergyInfo.IInfoHandler;
import mekanism.client.gui.element.GuiPowerBar;
import mekanism.client.gui.element.GuiProgress;
import mekanism.client.gui.element.GuiRedstoneControl;
import mekanism.client.gui.element.GuiProgress.IProgressInfoHandler;
import mekanism.client.gui.element.GuiProgress.ProgressBar;
import mekanism.client.gui.element.GuiSideConfigurationTab;
import mekanism.client.gui.element.GuiSlot;
import mekanism.client.gui.element.GuiSlot.SlotOverlay;
import mekanism.client.gui.element.GuiSlot.SlotType;
import mekanism.client.gui.element.GuiTransporterConfigTab;
import mekanism.client.gui.element.GuiUpgradeTab;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.inventory.container.ContainerAdvancedElectricMachine;
import mekanism.common.tile.TileEntityAdvancedElectricMachine;
import mekanism.common.util.MekanismUtils;
import net.minecraft.entity.player.InventoryPlayer;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiAdvancedElectricMachine extends GuiMekanism
{
	public TileEntityAdvancedElectricMachine tileEntity;

	public GuiAdvancedElectricMachine(InventoryPlayer inventory, TileEntityAdvancedElectricMachine tentity)
	{
		super(tentity, new ContainerAdvancedElectricMachine(inventory, tentity));
		tileEntity = tentity;

		xSize = 176;
	    ySize = 186;

		guiElements.add(new GuiRedstoneControl(this, tileEntity, tileEntity.guiLocation, xSize, 63));
		guiElements.add(new GuiUpgradeTab(this, tileEntity, tileEntity.guiLocation, xSize, 5));
		guiElements.add(new GuiSideConfigurationTab(this, tileEntity, tileEntity.guiLocation,-26, 5));
		guiElements.add(new GuiTransporterConfigTab(this, tileEntity, tileEntity.guiLocation,-26, 34));
		guiElements.add(new GuiPowerBar(this, tileEntity, tileEntity.guiLocation, 7, 35));
		guiElements.add(new GuiEnergyInfo(new IInfoHandler() {
			@Override
			public List<String> getInfo()
			{
				String multiplier = MekanismUtils.getEnergyDisplay(tileEntity.energyPerTick);
				return ListUtils.asList(MekanismUtils.localize("gui.using") + ": " + multiplier + "/t", MekanismUtils.localize("gui.needed") + ": " + MekanismUtils.getEnergyDisplay(tileEntity.getMaxEnergy()-tileEntity.getEnergy()));
			}
		}, this, tileEntity.guiLocation,-26, 63));

		guiElements.add(new GuiSlot(SlotType.INPUT, this, tileEntity.guiLocation, 39, 20));
		guiElements.add(new GuiSlot(SlotType.POWER, this, tileEntity.guiLocation, 13, 71).with(SlotOverlay.POWER));
		guiElements.add(new GuiSlot(SlotType.EXTRA, this, tileEntity.guiLocation, 39, 56));
		guiElements.add(new GuiSlot(SlotType.OUTPUT_LARGE, this, tileEntity.guiLocation, 111, 34));

		guiElements.add(new GuiProgress(new IProgressInfoHandler()
		{
			@Override
			public double getProgress()
			{
				return tileEntity.getScaledProgress();
			}
		}, getProgressType(), this, tileEntity.guiLocation, 57, 39));
	}
	
	public ProgressBar getProgressType()
	{
		return ProgressBar.BLUE;
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY)
	{
		int xAxis = mouseX - guiLeft;
		int yAxis = mouseY - guiTop;

		mc.renderEngine.bindTexture(tileEntity.guiLocation);
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		drawTexturedModalRect(guiLeft + 44, guiTop + 40, 176, 0, 8, 14);

		fontRendererObj.drawString(tileEntity.getInventoryName(), (xSize/2)-(fontRendererObj.getStringWidth(tileEntity.getInventoryName())/2), 6, 0x404040);
		fontRendererObj.drawString(MekanismUtils.localize("container.inventory"), 8, (ySize - 96) + 2, 0x404040);

		if(xAxis >= 43 && xAxis <= 52 && yAxis >= 39 && yAxis <= 54)
		{
			drawCreativeTabHoveringText(tileEntity.gasTank.getGas() != null ? tileEntity.gasTank.getGas().getGas().getLocalizedName() + ": " + tileEntity.gasTank.getStored() : MekanismUtils.localize("gui.none"), xAxis, yAxis);
		}

		super.drawGuiContainerForegroundLayer(mouseX, mouseY);
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTick, int mouseX, int mouseY)
	{
		mc.renderEngine.bindTexture(tileEntity.guiLocation);
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);

		int displayInt;

		if(tileEntity.getScaledGasLevel(12) > 0)
		{
			displayInt = tileEntity.getScaledGasLevel(12);
			displayGauge(45, 41 + 12 - displayInt, 6, displayInt, tileEntity.gasTank.getGas());
		}

		super.drawGuiContainerBackgroundLayer(partialTick, mouseX, mouseY);
	}

	public void displayGauge(int xPos, int yPos, int sizeX, int sizeY, GasStack gas)
	{
		if(gas == null)
		{
			return;
		}

		mc.renderEngine.bindTexture(MekanismRenderer.getBlocksTexture());
		drawTexturedModelRectFromIcon(guiLeft + xPos, guiTop + yPos, gas.getGas().getIcon(), sizeX, sizeY);
	}
}