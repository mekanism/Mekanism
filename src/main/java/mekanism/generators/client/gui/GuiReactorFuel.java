package mekanism.generators.client.gui;

import java.util.List;

import mekanism.api.ListUtils;
import mekanism.api.gas.GasTank;
import mekanism.client.gui.GuiEnergyInfo;
import mekanism.client.gui.GuiEnergyInfo.IInfoHandler;
import mekanism.client.gui.GuiFluidGauge;
import mekanism.client.gui.GuiFluidGauge.IFluidInfoHandler;
import mekanism.client.gui.GuiGasGauge;
import mekanism.client.gui.GuiGasGauge.IGasInfoHandler;
import mekanism.client.gui.GuiGauge.Type;
import mekanism.client.gui.GuiMekanism;
import mekanism.client.gui.GuiNumberGauge;
import mekanism.client.gui.GuiNumberGauge.INumberInfoHandler;
import mekanism.client.gui.GuiPowerBar;
import mekanism.client.gui.GuiProgress;
import mekanism.client.gui.GuiProgress.IProgressInfoHandler;
import mekanism.client.gui.GuiProgress.ProgressBar;
import mekanism.client.gui.GuiSlot;
import mekanism.client.gui.GuiSlot.SlotType;
import mekanism.common.inventory.container.ContainerNull;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import mekanism.generators.common.inventory.container.ContainerReactorController;
import mekanism.generators.common.tile.reactor.TileEntityReactorController;

import net.minecraft.block.BlockStaticLiquid;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.IIcon;
import net.minecraftforge.fluids.FluidTank;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

@SideOnly(Side.CLIENT)
public class GuiReactorFuel extends GuiMekanism
{
	public TileEntityReactorController tileEntity;

	public GuiReactorFuel(InventoryPlayer inventory, final TileEntityReactorController tentity)
	{
		super(new ContainerNull(inventory.player, tentity));
		tileEntity = tentity;
		guiElements.add(new GuiEnergyInfo(new IInfoHandler()
		{
			@Override
			public List<String> getInfo()
			{
				return ListUtils.asList(
						"Storing: " + MekanismUtils.getEnergyDisplay(tileEntity.getEnergy()),
						"Max Output: " + MekanismUtils.getEnergyDisplay(tileEntity.getMaxOutput()) + "/t");
			}
		}, this, MekanismUtils.getResource(ResourceType.GUI, "GuiTall.png")));
		guiElements.add(new GuiGasGauge(new IGasInfoHandler()
		{
			@Override
			public GasTank getTank()
			{
				return tentity.deuteriumTank;
			}
		}, Type.SMALL, this, MekanismUtils.getResource(ResourceType.GUI, "GuiTall.png"), 25, 64));
		guiElements.add(new GuiGasGauge(new IGasInfoHandler()
		{
			@Override
			public GasTank getTank()
			{
				return tentity.fuelTank;
			}
		}, Type.STANDARD, this, MekanismUtils.getResource(ResourceType.GUI, "GuiTall.png"), 79, 50));
		guiElements.add(new GuiGasGauge(new IGasInfoHandler()
		{
			@Override
			public GasTank getTank()
			{
				return tentity.tritiumTank;
			}
		}, Type.SMALL, this, MekanismUtils.getResource(ResourceType.GUI, "GuiTall.png"), 133, 64));
		guiElements.add(new GuiProgress(new IProgressInfoHandler()
		{
			@Override
			public double getProgress()
			{
				return tileEntity.getActive() ? 1 : 0;
			}
		}, ProgressBar.SMALL_RIGHT, this, MekanismUtils.getResource(ResourceType.GUI, "GuiTall.png"), 45, 75));
		guiElements.add(new GuiProgress(new IProgressInfoHandler()
		{
			@Override
			public double getProgress()
			{
				return tileEntity.getActive() ? 1 : 0;
			}
		}, ProgressBar.SMALL_LEFT, this, MekanismUtils.getResource(ResourceType.GUI, "GuiTall.png"), 99, 75));
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY)
	{
		super.drawGuiContainerForegroundLayer(mouseX, mouseY);

		fontRendererObj.drawString(tileEntity.getInventoryName(), 6, 6, 0x404040);
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTick, int mouseX, int mouseY)
	{
		mc.renderEngine.bindTexture(MekanismUtils.getResource(ResourceType.GUI, "GuiTall.png"));
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		int guiWidth = (width - xSize) / 2;
		int guiHeight = (height - ySize) / 2;
		drawTexturedModalRect(guiWidth, guiHeight, 0, 0, xSize, ySize);

		super.drawGuiContainerBackgroundLayer(partialTick, mouseX, mouseY);
	}
}
