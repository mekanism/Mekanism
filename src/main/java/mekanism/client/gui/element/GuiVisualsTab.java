package mekanism.client.gui.element;

import mekanism.client.gui.IGuiWrapper;
import mekanism.client.sound.SoundHandler;
import mekanism.common.tile.TileEntityDigitalMiner;
import mekanism.common.util.LangUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.util.ResourceLocation;
import codechicken.lib.vec.Rectangle4i;

public class GuiVisualsTab extends GuiElement
{
	private TileEntityDigitalMiner tileEntity;

	public GuiVisualsTab(IGuiWrapper gui, TileEntityDigitalMiner tile, ResourceLocation def)
	{
		super(MekanismUtils.getResource(ResourceType.GUI_ELEMENT, "GuiVisualsTab.png"), gui, def);

		lmntLeft = -26;
		lmntTop = 5;

		tileEntity = tile;
	}

	public GuiVisualsTab(IGuiWrapper gui, TileEntityDigitalMiner tile, ResourceLocation def, int guiLeft, int guiTop)
	{
		super(MekanismUtils.getResource(ResourceType.GUI_ELEMENT, "GuiVisualsTab.png"), gui, def);

		lmntLeft = guiLeft;
		lmntTop = guiTop;

		tileEntity = tile;
	}

	@Override
	public void renderBackground(int xAxis, int yAxis, int guiLeft, int guiTop)
	{
		mc.renderEngine.bindTexture(RESOURCE);

		guiObj.drawTexturedRect(guiLeft + lmntLeft, guiTop + lmntTop, 0, 0, lmntWidth, lmntHeight);

		boolean mouseOver = xAxis >= lmntLeft + 5 && xAxis <= lmntLeft + 23 && yAxis >= lmntTop + 4 && yAxis <= lmntTop + 22;
		guiObj.drawTexturedRect(guiLeft + lmntLeft + 5, guiTop + lmntTop + 4, 26, mouseOver?  0: 18, 18, 18);

		mc.renderEngine.bindTexture(defaultLocation);
	}

	@Override
	public void renderForeground(int xAxis, int yAxis)
	{
		mc.renderEngine.bindTexture(RESOURCE);

		if(xAxis >= lmntLeft + 5 && xAxis <= lmntLeft + 23 && yAxis >= lmntTop + 4 && yAxis <= lmntTop + 22)
		{
			displayTooltip(LangUtils.localize("gui.visuals") + ": " + LangUtils.transOnOff(tileEntity.clientRendering), xAxis, yAxis);
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
			if(xAxis >= lmntLeft + 5 && xAxis <= lmntLeft + 23 && yAxis >= lmntTop + 4 && yAxis <= lmntTop + 22)
			{
				tileEntity.clientRendering = !tileEntity.clientRendering;
	            SoundHandler.playSound("gui.button.press");
			}
		}
	}
}
