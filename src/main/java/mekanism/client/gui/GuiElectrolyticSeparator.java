package mekanism.client.gui;

import java.util.ArrayList;
import java.util.List;

import mekanism.api.Coord4D;
import mekanism.api.gas.GasTank;
import mekanism.api.util.ListUtils;
import mekanism.client.gui.element.GuiEnergyInfo;
import mekanism.client.gui.element.GuiEnergyInfo.IInfoHandler;
import mekanism.client.gui.element.GuiFluidGauge;
import mekanism.client.gui.element.GuiFluidGauge.IFluidInfoHandler;
import mekanism.client.gui.element.GuiGasGauge;
import mekanism.client.gui.element.GuiGasGauge.IGasInfoHandler;
import mekanism.client.gui.element.GuiGauge;
import mekanism.client.gui.element.GuiPowerBar;
import mekanism.client.gui.element.GuiProgress;
import mekanism.client.gui.element.GuiProgress.IProgressInfoHandler;
import mekanism.client.gui.element.GuiProgress.ProgressBar;
import mekanism.client.gui.element.GuiSlot;
import mekanism.client.gui.element.GuiSlot.SlotOverlay;
import mekanism.client.gui.element.GuiSlot.SlotType;
import mekanism.client.gui.element.GuiUpgradeTab;
import mekanism.client.sound.SoundHandler;
import mekanism.common.Mekanism;
import mekanism.common.inventory.container.ContainerElectrolyticSeparator;
import mekanism.common.network.PacketTileEntity.TileEntityMessage;
import mekanism.common.tile.TileEntityElectrolyticSeparator;
import mekanism.common.tile.TileEntityGasTank;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraftforge.fluids.FluidTank;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiElectrolyticSeparator extends GuiMekanism
{
	public TileEntityElectrolyticSeparator tileEntity;

	public GuiElectrolyticSeparator(InventoryPlayer inventory, TileEntityElectrolyticSeparator tentity)
	{
		super(tentity, new ContainerElectrolyticSeparator(inventory, tentity));

		tileEntity = tentity;

		guiElements.add(new GuiUpgradeTab(this, tileEntity, MekanismUtils.getResource(ResourceType.GUI, "GuiElectrolyticSeparator.png")));
		guiElements.add(new GuiEnergyInfo(new IInfoHandler() {
			@Override
			public List<String> getInfo()
			{
				String usage = MekanismUtils.getEnergyDisplay(tileEntity.clientEnergyUsed);
				return ListUtils.asList(MekanismUtils.localize("gui.using") + ": " + usage + "/t", MekanismUtils.localize("gui.needed") + ": " + MekanismUtils.getEnergyDisplay(tileEntity.getMaxEnergy()-tileEntity.getEnergy()));
			}
		}, this, MekanismUtils.getResource(ResourceType.GUI, "GuiElectrolyticSeparator.png")));
		guiElements.add(new GuiFluidGauge(new IFluidInfoHandler() {
			@Override
			public FluidTank getTank()
			{
				return tileEntity.fluidTank;
			}
		}, GuiGauge.Type.STANDARD, this, MekanismUtils.getResource(ResourceType.GUI, "GuiElectrolyticSeparator.png"), 5, 10));
		guiElements.add(new GuiGasGauge(new IGasInfoHandler() {
			@Override
			public GasTank getTank()
			{
				return tileEntity.leftTank;
			}
		}, GuiGauge.Type.SMALL, this, MekanismUtils.getResource(ResourceType.GUI, "GuiElectrolyticSeparator.png"), 58, 18));
		guiElements.add(new GuiGasGauge(new IGasInfoHandler() {
			@Override
			public GasTank getTank()
			{
				return tileEntity.rightTank;
			}
		}, GuiGauge.Type.SMALL, this, MekanismUtils.getResource(ResourceType.GUI, "GuiElectrolyticSeparator.png"), 100, 18));
		guiElements.add(new GuiPowerBar(this, tileEntity, MekanismUtils.getResource(ResourceType.GUI, "GuiElectrolyticSeparator.png"), 164, 15));

		guiElements.add(new GuiSlot(SlotType.NORMAL, this, MekanismUtils.getResource(ResourceType.GUI, "GuiElectrolyticSeparator.png"), 25, 34));
		guiElements.add(new GuiSlot(SlotType.NORMAL, this, MekanismUtils.getResource(ResourceType.GUI, "GuiElectrolyticSeparator.png"), 58, 51));
		guiElements.add(new GuiSlot(SlotType.NORMAL, this, MekanismUtils.getResource(ResourceType.GUI, "GuiElectrolyticSeparator.png"), 100, 51));
		guiElements.add(new GuiSlot(SlotType.NORMAL, this, MekanismUtils.getResource(ResourceType.GUI, "GuiElectrolyticSeparator.png"), 142, 34).with(SlotOverlay.POWER));

		guiElements.add(new GuiProgress(new IProgressInfoHandler()
		{
			@Override
			public double getProgress()
			{
				return tileEntity.isActive ? 1 : 0;
			}
		}, ProgressBar.BI, this, MekanismUtils.getResource(ResourceType.GUI, "GuiElectrolyticSeparator.png"), 78, 29));
	}

	@Override
	protected void mouseClicked(int x, int y, int button)
	{
		super.mouseClicked(x, y, button);

		int xAxis = (x - (width - xSize) / 2);
		int yAxis = (y - (height - ySize) / 2);

		if(xAxis > 8 && xAxis < 17 && yAxis > 73 && yAxis < 82)
		{
			ArrayList data = new ArrayList();
			data.add((byte)0);

			Mekanism.packetHandler.sendToServer(new TileEntityMessage(Coord4D.get(tileEntity), data));
			SoundHandler.playSound("gui.button.press");
		}
		else if(xAxis > 160 && xAxis < 169 && yAxis > 73 && yAxis < 82)
		{
			ArrayList data = new ArrayList();
			data.add((byte)1);

			Mekanism.packetHandler.sendToServer(new TileEntityMessage(Coord4D.get(tileEntity), data));
			SoundHandler.playSound("gui.button.press");
		}
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY)
	{
		fontRendererObj.drawString(tileEntity.getInventoryName(), 45, 6, 0x404040);

		String name = chooseByMode(tileEntity.dumpLeft, MekanismUtils.localize("gui.idle"), MekanismUtils.localize("gui.dumping"), MekanismUtils.localize("gui.dumping_excess"));
		renderScaledText(name, 21, 73, 0x404040, 66);

		name = chooseByMode(tileEntity.dumpRight, MekanismUtils.localize("gui.idle"), MekanismUtils.localize("gui.dumping"), MekanismUtils.localize("gui.dumping_excess"));
		renderScaledText(name, 156-(int)(fontRendererObj.getStringWidth(name)*getNeededScale(name, 66)), 73, 0x404040, 66);

		super.drawGuiContainerForegroundLayer(mouseX, mouseY);
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTick, int mouseX, int mouseY)
	{
		mc.renderEngine.bindTexture(MekanismUtils.getResource(ResourceType.GUI, "GuiElectrolyticSeparator.png"));
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		int guiWidth = (width - xSize) / 2;
		int guiHeight = (height - ySize) / 2;
		drawTexturedModalRect(guiWidth, guiHeight, 0, 0, xSize, ySize);

		int displayInt = chooseByMode(tileEntity.dumpLeft, 52, 60, 68);
		drawTexturedModalRect(guiWidth + 8, guiHeight + 73, 176, displayInt, 8, 8);

		displayInt = chooseByMode(tileEntity.dumpRight, 52, 60, 68);
		drawTexturedModalRect(guiWidth + 160, guiHeight + 73, 176, displayInt, 8, 8);

		super.drawGuiContainerBackgroundLayer(partialTick, mouseX, mouseY);
	}
	
	private <T> T chooseByMode(TileEntityGasTank.GasMode dumping, T idleOption, T dumpingOption, T dumpingExcessOption)
	{
		if(dumping.equals(TileEntityGasTank.GasMode.IDLE))
		{
			return idleOption;
		}
		else if(dumping.equals(TileEntityGasTank.GasMode.DUMPING))
		{
			return dumpingOption;
		}
		else if(dumping.equals(TileEntityGasTank.GasMode.DUMPING_EXCESS))
		{
			return dumpingExcessOption;
		}
		
		return idleOption; //should not happen;
	}
}
