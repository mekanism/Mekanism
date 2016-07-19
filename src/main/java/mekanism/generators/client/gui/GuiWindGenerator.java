package mekanism.generators.client.gui;

import java.text.DecimalFormat;
import java.util.List;

import mekanism.api.EnumColor;
import mekanism.api.MekanismConfig.generators;
import mekanism.api.util.ListUtils;
import mekanism.client.gui.GuiMekanism;
import mekanism.client.gui.element.GuiElement.IInfoHandler;
import mekanism.client.gui.element.GuiEnergyInfo;
import mekanism.client.gui.element.GuiPowerBar;
import mekanism.client.gui.element.GuiRedstoneControl;
import mekanism.client.gui.element.GuiSecurityTab;
import mekanism.client.gui.element.GuiSlot;
import mekanism.client.gui.element.GuiSlot.SlotOverlay;
import mekanism.client.gui.element.GuiSlot.SlotType;
import mekanism.common.util.LangUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import mekanism.generators.common.inventory.container.ContainerWindGenerator;
import mekanism.generators.common.tile.TileEntityWindGenerator;
import net.minecraft.entity.player.InventoryPlayer;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiWindGenerator extends GuiMekanism
{
	public TileEntityWindGenerator tileEntity;

	private DecimalFormat powerFormat = new DecimalFormat("0.##");

	public GuiWindGenerator(InventoryPlayer inventory, TileEntityWindGenerator tentity)
	{
		super(new ContainerWindGenerator(inventory, tentity));
		tileEntity = tentity;
		guiElements.add(new GuiRedstoneControl(this, tileEntity, MekanismUtils.getResource(ResourceType.GUI, "GuiWindTurbine.png")));
		guiElements.add(new GuiSecurityTab(this, tileEntity, MekanismUtils.getResource(ResourceType.GUI, "GuiWindTurbine.png")));
		guiElements.add(new GuiEnergyInfo(new IInfoHandler()
		{
			@Override
			public List<String> getInfo()
			{
				return ListUtils.asList(
						LangUtils.localize("gui.producing") + ": " + MekanismUtils.getEnergyDisplay(tileEntity.isActive ? generators.windGenerationMin*tileEntity.currentMultiplier : 0) + "/t",
						LangUtils.localize("gui.maxOutput") + ": " + MekanismUtils.getEnergyDisplay(tileEntity.getMaxOutput()) + "/t");
			}
		}, this, MekanismUtils.getResource(ResourceType.GUI, "GuiWindTurbine.png")));
		guiElements.add(new GuiPowerBar(this, tileEntity, MekanismUtils.getResource(ResourceType.GUI, "GuiWindTurbine.png"), 164, 15));
		guiElements.add(new GuiSlot(SlotType.NORMAL, this, MekanismUtils.getResource(ResourceType.GUI, "GuiWindTurbine.png"), 142, 34).with(SlotOverlay.POWER));
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY)
	{
		super.drawGuiContainerForegroundLayer(mouseX, mouseY);

		fontRendererObj.drawString(tileEntity.getInventoryName(), 45, 6, 0x404040);
		fontRendererObj.drawString(LangUtils.localize("container.inventory"), 8, (ySize - 96) + 2, 0x404040);
		fontRendererObj.drawString(MekanismUtils.getEnergyDisplay(tileEntity.getEnergy()), 51, 26, 0x00CD00);
		fontRendererObj.drawString(LangUtils.localize("gui.power") + ": " + powerFormat.format(MekanismUtils.convertToDisplay(generators.windGenerationMin*tileEntity.currentMultiplier)), 51, 35, 0x00CD00);
		fontRendererObj.drawString(LangUtils.localize("gui.out") + ": " + MekanismUtils.getEnergyDisplay(tileEntity.getMaxOutput()) + "/t", 51, 44, 0x00CD00);

		int size = 44;

		if(!tileEntity.getActive())
		{
			size += 9;
			fontRendererObj.drawString(EnumColor.DARK_RED + LangUtils.localize("gui.skyBlocked"), 51, size, 0x00CD00);
		}
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTick, int mouseX, int mouseY)
	{
		mc.renderEngine.bindTexture(MekanismUtils.getResource(ResourceType.GUI, "GuiWindTurbine.png"));
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		int guiWidth = (width - xSize) / 2;
		int guiHeight = (height - ySize) / 2;
		drawTexturedModalRect(guiWidth, guiHeight, 0, 0, xSize, ySize);

		drawTexturedModalRect(guiWidth + 20, guiHeight + 37, 176, (tileEntity.getActive() ? 52 : 64), 12, 12);

		super.drawGuiContainerBackgroundLayer(partialTick, mouseX, mouseY);
	}
}
