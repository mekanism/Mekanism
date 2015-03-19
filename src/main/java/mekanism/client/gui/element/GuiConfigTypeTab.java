package mekanism.client.gui.element;

import mekanism.api.transmitters.TransmissionType;
import mekanism.client.gui.GuiSideConfiguration;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.sound.SoundHandler;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import codechicken.lib.vec.Rectangle4i;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiConfigTypeTab extends GuiElement
{
	private TileEntity tileEntity;
	public TransmissionType transmission;
	
	private int yPos;
	
	public boolean visible;
	public boolean left;

	public GuiConfigTypeTab(IGuiWrapper gui, TileEntity tile, TransmissionType type, ResourceLocation def)
	{
		super(getResource(type), gui, def);

		tileEntity = tile;
		transmission = type;
	}
	
	private static ResourceLocation getResource(TransmissionType t)
	{
		return MekanismUtils.getResource(ResourceType.GUI_ELEMENT, "Gui" + t.getTransmission() + "Tab.png");
	}
	
	public void setY(int y)
	{
		yPos = y;
	}

	@Override
	public Rectangle4i getBounds(int guiWidth, int guiHeight)
	{
		return new Rectangle4i(guiWidth + getLeftBound(false)-4, guiHeight + yPos, 26, 26);
	}

	@Override
	public void renderBackground(int xAxis, int yAxis, int guiWidth, int guiHeight)
	{
		if(!visible)
		{
			return;
		}
		
		mc.renderEngine.bindTexture(RESOURCE);

		guiObj.drawTexturedRect(guiWidth + getLeftBound(false)-4, guiHeight + yPos, 0, left ? 0 : 26, 26, 26);

		if(xAxis >= getLeftBound(true) && xAxis <= getRightBound(true) && yAxis >= yPos+4 && yAxis <= yPos+22)
		{
			guiObj.drawTexturedRect(guiWidth + getLeftBound(true), guiHeight + yPos+4, 26, 0, 18, 18);
		}
		else {
			guiObj.drawTexturedRect(guiWidth + getLeftBound(true), guiHeight + yPos+4, 26, 18, 18, 18);
		}

		mc.renderEngine.bindTexture(defaultLocation);
	}

	@Override
	public void renderForeground(int xAxis, int yAxis)
	{
		if(!visible)
		{
			return;
		}
		
		mc.renderEngine.bindTexture(RESOURCE);

		if(xAxis >= getLeftBound(true) && xAxis <= getRightBound(true) && yAxis >= yPos+4 && yAxis <= yPos+22)
		{
			displayTooltip(transmission.localize(), xAxis, yAxis);
		}

		mc.renderEngine.bindTexture(defaultLocation);
	}
	
	public int getLeftBound(boolean adjust)
	{
		return left ? -21+(adjust ? 1 : 0) : 179-(adjust ? 1 : 0);
	}
	
	public int getRightBound(boolean adjust)
	{
		return left ? -3+(adjust ? 1 : 0) : 197-(adjust ? 1 : 0);
	}

	@Override
	public void preMouseClicked(int xAxis, int yAxis, int button) {}

	@Override
	public void mouseClicked(int xAxis, int yAxis, int button)
	{
		if(!visible)
		{
			return;
		}
		
		if(button == 0)
		{
			if(xAxis >= getLeftBound(true) && xAxis <= getRightBound(true) && yAxis >= yPos+4 && yAxis <= yPos+22)
			{
				((GuiSideConfiguration)guiObj).currentType = transmission;
				((GuiSideConfiguration)guiObj).updateTabs();
				SoundHandler.playSound("gui.button.press");
			}
		}
	}
}
