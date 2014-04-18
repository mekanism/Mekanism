package mekanism.client.gui;

import java.util.List;

import mekanism.api.ListUtils;
import mekanism.client.gui.GuiEnergyInfo.IInfoHandler;
import mekanism.client.gui.GuiProgress.IProgressInfoHandler;
import mekanism.client.gui.GuiSlot.SlotOverlay;
import mekanism.client.gui.GuiSlot.SlotType;
import mekanism.common.inventory.container.ContainerChanceMachine;
import mekanism.common.tile.TileEntityChanceMachine;
import mekanism.common.util.MekanismUtils;
import net.minecraft.entity.player.InventoryPlayer;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiChanceMachine extends GuiMekanism
{
	public TileEntityChanceMachine tileEntity;

	public GuiChanceMachine(InventoryPlayer inventory, TileEntityChanceMachine tentity)
	{
		super(tentity, new ContainerChanceMachine(inventory, tentity));
		tileEntity = tentity;

		guiElements.add(new GuiRedstoneControl(this, tileEntity, tileEntity.guiLocation));
		guiElements.add(new GuiUpgradeManagement(this, tileEntity, tileEntity.guiLocation));
		guiElements.add(new GuiConfigurationTab(this, tileEntity, tileEntity.guiLocation));
		guiElements.add(new GuiPowerBar(this, tileEntity, tileEntity.guiLocation, 164, 15));
		guiElements.add(new GuiEnergyInfo(new IInfoHandler() {
			@Override
			public List<String> getInfo()
			{
				String multiplier = MekanismUtils.getEnergyDisplay(MekanismUtils.getEnergyPerTick(tileEntity.getSpeedMultiplier(), tileEntity.getEnergyMultiplier(), tileEntity.ENERGY_PER_TICK));
				return ListUtils.asList("Using: " + multiplier + "/t", "Needed: " + MekanismUtils.getEnergyDisplay(tileEntity.getMaxEnergy()-tileEntity.getEnergy()));
			}
		}, this, tileEntity, tileEntity.guiLocation));

		guiElements.add(new GuiSlot(SlotType.INPUT, this, tileEntity, tileEntity.guiLocation, 55, 16));
		guiElements.add(new GuiSlot(SlotType.POWER, this, tileEntity, tileEntity.guiLocation, 55, 52).with(SlotOverlay.POWER));
		guiElements.add(new GuiSlot(SlotType.OUTPUT_WIDE, this, tileEntity, tileEntity.guiLocation, 111, 30));

		guiElements.add(new GuiProgress(new IProgressInfoHandler()
		{
			@Override
			public double getProgress()
			{
				return tileEntity.getScaledProgress();
			}
		}, tileEntity.getProgressType(), this, tileEntity, tileEntity.guiLocation, 77, 37));
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY)
	{
		fontRenderer.drawString(tileEntity.getInvName(), 45, 6, 0x404040);
		fontRenderer.drawString(MekanismUtils.localize("container.inventory"), 8, (ySize - 96) + 2, 0x404040);

		super.drawGuiContainerForegroundLayer(mouseX, mouseY);
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTick, int mouseX, int mouseY)
	{
		mc.renderEngine.bindTexture(tileEntity.guiLocation);
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		int guiWidth = (width - xSize) / 2;
		int guiHeight = (height - ySize) / 2;
		drawTexturedModalRect(guiWidth, guiHeight, 0, 0, xSize, ySize);

		super.drawGuiContainerBackgroundLayer(partialTick, mouseX, mouseY);
	}
}
