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
import mekanism.client.gui.GuiPowerBar;
import mekanism.client.gui.GuiRedstoneControl;
import mekanism.client.gui.GuiSlot;
import mekanism.client.gui.GuiSlot.SlotOverlay;
import mekanism.client.gui.GuiSlot.SlotType;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import mekanism.generators.common.MekanismGenerators;
import mekanism.generators.common.inventory.container.ContainerReactorController;
import mekanism.generators.common.inventory.container.ContainerSolarGenerator;
import mekanism.generators.common.tile.TileEntitySolarGenerator;
import mekanism.generators.common.tile.reactor.TileEntityReactorController;

import net.minecraft.entity.player.InventoryPlayer;
import net.minecraftforge.fluids.FluidTank;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

@SideOnly(Side.CLIENT)
public class GuiReactorController extends GuiMekanism
{
	public TileEntityReactorController tileEntity;

	public GuiReactorController(InventoryPlayer inventory, final TileEntityReactorController tentity)
	{
		super(new ContainerReactorController(inventory, tentity));
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
		}, this, MekanismUtils.getResource(ResourceType.GUI, "GuiBlank.png")));
		guiElements.add(new GuiGasGauge(new IGasInfoHandler()
		{
			@Override
			public GasTank getTank()
			{
				return tentity.deuteriumTank;
			}
		}, Type.SMALL, this, MekanismUtils.getResource(ResourceType.GUI, "GuiBlank.png"), 124, 16));
		guiElements.add(new GuiGasGauge(new IGasInfoHandler()
		{
			@Override
			public GasTank getTank()
			{
				return tentity.tritiumTank;
			}
		}, Type.SMALL, this, MekanismUtils.getResource(ResourceType.GUI, "GuiBlank.png"), 124, 46));
		guiElements.add(new GuiGasGauge(new IGasInfoHandler()
		{
			@Override
			public GasTank getTank()
			{
				return tentity.fuelTank;
			}
		}, Type.STANDARD, this, MekanismUtils.getResource(ResourceType.GUI, "GuiBlank.png"), 144, 16));
		guiElements.add(new GuiFluidGauge(new IFluidInfoHandler()
		{
			@Override
			public FluidTank getTank()
			{
				return tentity.waterTank;
			}
		}, Type.SMALL, this, MekanismUtils.getResource(ResourceType.GUI, "GuiBlank.png"), 78, 46));
		guiElements.add(new GuiFluidGauge(new IFluidInfoHandler()
		{
			@Override
			public FluidTank getTank()
			{
				return tentity.steamTank;
			}
		}, Type.SMALL, this, MekanismUtils.getResource(ResourceType.GUI, "GuiBlank.png"), 98, 46));
		guiElements.add(new GuiPowerBar(this, tileEntity, MekanismUtils.getResource(ResourceType.GUI, "GuiBlank.png"), 164, 15));
		guiElements.add(new GuiSlot(SlotType.NORMAL, this, MekanismUtils.getResource(ResourceType.GUI, "GuiBlank.png"), 98, 26));
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY)
	{
		super.drawGuiContainerForegroundLayer(mouseX, mouseY);

		fontRendererObj.drawString(tileEntity.getInventoryName(), 30, 6, 0x404040);
		fontRendererObj.drawString(MekanismUtils.localize("container.inventory"), 8, (ySize - 96) + 2, 0x404040);
		if(tileEntity.getReactor() == null)
		{
			fontRendererObj.drawString(MekanismUtils.localize("container.reactor.notFormed"), 8, 16, 0x404040);
		}
		else
		{
			fontRendererObj.drawString(MekanismUtils.localize("container.reactor.formed"), 8, 16, 0x404040);
			fontRendererObj.drawString(MekanismUtils.localize("plasma") + ": " + (int)(tileEntity.getReactor().getPlasmaTemp()+23)+"C", 8, 26, 0x404040);
			fontRendererObj.drawString(MekanismUtils.localize("casing") + ": " + (int)(tileEntity.getReactor().getCaseTemp()+23)+"C", 8, 36, 0x404040);
		}
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTick, int mouseX, int mouseY)
	{
		mc.renderEngine.bindTexture(MekanismUtils.getResource(ResourceType.GUI, "GuiBlank.png"));
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		int guiWidth = (width - xSize) / 2;
		int guiHeight = (height - ySize) / 2;
		drawTexturedModalRect(guiWidth, guiHeight, 0, 0, xSize, ySize);

		super.drawGuiContainerBackgroundLayer(partialTick, mouseX, mouseY);
	}
}
