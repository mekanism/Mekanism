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

		guiElements.add(new GuiRedstoneControl(this, tileEntity, tileEntity.guiLocation));
		guiElements.add(new GuiUpgradeTab(this, tileEntity, tileEntity.guiLocation));
		guiElements.add(new GuiSideConfigurationTab(this, tileEntity, tileEntity.guiLocation));
		guiElements.add(new GuiTransporterConfigTab(this, 34, tileEntity, tileEntity.guiLocation));
		guiElements.add(new GuiPowerBar(this, tileEntity, tileEntity.guiLocation, 164, 15));
		guiElements.add(new GuiEnergyInfo(new IInfoHandler() {
			@Override
			public List<String> getInfo()
			{
				String multiplier = MekanismUtils.getEnergyDisplay(tileEntity.energyPerTick);
				return ListUtils.asList(MekanismUtils.localize("gui.using") + ": " + multiplier + "/t", MekanismUtils.localize("gui.needed") + ": " + MekanismUtils.getEnergyDisplay(tileEntity.getMaxEnergy()-tileEntity.getEnergy()));
			}
		}, this, tileEntity.guiLocation));

		guiElements.add(new GuiSlot(SlotType.INPUT, this, tileEntity.guiLocation, 55, 16));
		guiElements.add(new GuiSlot(SlotType.POWER, this, tileEntity.guiLocation, 30, 34).with(SlotOverlay.POWER));
		guiElements.add(new GuiSlot(SlotType.EXTRA, this, tileEntity.guiLocation, 55, 52));
		guiElements.add(new GuiSlot(SlotType.OUTPUT_LARGE, this, tileEntity.guiLocation, 111, 30));

		guiElements.add(new GuiProgress(new IProgressInfoHandler()
		{
			@Override
			public double getProgress()
			{
				return tileEntity.getScaledProgress();
			}
		}, getProgressType(), this, tileEntity.guiLocation, 77, 37));
	}
	
	public ProgressBar getProgressType()
	{
		return ProgressBar.BLUE;
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY)
	{
		int xAxis = (mouseX - (width - xSize) / 2);
		int yAxis = (mouseY - (height - ySize) / 2);

		fontRendererObj.drawString(tileEntity.getInventoryName(), (xSize/2)-(fontRendererObj.getStringWidth(tileEntity.getInventoryName())/2), 6, 0x404040);
		fontRendererObj.drawString(MekanismUtils.localize("container.inventory"), 8, (ySize - 96) + 2, 0x404040);

		if(xAxis >= 61 && xAxis <= 67 && yAxis >= 37 && yAxis <= 49)
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
		int guiWidth = (width - xSize) / 2;
		int guiHeight = (height - ySize) / 2;
		drawTexturedModalRect(guiWidth, guiHeight, 0, 0, xSize, ySize);

		int displayInt;

		if(tileEntity.getScaledGasLevel(12) > 0)
		{
			displayInt = tileEntity.getScaledGasLevel(12);
			displayGauge(61, 37 + 12 - displayInt, 6, displayInt, tileEntity.gasTank.getGas());
		}

		super.drawGuiContainerBackgroundLayer(partialTick, mouseX, mouseY);
	}

	public void displayGauge(int xPos, int yPos, int sizeX, int sizeY, GasStack gas)
	{
		if(gas == null)
		{
			return;
		}

		int guiWidth = (width - xSize) / 2;
		int guiHeight = (height - ySize) / 2;

		mc.renderEngine.bindTexture(MekanismRenderer.getBlocksTexture());
		drawTexturedModelRectFromIcon(guiWidth + xPos, guiHeight + yPos, gas.getGas().getIcon(), sizeX, sizeY);
	}
}