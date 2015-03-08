package mekanism.client.gui.element;

import java.util.List;

import mekanism.client.gui.IGuiWrapper;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.util.ResourceLocation;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import codechicken.lib.vec.Rectangle4i;

@SideOnly(Side.CLIENT)
public class GuiEnergyInfo extends GuiElement
{
	public IInfoHandler infoHandler;

	public GuiEnergyInfo(IInfoHandler handler, IGuiWrapper gui, ResourceLocation def)
	{
		super(MekanismUtils.getResource(ResourceType.GUI_ELEMENT, "GuiEnergyInfo.png"), gui, def);

		infoHandler = handler;
	}
	
	@Override
	public Rectangle4i getBounds(int guiWidth, int guiHeight)
	{
		return new Rectangle4i(guiWidth - 26, guiHeight + 138, 26, 26);
	}

	public static interface IInfoHandler
	{
		public List<String> getInfo();
	}

	@Override
	public void renderBackground(int xAxis, int yAxis, int guiWidth, int guiHeight)
	{
		mc.renderEngine.bindTexture(RESOURCE);

		guiObj.drawTexturedRect(guiWidth - 26, guiHeight + 138, 0, 0, 26, 26);

		mc.renderEngine.bindTexture(defaultLocation);
	}

	@Override
	public void renderForeground(int xAxis, int yAxis)
	{
		if(xAxis >= -21 && xAxis <= -3 && yAxis >= 142 && yAxis <= 160)
		{
			displayTooltips(infoHandler.getInfo(), xAxis, yAxis);
		}
	}

	@Override
	public void preMouseClicked(int xAxis, int yAxis, int button) {}

	@Override
	public void mouseClicked(int xAxis, int yAxis, int button) {}
}
