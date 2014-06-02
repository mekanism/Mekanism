package mekanism.client.gui;

import java.util.ArrayList;
import java.util.List;

import mekanism.api.Coord4D;
import mekanism.api.ListUtils;
import mekanism.api.gas.GasTank;
import mekanism.client.gui.GuiEnergyInfo.IInfoHandler;
import mekanism.client.gui.GuiFluidGauge.IFluidInfoHandler;
import mekanism.client.gui.GuiGasGauge.IGasInfoHandler;
import mekanism.client.gui.GuiGauge.Type;
import mekanism.client.gui.GuiProgress.IProgressInfoHandler;
import mekanism.client.gui.GuiProgress.ProgressBar;
import mekanism.client.gui.GuiSlot.SlotOverlay;
import mekanism.client.gui.GuiSlot.SlotType;
import mekanism.client.sound.SoundHandler;
import mekanism.common.Mekanism;
import mekanism.common.inventory.container.ContainerChemicalWasher;
import mekanism.common.network.PacketTileEntity;
import mekanism.common.tile.TileEntityChemicalWasher;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraftforge.fluids.FluidTank;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiChemicalWasher extends GuiMekanism
{
	public TileEntityChemicalWasher tileEntity;

	public GuiChemicalWasher(InventoryPlayer inventory, TileEntityChemicalWasher tentity)
	{
		super(tentity, new ContainerChemicalWasher(inventory, tentity));
		tileEntity = tentity;

		guiElements.add(new GuiRedstoneControl(this, tileEntity, MekanismUtils.getResource(ResourceType.GUI, "GuiChemicalWasher.png")));
		guiElements.add(new GuiBucketIO(this, MekanismUtils.getResource(ResourceType.GUI, "GuiChemicalWasher.png")));
		guiElements.add(new GuiEnergyInfo(new IInfoHandler() {
			@Override
			public List<String> getInfo()
			{
				String multiplier = MekanismUtils.getEnergyDisplay(tileEntity.ENERGY_USAGE);
				return ListUtils.asList("Using: " + multiplier + "/t", "Needed: " + MekanismUtils.getEnergyDisplay(tileEntity.getMaxEnergy()-tileEntity.getEnergy()));
			}
		}, this, MekanismUtils.getResource(ResourceType.GUI, "GuiChemicalWasher.png")));
		guiElements.add(new GuiFluidGauge(new IFluidInfoHandler()
		{
			@Override
			public FluidTank getTank()
			{
				return tileEntity.fluidTank;
			}
		}, Type.STANDARD, this, MekanismUtils.getResource(ResourceType.GUI, "GuiChemicalWasher.png"), 5, 4));
		guiElements.add(new GuiGasGauge(new IGasInfoHandler() {
			@Override
			public GasTank getTank()
			{
				return tileEntity.inputTank;
			}
		}, GuiGauge.Type.STANDARD, this, MekanismUtils.getResource(ResourceType.GUI, "GuiChemicalWasher.png"), 26, 13));
		guiElements.add(new GuiGasGauge(new IGasInfoHandler() {
			@Override
			public GasTank getTank()
			{
				return tileEntity.outputTank;
			}
		}, GuiGauge.Type.STANDARD, this, MekanismUtils.getResource(ResourceType.GUI, "GuiChemicalWasher.png"), 133, 13));

		guiElements.add(new GuiSlot(SlotType.NORMAL, this, MekanismUtils.getResource(ResourceType.GUI, "GuiChemicalWasher.png"), 154, 4).with(SlotOverlay.POWER));
		guiElements.add(new GuiSlot(SlotType.NORMAL, this, MekanismUtils.getResource(ResourceType.GUI, "GuiChemicalWasher.png"), 154, 55).with(SlotOverlay.MINUS));

		guiElements.add(new GuiProgress(new IProgressInfoHandler()
		{
			@Override
			public double getProgress()
			{
				return tileEntity.isActive ? 1 : 0;
			}
		}, ProgressBar.LARGE_RIGHT, this, MekanismUtils.getResource(ResourceType.GUI, "GuiChemicalWasher.png"), 62, 38));
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY)
	{
		fontRendererObj.drawString(tileEntity.getInventoryName(), 45, 4, 0x404040);

		super.drawGuiContainerForegroundLayer(mouseX, mouseY);
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTick, int mouseX, int mouseY)
	{
		mc.renderEngine.bindTexture(MekanismUtils.getResource(ResourceType.GUI, "GuiChemicalWasher.png"));
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		int guiWidth = (width - xSize) / 2;
		int guiHeight = (height - ySize) / 2;
		drawTexturedModalRect(guiWidth, guiHeight, 0, 0, xSize, ySize);

		super.drawGuiContainerBackgroundLayer(partialTick, mouseX, mouseY);
	}

	@Override
	protected void mouseClicked(int x, int y, int button)
	{
		super.mouseClicked(x, y, button);

		if(button == 0)
		{
			int xAxis = (x - (width - xSize) / 2);
			int yAxis = (y - (height - ySize) / 2);

			if(xAxis > 45 && xAxis < 63 && yAxis > 13 && yAxis < 21)
			{
				ArrayList data = new ArrayList();
				data.add(0);

				Mekanism.packetPipeline.sendToServer(new PacketTileEntity(Coord4D.get(tileEntity), data));
				SoundHandler.playSound("gui.button.press");
			}
		}
	}

}
