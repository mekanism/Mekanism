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
import mekanism.generators.common.inventory.container.ContainerBioGenerator;
import mekanism.generators.common.tile.TileEntityBioGenerator;
import net.minecraft.entity.player.InventoryPlayer;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiBioGenerator extends GuiMekanism
{
	public TileEntityBioGenerator tileEntity;

	public GuiBioGenerator(InventoryPlayer inventory, TileEntityBioGenerator tentity)
	{
		super(new ContainerBioGenerator(inventory, tentity));
		tileEntity = tentity;
		guiElements.add(new GuiRedstoneControl(this, tileEntity, MekanismUtils.getResource(ResourceType.GUI, "GuiBioGenerator.png")));
		guiElements.add(new GuiEnergyInfo(new IInfoHandler()
		{
			@Override
			public List<String> getInfo()
			{
				return ListUtils.asList(
						"Producing: " + MekanismUtils.getEnergyDisplay(tileEntity.isActive ? MekanismGenerators.bioGeneration : 0) + "/t",
						"Storing: " + MekanismUtils.getEnergyDisplay(tileEntity.getEnergy()),
						"Max Output: " + MekanismUtils.getEnergyDisplay(tileEntity.getMaxOutput()) + "/t");
			}
		}, this, MekanismUtils.getResource(ResourceType.GUI, "GuiBioGenerator.png")));
		guiElements.add(new GuiPowerBar(this, tileEntity, MekanismUtils.getResource(ResourceType.GUI, "GuiBioGenerator.png"), 164, 15));
		guiElements.add(new GuiSlot(SlotType.NORMAL, this, MekanismUtils.getResource(ResourceType.GUI, "GuiBioGenerator.png"), 16, 34));
		guiElements.add(new GuiSlot(SlotType.NORMAL, this, MekanismUtils.getResource(ResourceType.GUI, "GuiBioGenerator.png"), 142, 34).with(SlotOverlay.POWER));
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY)
	{
		super.drawGuiContainerForegroundLayer(mouseX, mouseY);

		fontRendererObj.drawString(tileEntity.getInvName(), 45, 6, 0x404040);
		fontRendererObj.drawString(MekanismUtils.localize("container.inventory"), 8, (ySize - 96) + 2, 0x404040);
		fontRendererObj.drawString(MekanismUtils.getEnergyDisplay(tileEntity.getEnergy()), 51, 26, 0x00CD00);
		fontRendererObj.drawString(MekanismUtils.localize("gui.bioGenerator.bioFuel") + ": " + tileEntity.bioFuelSlot.fluidStored, 51, 35, 0x00CD00);
		fontRendererObj.drawString(MekanismUtils.localize("gui.out") + ": " + MekanismUtils.getEnergyDisplay(tileEntity.getMaxOutput()) + "/t", 51, 44, 0x00CD00);
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTick, int mouseX, int mouseY)
	{
		mc.renderEngine.bindTexture(MekanismUtils.getResource(ResourceType.GUI, "GuiBioGenerator.png"));
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		int guiWidth = (width - xSize) / 2;
		int guiHeight = (height - ySize) / 2;
		drawTexturedModalRect(guiWidth, guiHeight, 0, 0, xSize, ySize);

		int displayInt;

		displayInt = tileEntity.getScaledFuelLevel(52);
		drawTexturedModalRect(guiWidth + 7, guiHeight + 17 + 52 - displayInt, 176, 52 + 52 - displayInt, 4, displayInt);

		super.drawGuiContainerBackgroundLayer(partialTick, mouseX, mouseY);
	}
}
