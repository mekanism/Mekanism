package mekanism.client.gui;

import java.util.ArrayList;
import java.util.List;

import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

import codechicken.lib.vec.Rectangle4i;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiScrollList extends GuiElement
{
	public int xSize;
	public int size;
	
	public int xPosition;
	public int yPosition;
	
	public List<String> textEntries = new ArrayList<String>();
	
	public int scrollIndex;
	
	public GuiScrollList(IGuiWrapper gui, ResourceLocation def, int x, int y, int sizeX, int sizeY)
	{
		super(MekanismUtils.getResource(ResourceType.GUI_ELEMENT, "GuiScrollList.png"), gui, def);
		
		xPosition = x;
		yPosition = y;
		
		xSize = sizeX;
		size = sizeY;
	}
	
	public void setText(List<String> text)
	{
		if(text == null)
		{
			textEntries.clear();
			return;
		}
		
		textEntries = text;
	}
	
	@Override
	public Rectangle4i getBounds(int guiWidth, int guiHeight)
	{
		return new Rectangle4i(guiWidth - 26, guiHeight + 6, 26, 26);
	}

	@Override
	public void renderBackground(int xAxis, int yAxis, int guiWidth, int guiHeight)
	{
		mc.renderEngine.bindTexture(RESOURCE);
		
		drawBlack(guiWidth, guiHeight);

		mc.renderEngine.bindTexture(defaultLocation);
	}
	
	public void drawBlack(int guiWidth, int guiHeight)
	{
		int xDisplays = xSize/10 + (xSize%10 > 0 ? 1 : 0);
		
		for(int yIter = 0; yIter < size; yIter++)
		{
			for(int xIter = 0; xIter < xDisplays; xIter++)
			{
				int width = (xSize%10 > 0 && xIter == xDisplays ? xSize%10 : 10);
				guiObj.drawTexturedRect(guiWidth + xPosition + (xIter*10), guiHeight + yPosition + (yIter*10), 0, 0, width, 10);
			}
		}
	}
	
	public void drawScroll()
	{
		GL11.glColor4f(1, 1, 1, 1);
		
		int xStart = xPosition + xSize - 6;
		int yStart = yPosition;
		
		for(int i = 0; i < size; i++)
		{
			guiObj.drawTexturedRect(xStart, yStart+(i*10), 10, 1, 6, 10);
		}
		
		guiObj.drawTexturedRect(xStart, yStart, 10, 0, 6, 1);
		guiObj.drawTexturedRect(xStart, yStart+(size*10)-1, 10, 0, 6, 1);
		
		guiObj.drawTexturedRect(xStart+1, yStart+1+getScroll(), 16, 0, 4, 4);
	}
	
	public int getMaxScroll()
	{
		return textEntries.size()-size;
	}
	
	public int getScroll()
	{
		return 0;
	}

	@Override
	public void renderForeground(int xAxis, int yAxis)
	{		
		if((scrollIndex > 0 && textEntries.size() <= size) || (scrollIndex > 0 && getMaxScroll() < scrollIndex))
		{
			scrollIndex = 0;
		}
		
		if(!textEntries.isEmpty())
		{
			for(int i = 0; i < size; i++)
			{
				int index = scrollIndex + i;
				
				if(index <= textEntries.size()-1)
				{
					guiObj.getFont().drawString(textEntries.get(index), xPosition + 1, yPosition + 1 + (10*i), 0x00CD00);
				}
			}
		}
		
		mc.renderEngine.bindTexture(RESOURCE);
		
		drawScroll();

		mc.renderEngine.bindTexture(defaultLocation);
	}

	@Override
	public void preMouseClicked(int xAxis, int yAxis, int button) {}

	@Override
	public void mouseClicked(int xAxis, int yAxis, int button) {}
}
