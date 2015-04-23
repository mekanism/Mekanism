package mekanism.client.gui.element;

import java.util.List;

import mekanism.client.gui.IGuiWrapper;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.util.ResourceLocation;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiEnergyInfo extends GuiElement
{
	public IInfoHandler infoHandler;

	public GuiEnergyInfo(IInfoHandler handler, IGuiWrapper gui, ResourceLocation def)
	{
		super(MekanismUtils.getResource(ResourceType.GUI_ELEMENT, "GuiEnergyInfo.png"), gui, def);

		lmntLeft = -26;
		lmntTop = 138;

		infoHandler = handler;
	}

	public GuiEnergyInfo(IInfoHandler handler, IGuiWrapper gui, ResourceLocation def, int guiLeft, int guiTop)
	{
		super(MekanismUtils.getResource(ResourceType.GUI_ELEMENT, "GuiEnergyInfo.png"), gui, def);

		lmntLeft = guiLeft;
		lmntTop = guiTop;

		infoHandler = handler;
	}

	public static interface IInfoHandler
	{
		public List<String> getInfo();
	}

	@Override
	public void renderBackground(int xAxis, int yAxis, int guiLeft, int guiTop)
	{
		mc.renderEngine.bindTexture(RESOURCE);

		guiObj.drawTexturedRect(guiLeft + lmntLeft, guiTop + lmntTop, 0, 0, lmntWidth, lmntHeight);

		mc.renderEngine.bindTexture(defaultLocation);
	}

	@Override
	public void renderForeground(int xAxis, int yAxis)
	{
		if(xAxis >= lmntLeft + 5 && xAxis <= lmntLeft + 23 && yAxis >= lmntTop + 4 && yAxis <= lmntTop + 22)
		{
			displayTooltips(infoHandler.getInfo(), xAxis, yAxis);
		}
	}

	@Override
	public void preMouseClicked(int xAxis, int yAxis, int button) {}

	@Override
	public void mouseClicked(int xAxis, int yAxis, int button) {}
}
