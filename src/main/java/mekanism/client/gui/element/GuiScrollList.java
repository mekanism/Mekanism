package mekanism.client.gui.element;

import java.util.ArrayList;
import java.util.List;

import mekanism.client.gui.IGuiWrapper;
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
	
	public int dragOffset = 0;
	
	public int selected = -1;
	
	public float scroll;
	
	public boolean isDragging;
	
	public GuiScrollList(IGuiWrapper gui, ResourceLocation def, int x, int y, int sizeX, int sizeY)
	{
		super(MekanismUtils.getResource(ResourceType.GUI_ELEMENT, "GuiScrollList.png"), gui, def);
		
		xPosition = x;
		yPosition = y;
		
		xSize = sizeX;
		size = sizeY;
	}
	
	public boolean hasSelection()
	{
		return selected != -1;
	}
	
	public int getSelection()
	{
		return selected;
	}
	
	public void setText(List<String> text)
	{
		if(text == null)
		{
			textEntries.clear();
			return;
		}
		
		if(selected > text.size()-1)
		{
			selected = -1;
		}
		
		textEntries = text;
		
		if(textEntries.size()<=size)
		{
			scroll = 0;	
		}
	}
	
	@Override
	public Rectangle4i getBounds(int guiWidth, int guiHeight)
	{
		return new Rectangle4i(guiWidth + xPosition, guiHeight + yPosition, xSize, size*10);
	}

	@Override
	public void renderBackground(int xAxis, int yAxis, int guiWidth, int guiHeight)
	{
		mc.renderEngine.bindTexture(RESOURCE);
		
		drawBlack(guiWidth, guiHeight);
		drawSelected(guiWidth, guiHeight, selected);

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
	
	public void drawSelected(int guiWidth, int guiHeight, int index)
	{
		int scroll = getScrollIndex();
		
		if(selected != -1 && index >= scroll && index <= scroll+size-1)
		{
			int xDisplays = xSize/10 + (xSize%10 > 0 ? 1 : 0);
			
			for(int xIter = 0; xIter < xDisplays; xIter++)
			{
				int width = (xSize%10 > 0 && xIter == xDisplays ? xSize%10 : 10);
				guiObj.drawTexturedRect(guiWidth + xPosition + (xIter*10), guiHeight + yPosition + (index-scroll)*10, 0, 10, width, 10);
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
		return (size*10)-2;
	}
	
	public int getScroll()
	{
		return Math.max(Math.min((int)(scroll*(getMaxScroll()-4)), (getMaxScroll()-4)), 0);
	}
	
	public int getScrollIndex()
	{
		if(textEntries.size() <= size)
		{
			return 0;
		}

		return (int)((textEntries.size()*scroll) - (((float)size/(float)textEntries.size()))*scroll);
	}

	@Override
	public void renderForeground(int xAxis, int yAxis)
	{
		if(!textEntries.isEmpty())
		{
			for(int i = 0; i < size; i++)
			{
				int index = getScrollIndex() + i;
				
				if(index <= textEntries.size()-1)
				{
					renderScaledText(textEntries.get(index), xPosition + 1, yPosition + 1 + (10*i), 0x00CD00, xSize-6);
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
	public void mouseClicked(int xAxis, int yAxis, int button) 
	{
		if(button == 0)
		{
			int xStart = xPosition + xSize - 5;
			
			if(xAxis >= xStart && xAxis <= xStart+4 && yAxis >= getScroll()+yPosition+1 && yAxis <= getScroll()+4+yPosition+1)
			{
				if(textEntries.size()>size)
				{
					dragOffset = yAxis - (getScroll()+yPosition+1);
					isDragging = true;
				}
			}
			else if(xAxis >= xPosition && xAxis <= xPosition + xSize-6 && yAxis >= yPosition && yAxis <= yPosition+size*10)
			{
				int index = getScrollIndex();
				selected = -1;
				
				for(int i = 0; i < size; i++)
				{
					if(index+i <= textEntries.size()-1)
					{
						if(yAxis >= (yPosition + i*10) && yAxis <= (yPosition + i*10 + 10))
						{
							selected = index+i;
							break;
						}
					}
				}
			}
		}
	}
	
	@Override
	public void mouseClickMove(int xAxis, int yAxis, int button, long ticks)
	{
		super.mouseClickMove(xAxis, yAxis, button, ticks);

		if(isDragging)
		{
			scroll = Math.min(Math.max((float)(yAxis-(yPosition+1)-dragOffset)/(float)(getMaxScroll()-4), 0), 1);
		}
	}

	@Override
	public void mouseMovedOrUp(int xAxis, int yAxis, int type)
	{
		super.mouseMovedOrUp(xAxis, yAxis, type);

		if(type == 0)
		{
			if(isDragging)
			{
				dragOffset = 0;
				isDragging = false;
			}
		}
	}
}
