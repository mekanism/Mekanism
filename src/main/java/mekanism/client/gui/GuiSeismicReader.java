package mekanism.client.gui;

import java.util.ArrayList;
import java.util.List;

import mekanism.api.Coord4D;
import mekanism.common.item.ItemSeismicReader;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.IFluidBlock;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

@SideOnly(Side.CLIENT)
public class GuiSeismicReader extends GuiScreen
{
	private World worldObj = Minecraft.getMinecraft().theWorld;
	
	public ItemStack itemStack;
	
	public int scroll;
	
	public int prevMouseY;
	
	public Coord4D pos;
	
	public boolean prevMouseDown;
	
	public boolean isDragging = false;
	
	public List<SeismicType> seismicCalculation = new ArrayList<SeismicType>();
	
	public int scrollStartX = 10;
	public int scrollStartY = 12;
	
	protected int xSize = 118;
	protected int ySize = 166;
	
	public GuiSeismicReader(Coord4D coord, ItemStack stack)
	{
		pos = coord;
		pos.getPos().getY() = Math.min(255, pos.getPos().getY());
		
		itemStack = stack;
		
		calculate();
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTick)
	{
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		mc.renderEngine.bindTexture(MekanismUtils.getResource(ResourceType.GUI, "GuiSeismicReader.png"));

		int guiWidth = (width - xSize) / 2;
		int guiHeight = (height - ySize) / 2;
		
		int xAxis = mouseX - guiWidth;
		int yAxis = mouseY - guiHeight;
		
		if(Mouse.isButtonDown(0))
		{
			if(!isDragging && (xAxis >= 10 && xAxis <= 48 && yAxis >= 12 && yAxis <= 154))
			{
				if(!prevMouseDown)
				{
					prevMouseDown = true;
				}
				else {
					int mouseDiff = prevMouseY-yAxis;
					
					scroll = Math.max(0, Math.min(scroll+mouseDiff, calcMaxScroll()));
				}
			}
			
			int maxScroll = calcMaxScroll();
			
			if(!isDragging && maxScroll > 0 && (xAxis >= 49 && xAxis <= 53 && yAxis >= getScrollButtonY() && yAxis <= getScrollButtonY()+4))
			{	
				if(!prevMouseDown)
				{
					prevMouseDown = true;
					isDragging = true;
				}
			}
			
			if(isDragging)
			{
				int relY = Math.max(13, Math.min(149, yAxis))-13;
				scroll = calcScrollFromButton(relY);
			}
			
			prevMouseY = yAxis;
		}
		else {
			prevMouseDown = false;
			isDragging = false;
		}
		
		drawTexturedModalRect(guiWidth + scrollStartX, guiHeight + scrollStartY, xSize, 13, 38, 142);
		
		drawChartLayer(guiWidth, guiHeight);
		
		drawTexturedModalRect(guiWidth, guiHeight, 0, 0, xSize, ySize);
		
		drawTexturedModalRect(guiWidth + scrollStartX + 38 + 1, guiHeight + getScrollButtonY(), xSize, 0, 4, 4);
		
		fontRendererObj.drawString(MekanismUtils.localize("gui.seismicReader.short"), guiWidth + 62, guiHeight + 18, 0x000000);
		
		fontRendererObj.drawString(MekanismUtils.localize("gui.seismicReader.solids"), guiWidth + 70, guiHeight + 40, 0x0404040);
		fontRendererObj.drawString(MekanismUtils.localize("gui.seismicReader.fluids"), guiWidth + 70, guiHeight + 62, 0x0404040);
		fontRendererObj.drawString(MekanismUtils.localize("gui.empty"), guiWidth + 70, guiHeight + 78, 0x0404040);
		
		fontRendererObj.drawString(MekanismUtils.localize("gui.seismicReader.reading"), guiWidth + 62, guiHeight + 114, 0x00CD00);
		fontRendererObj.drawString(MekanismUtils.localize("gui.energy") + ":", guiWidth + 62, guiHeight + 132, 0x00CD00);
		fontRendererObj.drawString(MekanismUtils.getEnergyDisplay(getEnergy()), guiWidth + 62, guiHeight + 141, 0x00CD00);
		
		super.drawScreen(mouseX, mouseY, partialTick);
	}
	
	public double getEnergy()
	{
		return ((ItemSeismicReader)itemStack.getItem()).getEnergy(itemStack);
	}
	
	public void drawChartLayer(int guiWidth, int guiHeight)
	{
		if(scroll <= 5)
		{
			drawTexturedModalRect(guiWidth + scrollStartX, guiHeight + scrollStartY+5-scroll, xSize, 4, 38, 1);
		}
		
		int amount = Math.min((142/2)+Math.min(0, (scroll/2)-3), pos.getPos().getY());
		int start = pos.getPos().getY()-(scroll/2)+Math.min(3, scroll/2);
		int yStart = scrollStartY + Math.max(0, 6-scroll);
		
		drawScale(guiWidth, guiHeight, amount, start, yStart, scroll%2==1);
		
		if(calcMaxScroll() > 0 && scroll == calcMaxScroll())
		{
			drawTexturedModalRect(guiWidth + scrollStartX, guiHeight + scrollStartY+142-1, xSize, 4, 38, 1);
		}
		else if(calcMaxScroll() <= 0)
		{
			int dist = yStart + amount*2;
			drawTexturedModalRect(guiWidth + scrollStartX, guiHeight + dist, xSize, 4, 38, 1);
		}
	}
	
	public void drawScale(int guiWidth, int guiHeight, int amount, int start, int yStart, boolean half)
	{
		int starting = 0;
		int toRender = amount;
		int nextRender = guiHeight + yStart;
		
		if(half)
		{
			starting++;
		}
		
		if(scroll < 6)
		{
			nextRender += (half ? 1 : 0);
		}
		
		if(start%4 != 0)
		{
			starting += (4-(start%4))*2;
			drawTexturedModalRect(guiWidth + 30, nextRender, xSize, 5+starting, 3, 8-starting);
			toRender -= (8-starting)/2;
			nextRender += (8-starting);
			
			if(!seismicCalculation.isEmpty())
			{
				int rendered = 8-starting+(half && scroll > 6 ? 1 : 0);
				int nextY = guiHeight + yStart - (half && scroll > 6 ? 1 : 0);
				
				seismicCalculation.get(start-1).render(this, xSize, guiWidth, nextY);
				
				if(rendered > 2)
				{
					seismicCalculation.get(start-2).render(this, xSize, guiWidth, nextY+2);
				}
				
				if(rendered > 4)
				{
					seismicCalculation.get(start-3).render(this, xSize, guiWidth, nextY+4);
				}
			}
		}
		else {
			nextRender -= half ? 1 : 0;
		}
		
		while(toRender >= 4)
		{
			toRender -= 4;
			
			drawTexturedModalRect(guiWidth + 30, nextRender, xSize, 5, 3, 8);
			
			int index = start-(amount-toRender);
			
			if((index-1)%2 == 0)
			{
				index -= 1;
			}
			
			if(index%8 == 0 && pos.getPos().getY()-index > 6)
			{
				int yPos = nextRender+2;
				
				if(index == 0)
				{
					yPos -= 2;
				}
				
				fontRendererObj.drawString(Integer.toString(index), guiWidth + 28-fontRendererObj.getStringWidth(Integer.toString(index)), yPos, 0xFFFFFF);
				mc.renderEngine.bindTexture(MekanismUtils.getResource(ResourceType.GUI, "GuiSeismicReader.png"));
			}
			
			if(!seismicCalculation.isEmpty())
			{
				seismicCalculation.get(index+3).render(this, xSize, guiWidth, nextRender);
				seismicCalculation.get(index+2).render(this, xSize, guiWidth, nextRender+2);
				seismicCalculation.get(index+1).render(this, xSize, guiWidth, nextRender+4);
				seismicCalculation.get(index+0).render(this, xSize, guiWidth, nextRender+6);
			}
			
			nextRender += 8;
		}
		
		if(toRender > 0)
		{
			drawTexturedModalRect(guiWidth + 30, nextRender, xSize, 5, 3, toRender*2);
			
			if(!seismicCalculation.isEmpty())
			{
				int index = start-(amount-toRender);
				
				if((index-1)%2 == 0)
				{
					index -= 1;
				}
				
				if(index-1 >= 0)
				{
					seismicCalculation.get(index-1).render(this, xSize, guiWidth, nextRender);
				}
				
				if(toRender > 1 && index-2 >= 0)
				{
					seismicCalculation.get(index-2).render(this, xSize, guiWidth, nextRender+2);
				}
				
				if(toRender > 2 && index-3 >= 0)
				{
					seismicCalculation.get(index-3).render(this, xSize, guiWidth, nextRender+4);
				}
			}
		}
	}
	
	public int getScrollButtonY()
	{
		int max = calcMaxScroll();
		
		if(max == 0)
		{
			return scrollStartY+1;
		}
		
		return scrollStartY+1+(int)(((float)scroll/max)*136);
	}
	
	public int calcScrollFromButton(int relButtonY)
	{
		return (int)(((float)relButtonY/136)*calcMaxScroll());
	}
	
	public int calcMaxScroll()
	{
		int ret = 6;
		
		ret += (pos.getPos().getY()*2); //2 pixels per block
		ret += 1; //Bottom layer thing
		ret -= 142; //142 total pixels lengthwise on display
		
		return ret;
	}
	
	public void calculate()
	{
		seismicCalculation.clear();
		
		for(int y = 1; y <= pos.getPos().getY(); y++)
		{
			Coord4D coord = new Coord4D(pos.getPos().getX(), y, pos.getPos().getZ(), pos.dimensionId);
			
			if(coord.isAirBlock(worldObj))
			{
				seismicCalculation.add(SeismicType.EMPTY);
				continue;
			}
			
			Block block = coord.getBlock(worldObj);
			int meta = coord.getMetadata(worldObj);

			if(block == Blocks.grass)
			{
				seismicCalculation.add(SeismicType.GRASS);
				continue;
			}
			else if(block == Blocks.dirt)
			{
				seismicCalculation.add(SeismicType.DIRT);
				continue;
			}
			else if(block == Blocks.stone)
			{
				seismicCalculation.add(SeismicType.STONE);
				continue;
			}
			else if(block == Blocks.bedrock)
			{
				seismicCalculation.add(SeismicType.BEDROCK);
				continue;
			}
			else if(block == Blocks.water || block == Blocks.flowing_water)
			{
				seismicCalculation.add(SeismicType.WATER);
				continue;
			}
			else if(block == Blocks.lava || block == Blocks.flowing_lava)
			{
				seismicCalculation.add(SeismicType.LAVA);
				continue;
			}
			
			if(block instanceof IFluidBlock)
			{
				Fluid fluid = ((IFluidBlock)block).getFluid();
				
				if(fluid != null)
				{
					String name = fluid.getName().toLowerCase();
					
					if(name.equals("water"))
					{
						seismicCalculation.add(SeismicType.WATER);
						continue;
					}
					else if(name.equals("lava"))
					{
						seismicCalculation.add(SeismicType.LAVA);
						continue;
					}
					else if(name.equals("oil"))
					{
						seismicCalculation.add(SeismicType.OIL);
						continue;
					}
				}
			}
			
			List<String> oreDictNames = MekanismUtils.getOreDictName(new ItemStack(block, 1, meta));
			boolean foundName = false;
			
			if(oreDictNames != null && !oreDictNames.isEmpty())
			{
				for(String s : oreDictNames)
				{
					if(s.trim().startsWith("ore"))
					{
						seismicCalculation.add(SeismicType.ORE);
						foundName = true;
						break;
					}
				}
			}
			
			if(foundName)
			{
				continue;
			}
			
			seismicCalculation.add(SeismicType.OTHER);
		}
	}
	
	@Override
	public boolean doesGuiPauseGame()
	{
		return false;
	}		
	
	public static enum SeismicType
	{
		GRASS,
		DIRT,
		OTHER,
		ORE,
		STONE,
		BEDROCK,
		WATER,
		LAVA,
		OIL,
		EMPTY;
		
		public void render(GuiSeismicReader gui, int xSize, int guiWidth, int y)
		{
			gui.drawTexturedModalRect(guiWidth + 33, y, xSize, 155+(ordinal()*2), 4, 2);
		}
	}
}
