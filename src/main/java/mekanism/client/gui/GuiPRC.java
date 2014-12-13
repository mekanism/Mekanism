package mekanism.client.gui;

import java.util.List;

import mekanism.api.gas.GasTank;
import mekanism.api.util.ListUtils;
import mekanism.client.gui.GuiEnergyInfo.IInfoHandler;
import mekanism.client.gui.GuiFluidGauge.IFluidInfoHandler;
import mekanism.client.gui.GuiGasGauge.IGasInfoHandler;
import mekanism.client.gui.GuiProgress.IProgressInfoHandler;
import mekanism.client.gui.GuiProgress.ProgressBar;
import mekanism.client.gui.GuiSlot.SlotOverlay;
import mekanism.client.gui.GuiSlot.SlotType;
import mekanism.common.inventory.container.ContainerPRC;
import mekanism.common.tile.TileEntityPRC;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraftforge.fluids.FluidTank;

import org.lwjgl.opengl.GL11;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiPRC extends GuiMekanism
{
	public TileEntityPRC tileEntity;

	public GuiPRC(InventoryPlayer inventory, TileEntityPRC tentity)
	{
		super(tentity, new ContainerPRC(inventory, tentity));
		tileEntity = tentity;

		guiElements.add(new GuiRedstoneControl(this, tileEntity, MekanismUtils.getResource(ResourceType.GUI, "GuiBlank.png")));
		guiElements.add(new GuiConfigurationTab(this, tileEntity, MekanismUtils.getResource(ResourceType.GUI, "GuiBlank.png")));
		guiElements.add(new GuiUpgradeTab(this, tileEntity, MekanismUtils.getResource(ResourceType.GUI, "GuiBlank.png")));
		guiElements.add(new GuiEnergyInfo(new IInfoHandler() {
			@Override
			public List<String> getInfo()
			{
				String multiplier = MekanismUtils.getEnergyDisplay(tileEntity.energyPerTick);
				return ListUtils.asList("Using: " + multiplier + "/t", "Needed: " + MekanismUtils.getEnergyDisplay(tileEntity.getMaxEnergy() - tileEntity.getEnergy()));
			}
		}, this, tileEntity.guiLocation));
		guiElements.add(new GuiFluidGauge(new IFluidInfoHandler() {
			@Override
			public FluidTank getTank()
			{
				return tileEntity.inputFluidTank;
			}
		}, GuiGauge.Type.STANDARD, this, MekanismUtils.getResource(ResourceType.GUI, "GuiBlank.png"), 5, 10));
		guiElements.add(new GuiGasGauge(new IGasInfoHandler() {
			@Override
			public GasTank getTank()
			{
				return tileEntity.inputGasTank;
			}
		}, GuiGauge.Type.STANDARD, this, MekanismUtils.getResource(ResourceType.GUI, "GuiBlank.png"), 28, 10));
		guiElements.add(new GuiGasGauge(new IGasInfoHandler() {
			@Override
			public GasTank getTank()
			{
				return tileEntity.outputGasTank;
			}
		}, GuiGauge.Type.SMALL, this, MekanismUtils.getResource(ResourceType.GUI, "GuiBlank.png"), 140, 40));
		guiElements.add(new GuiPowerBar(this, tileEntity, MekanismUtils.getResource(ResourceType.GUI, "GuiBlank.png"), 164, 15));

		guiElements.add(new GuiSlot(SlotType.INPUT, this, MekanismUtils.getResource(ResourceType.GUI, "GuiBlank.png"), 53, 34));
		guiElements.add(new GuiSlot(SlotType.POWER, this, MekanismUtils.getResource(ResourceType.GUI, "GuiBlank.png"), 140, 18).with(SlotOverlay.POWER));
		guiElements.add(new GuiSlot(SlotType.OUTPUT, this, MekanismUtils.getResource(ResourceType.GUI, "GuiBlank.png"), 115, 34));

		guiElements.add(new GuiProgress(new IProgressInfoHandler()
		{
			@Override
			public double getProgress()
			{
				return tileEntity.getScaledProgress();
			}
		}, getProgressType(), this, MekanismUtils.getResource(ResourceType.GUI, "GuiBlank.png"), 75, 37));
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY)
	{
		int xAxis = (mouseX - (width - xSize) / 2);
		int yAxis = (mouseY - (height - ySize) / 2);

		fontRendererObj.drawString(tileEntity.getInventoryName(), 81, 6, 0x404040);
		fontRendererObj.drawString(MekanismUtils.localize("container.inventory"), 8, (ySize - 96) + 2, 0x404040);

		super.drawGuiContainerForegroundLayer(mouseX, mouseY);
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTick, int mouseX, int mouseY)
	{
		mc.renderEngine.bindTexture(MekanismUtils.getResource(ResourceType.GUI, "GuiBlank.png"));
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		int guiWidth = (width - xSize) / 2;
		int guiHeight = (height - ySize) / 2;
		drawTexturedModalRect(guiWidth, guiHeight, 0, 0, xSize, ySize);

		int xAxis = mouseX - guiWidth;
		int yAxis = mouseY - guiHeight;

		super.drawGuiContainerBackgroundLayer(partialTick, mouseX, mouseY);
	}

	public ProgressBar getProgressType()
	{
		return ProgressBar.MEDIUM;
	}
}
