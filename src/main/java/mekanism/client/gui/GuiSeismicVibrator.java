package mekanism.client.gui;

import mekanism.client.gui.element.GuiEnergyInfo;
import mekanism.client.gui.element.GuiPowerBar;
import mekanism.client.gui.element.GuiRedstoneControl;
import mekanism.client.gui.element.GuiSecurityTab;
import mekanism.client.gui.element.GuiSlot;
import mekanism.client.gui.element.GuiSlot.SlotOverlay;
import mekanism.client.gui.element.GuiSlot.SlotType;
import mekanism.common.config.MekanismConfig;
import mekanism.common.inventory.container.ContainerSeismicVibrator;
import mekanism.common.tile.TileEntitySeismicVibrator;
import mekanism.common.util.LangUtils;
import mekanism.common.util.ListUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.lwjgl.opengl.GL11;

@SideOnly(Side.CLIENT)
public class GuiSeismicVibrator extends GuiMekanism
{
	public TileEntitySeismicVibrator tileEntity;

	public GuiSeismicVibrator(InventoryPlayer inventory, TileEntitySeismicVibrator tentity)
	{
		super(tentity, new ContainerSeismicVibrator(inventory, tentity));
		tileEntity = tentity;

		guiElements.add(new GuiSecurityTab(this, tileEntity, MekanismUtils.getResource(ResourceType.GUI, "GuiSeismicVibrator.png")));
		guiElements.add(new GuiRedstoneControl(this, tileEntity, MekanismUtils.getResource(ResourceType.GUI, "GuiSeismicVibrator.png")));
		guiElements.add(new GuiPowerBar(this, tileEntity, MekanismUtils.getResource(ResourceType.GUI, "GuiSeismicVibrator.png"), 164, 15));
		guiElements.add(new GuiEnergyInfo(() ->
        {
            String multiplier = MekanismUtils.getEnergyDisplay(MekanismConfig.current().usage.seismicVibratorUsage.val());
            return ListUtils.asList(LangUtils.localize("gui.using") + ": " + multiplier + "/t", LangUtils.localize("gui.needed") + ": " + MekanismUtils.getEnergyDisplay(tileEntity.getMaxEnergy()-tileEntity.getEnergy()));
        }, this, MekanismUtils.getResource(ResourceType.GUI, "GuiSeismicVibrator.png")));

		guiElements.add(new GuiSlot(SlotType.NORMAL, this, MekanismUtils.getResource(ResourceType.GUI, "GuiSeismicVibrator.png"), 142, 34).with(SlotOverlay.POWER));
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY)
	{
		int xAxis = (mouseX - (width - xSize) / 2);
		int yAxis = (mouseY - (height - ySize) / 2);

		fontRenderer.drawString(tileEntity.getName(), 45, 6, 0x404040);
		fontRenderer.drawString(LangUtils.localize("container.inventory"), 8, (ySize - 96) + 2, 0x404040);
		
		fontRenderer.drawString(tileEntity.isActive ? LangUtils.localize("gui.vibrating") : LangUtils.localize("gui.idle"), 19, 26, 0x00CD00);
		fontRenderer.drawString(LangUtils.localize("gui.chunk") + ": " + (tileEntity.getPos().getX() >> 4) + ", " + (tileEntity.getPos().getZ() >> 4), 19, 35, 0x00CD00);

		super.drawGuiContainerForegroundLayer(mouseX, mouseY);
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTick, int mouseX, int mouseY)
	{
		mc.renderEngine.bindTexture(MekanismUtils.getResource(ResourceType.GUI, "GuiSeismicVibrator.png"));
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		int guiWidth = (width - xSize) / 2;
		int guiHeight = (height - ySize) / 2;
		drawTexturedModalRect(guiWidth, guiHeight, 0, 0, xSize, ySize);

		int xAxis = mouseX - guiWidth;
		int yAxis = mouseY - guiHeight;

		super.drawGuiContainerBackgroundLayer(partialTick, mouseX, mouseY);
	}
}
