package mekanism.induction.client.gui;

import mekanism.induction.common.BatteryManager;
import mekanism.induction.common.MekanismInduction;
import mekanism.induction.common.inventory.container.ContainerBattery;
import mekanism.induction.common.tileentity.TileEntityBattery;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;

import org.lwjgl.opengl.GL11;

import universalelectricity.core.electricity.ElectricityDisplay;
import universalelectricity.core.electricity.ElectricityDisplay.ElectricUnit;

public class GuiBattery extends GuiContainer
{
	private static final ResourceLocation TEXTURE = new ResourceLocation(MekanismInduction.DOMAIN, MekanismInduction.GUI_DIRECTORY + "batterybox_gui.png");
	public TileEntityBattery tileEntity;

	public GuiBattery(InventoryPlayer inventory, TileEntityBattery tentity)
	{
		super(new ContainerBattery(inventory, tentity));
		tileEntity = tentity;
		ySize += 41;
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY)
	{
		int xAxis = (mouseX - (width - xSize) / 2);
		int yAxis = (mouseY - (height - ySize) / 2);

		fontRenderer.drawString("Battery", 43, 6, 0x404040);
		fontRenderer.drawString(StatCollector.translateToLocal("container.inventory"), 8, ySize - 96 + 2, 0x404040);
		fontRenderer.drawString("Cells: " + tileEntity.clientCells + " / " + (tileEntity.clientVolume*BatteryManager.CELLS_PER_BATTERY), 62, 23, 0x404040);
		fontRenderer.drawString("Energy: ", 62, 33, 0x404040);
		fontRenderer.drawString(ElectricityDisplay.getDisplay(this.tileEntity.getEnergyStored(), ElectricUnit.JOULES, 4, true), 62, 43, 0x404040);
		fontRenderer.drawString("Max: " + ElectricityDisplay.getDisplayShort(this.tileEntity.getMaxEnergyStored(), ElectricUnit.JOULES), 62, 53, 0x404040);
		fontRenderer.drawString("Percentage: " + (int) (this.tileEntity.getEnergyStored() / this.tileEntity.getMaxEnergyStored() * 100) + "%", 62, 63, 0x404040);
		fontRenderer.drawString("Volume: " + tileEntity.structure.getVolume(), 62, 73, 0x404040);
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float par1, int mouseX, int mouseY)
	{
		mc.renderEngine.bindTexture(TEXTURE);
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		int guiWidth = (width - xSize) / 2;
		int guiHeight = (height - ySize) / 2;
		drawTexturedModalRect(guiWidth, guiHeight, 0, 0, xSize, ySize);

		int xAxis = (mouseX - (width - xSize) / 2);
		int yAxis = (mouseY - (height - ySize) / 2);

		int scale = (int) ((tileEntity.getEnergyStored() / tileEntity.getMaxEnergyStored()) * 105);
		drawTexturedModalRect(guiWidth + 61, guiHeight + 102, 0, 207, scale, 12);
	}
}
