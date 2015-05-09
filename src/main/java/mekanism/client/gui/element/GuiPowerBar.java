package mekanism.client.gui.element;

import mekanism.client.gui.IGuiWrapper;
import mekanism.common.tile.TileEntityElectricBlock;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.util.ResourceLocation;
import codechicken.lib.vec.Rectangle4i;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiPowerBar extends GuiElement
{
	private int xLocation;
	private int yLocation;

	private int width = 6;
	private int height = 56;
	private int innerOffsetY = 2;

	private TileEntityElectricBlock tileEntity;
	private IPowerInfoHandler handler;

	public GuiPowerBar(IGuiWrapper gui, TileEntityElectricBlock tile, ResourceLocation def, int x, int y)
	{
		super(MekanismUtils.getResource(ResourceType.GUI_ELEMENT, "GuiPowerBar.png"), gui, def);
		
		tileEntity = tile;
		
		handler = new IPowerInfoHandler() {
			@Override
			public String getTooltip()
			{
				return MekanismUtils.getEnergyDisplay(tileEntity.getEnergy());
			}
			
			@Override
			public double getLevel()
			{
				return tileEntity.getEnergy()/tileEntity.getMaxEnergy();
			}
		};
		
		xLocation = x;
		yLocation = y;
	}
	
	public GuiPowerBar(IGuiWrapper gui, IPowerInfoHandler h, ResourceLocation def, int x, int y)
	{
		super(MekanismUtils.getResource(ResourceType.GUI_ELEMENT, "GuiPowerBar.png"), gui, def);
		
		handler = h;
		
		xLocation = x;
		yLocation = y;
	}
	
	@Override
	public Rectangle4i getBounds(int guiWidth, int guiHeight)
	{
		return new Rectangle4i(guiWidth + xLocation, guiHeight + yLocation, width, height);
	}
	
	public static abstract class IPowerInfoHandler
	{
		public String getTooltip()
		{
			return null;
		}
		
		public abstract double getLevel();
	}

	@Override
	public void renderBackground(int xAxis, int yAxis, int guiWidth, int guiHeight)
	{
		mc.renderEngine.bindTexture(RESOURCE);

		guiObj.drawTexturedRect(guiWidth + xLocation, guiHeight + yLocation, 0, 0, width, height);
		
		if(handler.getLevel() > 0)
		{
			int displayInt = (int)(handler.getLevel()*52) + innerOffsetY;
			guiObj.drawTexturedRect(guiWidth + xLocation, guiHeight + yLocation + height - displayInt, 6, height - displayInt, width, displayInt);
		}

		mc.renderEngine.bindTexture(defaultLocation);
	}

	@Override
	public void renderForeground(int xAxis, int yAxis)
	{
		mc.renderEngine.bindTexture(RESOURCE);

		if(handler.getTooltip() != null && xAxis >= xLocation && xAxis <= xLocation + width && yAxis >= yLocation && yAxis <= yLocation + height)
		{
			displayTooltip(handler.getTooltip(), xAxis, yAxis);
		}

		mc.renderEngine.bindTexture(defaultLocation);
	}

	@Override
	public void preMouseClicked(int xAxis, int yAxis, int button) {}

	@Override
	public void mouseClicked(int xAxis, int yAxis, int button) {}
}
