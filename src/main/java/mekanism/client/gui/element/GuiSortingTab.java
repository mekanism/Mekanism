package mekanism.client.gui.element;

import java.util.ArrayList;

import mekanism.api.Coord4D;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.sound.SoundHandler;
import mekanism.common.Mekanism;
import mekanism.common.network.PacketTileEntity.TileEntityMessage;
import mekanism.common.tile.TileEntityFactory;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.util.ResourceLocation;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import codechicken.lib.vec.Rectangle4i;

@SideOnly(Side.CLIENT)
public class GuiSortingTab extends GuiElement
{
	TileEntityFactory tileEntity;

	public GuiSortingTab(IGuiWrapper gui, TileEntityFactory tile, ResourceLocation def)
	{
		super(MekanismUtils.getResource(ResourceType.GUI_ELEMENT, "GuiSortingTab.png"), gui, def);

		tileEntity = tile;
	}
	
	@Override
	public Rectangle4i getBounds(int guiWidth, int guiHeight)
	{
		return new Rectangle4i(guiWidth - 26, guiHeight + 34, 26, 35);
	}

	@Override
	public void renderBackground(int xAxis, int yAxis, int guiWidth, int guiHeight)
	{
		mc.renderEngine.bindTexture(RESOURCE);

		guiObj.drawTexturedRect(guiWidth - 26, guiHeight + 34, 0, 0, 26, 35);

		if(xAxis >= -21 && xAxis <= -3 && yAxis >= 38 && yAxis <= 56)
		{
			guiObj.drawTexturedRect(guiWidth - 21, guiHeight + 38, 26, 0, 18, 18);
		}
		else {
			guiObj.drawTexturedRect(guiWidth - 21, guiHeight + 38, 26, 18, 18, 18);
		}

		mc.renderEngine.bindTexture(defaultLocation);
	}

	@Override
	public void renderForeground(int xAxis, int yAxis)
	{
		mc.renderEngine.bindTexture(RESOURCE);

		getFontRenderer().drawString(((TileEntityFactory)tileEntity).sorting ? "On" : "Off", -21, 58, 0x0404040);

		if(xAxis >= -21 && xAxis <= -3 && yAxis >= 38 && yAxis <= 56)
		{
			displayTooltip(MekanismUtils.localize("gui.factory.autoSort"), xAxis, yAxis);
		}

		mc.renderEngine.bindTexture(defaultLocation);
	}

	@Override
	public void preMouseClicked(int xAxis, int yAxis, int button) {}

	@Override
	public void mouseClicked(int xAxis, int yAxis, int button)
	{
		if(button == 0)
		{
			if(xAxis >= -21 && xAxis <= -3 && yAxis >= 38 && yAxis <= 56)
			{
				ArrayList data = new ArrayList();
				data.add(0);
				Mekanism.packetHandler.sendToServer(new TileEntityMessage(Coord4D.get(tileEntity), data));
                SoundHandler.playSound("gui.button.press");
			}
		}
	}
}
