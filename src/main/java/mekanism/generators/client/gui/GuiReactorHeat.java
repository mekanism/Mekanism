package mekanism.generators.client.gui;

import java.util.ArrayList;
import java.util.List;

import mekanism.api.Coord4D;
import mekanism.api.energy.IStrictEnergyStorage;
import mekanism.api.util.ListUtils;
import mekanism.api.util.UnitDisplayUtils.TemperatureUnit;
import mekanism.client.gui.GuiMekanism;
import mekanism.client.gui.element.GuiEnergyGauge;
import mekanism.client.gui.element.GuiEnergyInfo;
import mekanism.client.gui.element.GuiFluidGauge;
import mekanism.client.gui.element.GuiNumberGauge;
import mekanism.client.gui.element.GuiProgress;
import mekanism.client.gui.element.GuiEnergyGauge.IEnergyInfoHandler;
import mekanism.client.gui.element.GuiEnergyInfo.IInfoHandler;
import mekanism.client.gui.element.GuiFluidGauge.IFluidInfoHandler;
import mekanism.client.gui.element.GuiGauge.Type;
import mekanism.client.gui.element.GuiNumberGauge.INumberInfoHandler;
import mekanism.client.gui.element.GuiProgress.IProgressInfoHandler;
import mekanism.client.gui.element.GuiProgress.ProgressBar;
import mekanism.client.sound.SoundHandler;
import mekanism.common.inventory.container.ContainerNull;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import mekanism.generators.common.MekanismGenerators;
import mekanism.generators.common.network.PacketGeneratorsGui.GeneratorsGuiMessage;
import mekanism.generators.common.tile.reactor.TileEntityReactorController;
import net.minecraft.block.BlockStaticLiquid;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.IIcon;
import net.minecraftforge.fluids.FluidTank;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import org.lwjgl.opengl.GL11;

@SideOnly(Side.CLIENT)
public class GuiReactorHeat extends GuiMekanism
{
	public TileEntityReactorController tileEntity;

	public GuiReactorHeat(InventoryPlayer inventory, final TileEntityReactorController tentity)
	{
		super(new ContainerNull(inventory.player, tentity));
		tileEntity = tentity;
		guiElements.add(new GuiEnergyInfo(new IInfoHandler()
		{
			@Override
			public List<String> getInfo()
			{
				return tileEntity.isFormed() ? ListUtils.asList(
						MekanismUtils.localize("gui.storing") + ": " + MekanismUtils.getEnergyDisplay(tileEntity.getEnergy()),
						MekanismUtils.localize("gui.producing") + ": " + MekanismUtils.getEnergyDisplay(tileEntity.getReactor().getPassiveGeneration(false, true)) + "/t") : new ArrayList();
			}
		}, this, MekanismUtils.getResource(ResourceType.GUI, "GuiTall.png")));
		guiElements.add(new GuiNumberGauge(new INumberInfoHandler()
		{
			@Override
			public IIcon getIcon()
			{
				return BlockStaticLiquid.getLiquidIcon("lava_still");
			}

			@Override
			public double getLevel()
			{
				return TemperatureUnit.AMBIENT.convertToK(tileEntity.getPlasmaTemp());
			}

			@Override
			public double getMaxLevel()
			{
				return 5E8;
			}

			@Override
			public String getText(double level)
			{
				return "Plasma: " + MekanismUtils.getTemperatureDisplay(level, TemperatureUnit.KELVIN);
			}
		}, Type.STANDARD, this, MekanismUtils.getResource(ResourceType.GUI, "GuiTall.png"), 7, 50));
		guiElements.add(new GuiProgress(new IProgressInfoHandler()
		{
			@Override
			public double getProgress()
			{
				return tileEntity.getPlasmaTemp() > tileEntity.getCaseTemp() ? 1 : 0;
			}
		}, ProgressBar.SMALL_RIGHT, this, MekanismUtils.getResource(ResourceType.GUI, "GuiTall.png"), 27, 75));
		guiElements.add(new GuiNumberGauge(new INumberInfoHandler()
		{
			@Override
			public IIcon getIcon()
			{
				return BlockStaticLiquid.getLiquidIcon("lava_still");
			}

			@Override
			public double getLevel()
			{
				return TemperatureUnit.AMBIENT.convertToK(tileEntity.getCaseTemp());
			}

			@Override
			public double getMaxLevel()
			{
				return 5E8;
			}

			@Override
			public String getText(double level)
			{
				return "Case: " + MekanismUtils.getTemperatureDisplay(level, TemperatureUnit.KELVIN);
			}
		}, Type.STANDARD, this, MekanismUtils.getResource(ResourceType.GUI, "GuiTall.png"), 61, 50));
		guiElements.add(new GuiProgress(new IProgressInfoHandler()
		{
			@Override
			public double getProgress()
			{
				return tileEntity.getCaseTemp() > 0 ? 1 : 0;
			}
		}, ProgressBar.SMALL_RIGHT, this, MekanismUtils.getResource(ResourceType.GUI, "GuiTall.png"), 81, 60));
		guiElements.add(new GuiProgress(new IProgressInfoHandler()
		{
			@Override
			public double getProgress()
			{
				return (tileEntity.getCaseTemp() > 0 && tileEntity.waterTank.getFluidAmount() > 0 && tileEntity.steamTank.getFluidAmount() < tileEntity.steamTank.getCapacity()) ? 1 : 0;
			}
		}, ProgressBar.SMALL_RIGHT, this, MekanismUtils.getResource(ResourceType.GUI, "GuiTall.png"), 81, 90));
		guiElements.add(new GuiFluidGauge(new IFluidInfoHandler()
		{
			@Override
			public FluidTank getTank()
			{
				return tentity.waterTank;
			}
		}, Type.SMALL, this, MekanismUtils.getResource(ResourceType.GUI, "GuiTall.png"), 115, 84));
		guiElements.add(new GuiFluidGauge(new IFluidInfoHandler()
		{
			@Override
			public FluidTank getTank()
			{
				return tentity.steamTank;
			}
		}, Type.SMALL, this, MekanismUtils.getResource(ResourceType.GUI, "GuiTall.png"), 151, 84));
		guiElements.add(new GuiEnergyGauge(new IEnergyInfoHandler()
		{
			@Override
			public IStrictEnergyStorage getEnergyStorage()
			{
				return tileEntity;
			}
		}, Type.SMALL, this, MekanismUtils.getResource(ResourceType.GUI, "GuiTall.png"), 115, 46));
		guiElements.add(new GuiFuelTab(this, tileEntity, MekanismUtils.getResource(ResourceType.GUI, "GuiTall.png")));
		guiElements.add(new GuiStatTab(this, tileEntity, MekanismUtils.getResource(ResourceType.GUI, "GuiTall.png")));
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY)
	{
		super.drawGuiContainerForegroundLayer(mouseX, mouseY);

		fontRendererObj.drawString(tileEntity.getInventoryName(), 46, 6, 0x404040);
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTick, int mouseX, int mouseY)
	{
		mc.renderEngine.bindTexture(MekanismUtils.getResource(ResourceType.GUI, "GuiTall.png"));
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		int guiWidth = (width - xSize) / 2;
		int guiHeight = (height - ySize) / 2;
		drawTexturedModalRect(guiWidth, guiHeight, 0, 0, xSize, ySize);

		int xAxis = (mouseX - (width - xSize) / 2);
		int yAxis = (mouseY - (height - ySize) / 2);

		if(xAxis >= 6 && xAxis <= 20 && yAxis >= 6 && yAxis <= 20)
		{
			drawTexturedModalRect(guiWidth + 6, guiHeight + 6, 176, 0, 14, 14);
		}
		else {
			drawTexturedModalRect(guiWidth + 6, guiHeight + 6, 176, 14, 14, 14);
		}

		super.drawGuiContainerBackgroundLayer(partialTick, mouseX, mouseY);
	}

	@Override
	public void mouseClicked(int mouseX, int mouseY, int button)
	{
		super.mouseClicked(mouseX, mouseY, button);

		int xAxis = (mouseX - (width - xSize) / 2);
		int yAxis = (mouseY - (height - ySize) / 2);

		if(button == 0)
		{
			if(xAxis >= 6 && xAxis <= 20 && yAxis >= 6 && yAxis <= 20)
			{
				SoundHandler.playSound("gui.button.press");
				MekanismGenerators.packetHandler.sendToServer(new GeneratorsGuiMessage(Coord4D.get(tileEntity), 10));
			}

		}
	}
}
