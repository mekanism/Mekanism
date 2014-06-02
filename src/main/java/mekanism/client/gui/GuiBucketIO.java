package mekanism.client.gui;

import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.util.ResourceLocation;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiBucketIO extends GuiElement
{
	public GuiBucketIO(IGuiWrapper gui, ResourceLocation def)
	{
		super(MekanismUtils.getResource(ResourceType.GUI_ELEMENT, "GuiBucket.png"), gui, def);
	}

	@Override
	public void renderBackground(int xAxis, int yAxis, int guiWidth, int guiHeight)
	{
		mc.renderEngine.bindTexture(RESOURCE);

		guiObj.drawTexturedRect(guiWidth - 26, guiHeight + 4, 0, 0, 26, 57);

		mc.renderEngine.bindTexture(defaultLocation);
	}

	@Override
	public void renderForeground(int xAxis, int yAxis)
	{
		mc.renderEngine.bindTexture(RESOURCE);

		mc.renderEngine.bindTexture(defaultLocation);
	}

	@Override
	public void preMouseClicked(int xAxis, int yAxis, int button) {}

	@Override
	public void mouseClicked(int xAxis, int yAxis, int button) {}
}
