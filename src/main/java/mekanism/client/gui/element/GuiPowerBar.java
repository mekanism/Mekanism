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
	private int innerOffsetY = 2;

	private TileEntityElectricBlock tileEntity;
	private IPowerInfoHandler handler;

	public GuiPowerBar(IGuiWrapper gui, TileEntityElectricBlock tile, ResourceLocation def, int guiLeft, int guiTop)
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
		
		lmntLeft = guiLeft;
		lmntTop = guiTop;
		lmntWidth = 6;
		lmntHeight = 54;
	}
	
	public GuiPowerBar(IGuiWrapper gui, IPowerInfoHandler h, ResourceLocation def, int guiLeft, int guiTop)
	{
		super(MekanismUtils.getResource(ResourceType.GUI_ELEMENT, "GuiPowerBar.png"), gui, def);
		
		handler = h;
		
		lmntLeft = guiLeft;
		lmntTop = guiTop;
		lmntWidth = 6;
		lmntHeight = 54;
	}
	
	@Override
	public Rectangle4i getBounds(int guiLeft, int guiTop)
	{
		return new Rectangle4i(guiLeft + lmntLeft, guiTop + lmntTop, lmntWidth, lmntHeight);
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
	public void renderBackground(int xAxis, int yAxis, int guiLeft, int guiTop)
	{
		mc.renderEngine.bindTexture(RESOURCE);

		guiObj.drawTexturedRect(guiLeft + lmntLeft, guiTop + lmntTop, 0, 0, lmntWidth, lmntHeight);
		
		if(handler.getLevel() > 0)
		{
			int barX = lmntLeft + 1;
			int barH = (int)(handler.getLevel()*52);
			int barY = lmntTop + 1 + 52 - barH;
			int barV = (int)((1.0 - handler.getLevel())*104);
			guiObj.drawTexturedRect(guiLeft + barX, guiTop + barY, 6, barV, 4, barH);
		}

		mc.renderEngine.bindTexture(defaultLocation);
	}

	@Override
	public void renderForeground(int xAxis, int yAxis)
	{
		mc.renderEngine.bindTexture(RESOURCE);

		if(handler.getTooltip() != null && xAxis >= lmntLeft && xAxis <= lmntLeft + lmntWidth && yAxis >= lmntTop && yAxis <= lmntTop + lmntHeight)
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
