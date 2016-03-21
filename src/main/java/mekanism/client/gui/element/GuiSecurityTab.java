package mekanism.client.gui.element;

import mekanism.api.Coord4D;
import mekanism.api.EnumColor;
import mekanism.api.util.ListUtils;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.sound.SoundHandler;
import mekanism.common.Mekanism;
import mekanism.common.network.PacketSecurityMode.SecurityModeMessage;
import mekanism.common.security.ISecurity;
import mekanism.common.security.ISecurity.SecurityMode;
import mekanism.common.util.LangUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import mekanism.common.util.SecurityUtils;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import codechicken.lib.vec.Rectangle4i;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiSecurityTab extends GuiElement
{
	TileEntity tileEntity;

	public GuiSecurityTab(IGuiWrapper gui, TileEntity tile, ResourceLocation def)
	{
		super(MekanismUtils.getResource(ResourceType.GUI_ELEMENT, "GuiSecurityTab.png"), gui, def);

		tileEntity = tile;
	}
	
	@Override
	public Rectangle4i getBounds(int guiWidth, int guiHeight)
	{
		return new Rectangle4i(guiWidth + 176, guiHeight + 32, 26, 26);
	}

	@Override
	public void renderBackground(int xAxis, int yAxis, int guiWidth, int guiHeight)
	{
		mc.renderEngine.bindTexture(RESOURCE);

		guiObj.drawTexturedRect(guiWidth + 176, guiHeight + 32, 0, 0, 26, 26);

		ISecurity tile = (ISecurity)tileEntity;
		int renderX = 26 + (18*tile.getSecurity().getMode().ordinal());

		if(tile.getSecurity().getOwner() != null && tile.getSecurity().getOwner().equals(mc.thePlayer.getCommandSenderName()))
		{
			if(xAxis >= 179 && xAxis <= 197 && yAxis >= 36 && yAxis <= 54)
			{
				guiObj.drawTexturedRect(guiWidth + 179, guiHeight + 36, renderX, 0, 18, 18);
			}
			else {
				guiObj.drawTexturedRect(guiWidth + 179, guiHeight + 36, renderX, 18, 18, 18);
			}
		}
		else {
			guiObj.drawTexturedRect(guiWidth + 179, guiHeight + 36, renderX, 36, 18, 18);
		}

		mc.renderEngine.bindTexture(defaultLocation);
	}

	@Override
	public void renderForeground(int xAxis, int yAxis)
	{
		mc.renderEngine.bindTexture(RESOURCE);

		ISecurity control = (ISecurity)tileEntity;

		if(xAxis >= 179 && xAxis <= 197 && yAxis >= 36 && yAxis <= 54)
		{
			String securityText = EnumColor.GREY + LangUtils.localize("gui.security") + ": " + control.getSecurity().getMode().getDisplay();
			String ownerText = SecurityUtils.getOwnerDisplay(mc.thePlayer.getCommandSenderName(), control.getSecurity().getOwner());
			
			displayTooltips(ListUtils.asList(securityText, ownerText), xAxis, yAxis);
		}

		mc.renderEngine.bindTexture(defaultLocation);
	}

	@Override
	public void preMouseClicked(int xAxis, int yAxis, int button) {}

	@Override
	public void mouseClicked(int xAxis, int yAxis, int button)
	{
		ISecurity control = (ISecurity)tileEntity;

		if(button == 0)
		{
			if(control.getSecurity().getOwner() != null && mc.thePlayer.getCommandSenderName().equals(control.getSecurity().getOwner()))
			{
				if(xAxis >= 179 && xAxis <= 197 && yAxis >= 36 && yAxis <= 54)
				{
					SecurityMode current = control.getSecurity().getMode();
					int ordinalToSet = current.ordinal() < (SecurityMode.values().length-1) ? current.ordinal()+1 : 0;
	
					SoundHandler.playSound("gui.button.press");
					Mekanism.packetHandler.sendToServer(new SecurityModeMessage(Coord4D.get(tileEntity), SecurityMode.values()[ordinalToSet]));
				}
			}
		}
	}
}
