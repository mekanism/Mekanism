package mekanism.client.gui;

import mekanism.api.Coord4D;
import mekanism.client.sound.SoundHandler;
import mekanism.common.IUpgradeTile;
import mekanism.common.Mekanism;
import mekanism.common.network.PacketRemoveUpgrade.RemoveUpgradeMessage;
import mekanism.common.network.PacketSimpleGui.SimpleGuiMessage;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import codechicken.lib.vec.Rectangle4i;

public class GuiUpgradeTab extends GuiElement
{
	TileEntity tileEntity;

	public GuiUpgradeTab(IGuiWrapper gui, TileEntity tile, ResourceLocation def)
	{
		super(MekanismUtils.getResource(ResourceType.GUI_ELEMENT, "GuiUpgradeTab.png"), gui, def);

		tileEntity = tile;
	}
	
	@Override
	public Rectangle4i getBounds(int guiWidth, int guiHeight)
	{
		return new Rectangle4i(guiWidth + 176, guiHeight + 6, 26, 26);
	}

	@Override
	public void renderBackground(int xAxis, int yAxis, int guiWidth, int guiHeight)
	{
		mc.renderEngine.bindTexture(RESOURCE);

		guiObj.drawTexturedRect(guiWidth + 176, guiHeight + 6, 0, 0, 26, 63);

		IUpgradeTile upgradeTile = (IUpgradeTile)tileEntity;
		int displayInt = upgradeTile.getComponent().getScaledUpgradeProgress(14);

		guiObj.drawTexturedRect(guiWidth + 180, guiHeight + 30, 26, 0, 10, displayInt);

		mc.renderEngine.bindTexture(defaultLocation);
	}

	@Override
	public void renderForeground(int xAxis, int yAxis)
	{
		mc.renderEngine.bindTexture(RESOURCE);

		IUpgradeTile upgradeTile = (IUpgradeTile)tileEntity;

		if(xAxis >= 179 && xAxis <= 197 && yAxis >= 10 && yAxis <= 28)
		{
			displayTooltip(MekanismUtils.localize("gui.upgrades"), xAxis, yAxis);
		}


		mc.renderEngine.bindTexture(defaultLocation);
	}

	@Override
	public void preMouseClicked(int xAxis, int yAxis, int button) {}

	@Override
	public void mouseClicked(int xAxis, int yAxis, int button)
	{
		if(xAxis >= 179 && xAxis <= 197 && yAxis >= 10 && yAxis <= 28)
		{
			Mekanism.packetHandler.sendToServer(new SimpleGuiMessage(Coord4D.get(tileEntity), 9));
            SoundHandler.playSound("gui.button.press");
		}
	}
}
