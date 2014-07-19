package mekanism.generators.client.gui;

import java.util.List;

import mekanism.api.ListUtils;
import mekanism.client.gui.GuiEnergyInfo;
import mekanism.client.gui.GuiEnergyInfo.IInfoHandler;
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
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

@SideOnly(Side.CLIENT)
public class GuiReactorController extends GuiMekanism
{
	public TileEntityReactorController tileEntity;

	public GuiReactorController(InventoryPlayer inventory, TileEntityReactorController tentity)
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
		guiElements.add(new GuiPowerBar(this, tileEntity, MekanismUtils.getResource(ResourceType.GUI, "GuiBlank.png"), 164, 15));
		guiElements.add(new GuiSlot(SlotType.NORMAL, this, MekanismUtils.getResource(ResourceType.GUI, "GuiBlank.png"), 142, 34));
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY)
	{
		super.drawGuiContainerForegroundLayer(mouseX, mouseY);

		fontRendererObj.drawString(tileEntity.getInventoryName(), 30, 6, 0x404040);
		fontRendererObj.drawString(MekanismUtils.localize("container.inventory"), 8, (ySize - 96) + 2, 0x404040);
		fontRendererObj.drawString(MekanismUtils.getEnergyDisplay(tileEntity.getEnergy()), 151, (ySize - 96) + 2, 0x00CD00);
		if(tileEntity.getReactor() == null)
		{
			fontRendererObj.drawString(MekanismUtils.localize("container.reactor.notFormed"), 8, 16, 0x404040);
		}
		else
		{
			fontRendererObj.drawString(MekanismUtils.localize("container.reactor.formed"), 8, 16, 0x404040);
			fontRendererObj.drawString(MekanismUtils.localize("plasma") + ": " + tileEntity.getReactor().getPlasmaTemp(), 8, 26, 0x404040);
			fontRendererObj.drawString(MekanismUtils.localize("casing") + ": " + tileEntity.getReactor().getCaseTemp(), 8, 36, 0x404040);
			fontRendererObj.drawString(MekanismUtils.localize("fuelLevel") + ": " + tileEntity.fuelTank.getStored(), 8, 46, 0x404040);
			fontRendererObj.drawString(MekanismUtils.localize("deuterium") + ": " + tileEntity.deuteriumTank.getStored(), 8, 56, 0x404040);
			fontRendererObj.drawString(MekanismUtils.localize("tritium") + ": " + tileEntity.tritiumTank.getStored(), 8, 66, 0x404040);
			fontRendererObj.drawString(MekanismUtils.localize("water") + ": " + tileEntity.waterTank.getFluidAmount(), 8, 76, 0x404040);
			fontRendererObj.drawString(MekanismUtils.localize("steam") + ": " + tileEntity.steamTank.getFluidAmount(), 8, 86, 0x404040);
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
