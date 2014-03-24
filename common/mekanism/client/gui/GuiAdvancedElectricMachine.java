package mekanism.client.gui;

import java.util.List;

import mekanism.api.ListUtils;
import mekanism.api.gas.GasStack;
import mekanism.client.gui.GuiEnergyInfo.IInfoHandler;
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
		guiElements.add(new GuiUpgradeManagement(this, tileEntity, tileEntity.guiLocation));
		guiElements.add(new GuiConfigurationTab(this, tileEntity, tileEntity.guiLocation));
		guiElements.add(new GuiPowerBar(this, tileEntity, tileEntity.guiLocation, 164, 15));
		guiElements.add(new GuiEnergyInfo(new IInfoHandler() {
			@Override
			public List<String> getInfo()
			{
				String multiplier = MekanismUtils.getEnergyDisplay(MekanismUtils.getEnergyPerTick(tileEntity.getSpeedMultiplier(), tileEntity.getEnergyMultiplier(), tileEntity.ENERGY_PER_TICK));
				return ListUtils.asList("Using: " + multiplier + "/t", "Needed: " + MekanismUtils.getEnergyDisplay(tileEntity.getMaxEnergy()-tileEntity.getEnergy()));
			}
		}, this, tileEntity, tileEntity.guiLocation));
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY)
	{
		int xAxis = (mouseX - (width - xSize) / 2);
		int yAxis = (mouseY - (height - ySize) / 2);

		fontRenderer.drawString(tileEntity.getInvName(), (xSize/2)-(fontRenderer.getStringWidth(tileEntity.getInvName())/2), 6, 0x404040);
		fontRenderer.drawString("Inventory", 8, (ySize - 96) + 2, 0x404040);

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

		int xAxis = mouseX - guiWidth;
		int yAxis = mouseY - guiHeight;

		int displayInt;

		displayInt = tileEntity.getScaledProgress(24);
		drawTexturedModalRect(guiWidth + 79, guiHeight + 39, 176, 0, displayInt + 1, 7);

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