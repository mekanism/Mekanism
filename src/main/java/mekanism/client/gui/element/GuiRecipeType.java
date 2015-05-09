package mekanism.client.gui.element;

import mekanism.client.gui.IGuiWrapper;
import mekanism.common.tile.TileEntityFactory;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.util.ResourceLocation;
import codechicken.lib.vec.Rectangle4i;

public class GuiRecipeType extends GuiElement
{
	public TileEntityFactory tileEntity;

	public GuiRecipeType(IGuiWrapper gui, TileEntityFactory tile, ResourceLocation def)
	{
		super(MekanismUtils.getResource(ResourceType.GUI_ELEMENT, "GuiRecipeType.png"), gui, def);

		lmntLeft = 176;
		lmntTop = 70;

		tileEntity = tile;
	}

	public GuiRecipeType(IGuiWrapper gui, TileEntityFactory tile, ResourceLocation def, int guiLeft, int guiTop )
	{
		super(MekanismUtils.getResource(ResourceType.GUI_ELEMENT, "GuiRecipeType.png"), gui, def);

		lmntLeft = guiLeft;
		lmntTop = guiTop;

		tileEntity = tile;
	}

	@Override
	public void renderBackground(int xAxis, int yAxis, int guiLeft, int guiTop)
	{
		mc.renderEngine.bindTexture(RESOURCE);

		guiObj.drawTexturedRect(guiLeft + lmntLeft, guiTop + lmntTop, 0, 0, 26, 63);

		TileEntityFactory factory = tileEntity;
		int displayInt = factory.getScaledRecipeProgress(15);

		guiObj.drawTexturedRect(guiLeft + 181, guiTop + 94, 26, 0, 10, displayInt);

		mc.renderEngine.bindTexture(defaultLocation);
	}

	@Override
	public void renderForeground(int xAxis, int yAxis) {}

	@Override
	public void preMouseClicked(int xAxis, int yAxis, int button)
	{
		if(button == 0)
		{
			if(xAxis >= 180 && xAxis <= 196 && yAxis >= 75 && yAxis <= 91)
			{
				offsetX(26);
			}
			else if(xAxis >= 180 && xAxis <= 196 && yAxis >= 112 && yAxis <= 128)
			{
				offsetX(26);
			}
		}
	}

	@Override
	public void mouseClicked(int xAxis, int yAxis, int button)
	{
		if(button == 0)
		{
			if(xAxis >= 180 && xAxis <= 196 && yAxis >= 75 && yAxis <= 91)
			{
				offsetX(-26);
			}
			else if(xAxis >= 180 && xAxis <= 196 && yAxis >= 112 && yAxis <= 128)
			{
				offsetX(-26);
			}
		}
	}
}
