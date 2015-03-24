package mekanism.client.gui;

import java.util.ArrayList;

import mekanism.api.Coord4D;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.block.Block;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import org.apache.commons.lang3.tuple.Pair;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.Rectangle;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiSeismicReader extends GuiScreen
{
	private World worldObj;

	public ItemStack itemStack;

	private ArrayList<Pair<Integer, Block>> blockList = new ArrayList<Pair<Integer, Block>>();

	public Coord4D pos;

	protected int xSize = 137;

	protected int ySize = 182;

	private Rectangle upButton, downButton, tooltip;

	private int currentLayer = 0;

	public GuiSeismicReader(World world, Coord4D coord, ItemStack stack)
	{
		pos = coord;
		pos.yCoord = Math.min(255, pos.yCoord);
		worldObj = world;

		itemStack = stack;
		calculate();
		currentLayer = Math.max(0, blockList.size() - 1);
	}

	@Override
	public void initGui()
	{
		super.initGui();

		upButton = new Rectangle((width - xSize) / 2 + 70, (height - ySize) / 2 + 75, 13, 13);
		downButton = new Rectangle((width - xSize) / 2 + 70, (height - ySize) / 2 + 92, 13, 13);
		tooltip = new Rectangle((width - xSize) / 2 + 30, (height - ySize) / 2 + 82, 16, 16);
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTick)
	{
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		int guiWidth = (width - xSize) / 2;
		int guiHeight = (height - ySize) / 2;

		mc.renderEngine.bindTexture(MekanismUtils.getResource(ResourceType.GUI, "GuiSeismicReader.png"));

		drawTexturedModalRect(guiWidth, guiHeight, 0, 0, xSize, ySize);
		// Draws the up button
		
		if(upButton.intersects(new Rectangle(mouseX, mouseY, 1, 1)))
		{
			GL11.glColor3f(0.5f, 0.5f, 1f);
		}
		
		drawTexturedModalRect(upButton.getX(), upButton.getY(), 137, 0, upButton.getWidth(), upButton.getHeight());
		GL11.glColor3f(1, 1, 1);
		
		// Draws the down button
		if(downButton.intersects(new Rectangle(mouseX, mouseY, 1, 1)))
		{
			GL11.glColor3f(0.5f, 0.5f, 1f);
		}
		
		drawTexturedModalRect(downButton.getX(), downButton.getY(), 150, 0, downButton.getWidth(), downButton.getHeight());
		GL11.glColor3f(1, 1, 1);

		// Fix the overlapping if > 100
		GL11.glPushMatrix();
		GL11.glTranslatef(guiWidth + 48, guiHeight + 87, 0);
		
		if(currentLayer >= 100)
		{
			GL11.glTranslatef(0, 1, 0);
			GL11.glScalef(0.7f, 0.7f, 0.7f);
		}
		
		fontRendererObj.drawString(String.format("%s", currentLayer), 0, 0, 0xAFAFAF);
		GL11.glPopMatrix();

		// Render the item stacks
		for(int i = 0; i < 9; i++)
		{
			int centralX = guiWidth + 32, centralY = guiHeight + 103;
			int layer = currentLayer + (i - 5);
			
			if(0 <= layer && layer < blockList.size())
			{
				ItemStack stack = new ItemStack(blockList.get(layer).getRight(), 1, blockList.get(layer).getLeft());
				
				if(stack.getItem() == null)
				{
					continue;
				}
				
				GL11.glPushMatrix();
				GL11.glTranslatef(centralX - 2, centralY - i * 16 + (22 * 2), 0);
				
				if(i < 4)
				{
					GL11.glTranslatef(0.2f, 2.5f, 0);
				}
				
				if(i != 4)
				{
					GL11.glTranslatef(1.5f, 0, 0);
					GL11.glScalef(0.8f, 0.8f, 0.8f);
				}
				
				itemRender.renderItemAndEffectIntoGUI(fontRendererObj, this.mc.getTextureManager(), stack, 0, 0);
				GL11.glPopMatrix();
			}
		}

		// Get the name from the stack and render it
		if(currentLayer - 1 >= 0)
		{
			ItemStack nameStack = new ItemStack(blockList.get(currentLayer - 1).getRight(), 0, blockList.get(currentLayer - 1).getLeft());
			String renderString = "unknown";
			
			if(nameStack.getItem() != null)
			{
				renderString = nameStack.getDisplayName();
			}
			else if(blockList.get(currentLayer - 1).getRight() == Blocks.air)
			{
				renderString = "Air";
			}
			
			String capitalised = renderString.substring(0, 1).toUpperCase() + renderString.substring(1);
			float renderScale = 1.0f;
			int lengthX = fontRendererObj.getStringWidth(capitalised);

			renderScale = lengthX > 53 ? 53f / lengthX : 1.0f;

			GL11.glPushMatrix();
			GL11.glTranslatef(guiWidth + 72, guiHeight + 16, 0);
			GL11.glScalef(renderScale, renderScale, renderScale);
			fontRendererObj.drawString(capitalised, 0, 0, 0x919191);
			GL11.glPopMatrix();
			
			if(tooltip.intersects(new Rectangle(mouseX, mouseY, 1, 1)))
			{
				mc.renderEngine.bindTexture(MekanismUtils.getResource(ResourceType.GUI_ELEMENT, "GuiTooltips.png"));
				int fontLengthX = fontRendererObj.getStringWidth(capitalised) + 5;
				int renderX = mouseX + 10, renderY = mouseY - 5;
				GL11.glPushMatrix();
				GL11.glColor3f(1, 1, 1);
				drawTexturedModalRect(renderX, renderY, 0, 0, fontLengthX, 16);
				drawTexturedModalRect(renderX + fontLengthX, renderY, 0, 16, 2, 16);
				fontRendererObj.drawString(capitalised, renderX + 4, renderY + 4, 0x919191);
				GL11.glPopMatrix();
			}
		}

		int frequency = 0;

		for(Pair<Integer, Block> pair : blockList)
		{
			if(blockList.get(currentLayer - 1) != null)
			{
				Block block = blockList.get(currentLayer - 1).getRight();

				if(pair.getRight() == block && pair.getLeft() == blockList.get(currentLayer - 1).getLeft())
				{
					frequency++;
				}
			}
		}

		GL11.glPushMatrix();
		GL11.glTranslatef(guiWidth + 72, guiHeight + 26, 0);
		GL11.glScalef(0.70f, 0.70f, 0.70f);
		fontRendererObj.drawString(MekanismUtils.localize("gui.abundancy") + ": " + frequency, 0, 0, 0x919191);
		GL11.glPopMatrix();
		super.drawScreen(mouseX, mouseY, partialTick);
	}

	public String wrapString(String str, int index)
	{
		String string = str;

		for(int i = 0; i < string.length(); i++)
		{
			if(i == index)
			{
				string = string.substring(0, i) + "\n" + string.substring(i);
			}
		}

		return string;
	}

	@Override
	public void onGuiClosed()
	{
		super.onGuiClosed();

		blockList.clear();
	}

	public void calculate()
	{
		for(int y = 0; y < pos.yCoord; y++)
		{
			Block block = worldObj.getBlock(pos.xCoord, y, pos.zCoord);
			int metadata = worldObj.getBlockMetadata(pos.xCoord, y, pos.zCoord);
			
			blockList.add(Pair.of(metadata, block));
		}
	}

	@Override
	protected void mouseClicked(int xPos, int yPos, int buttonClicked)
	{
		super.mouseClicked(xPos, yPos, buttonClicked);

		if(upButton.intersects(new Rectangle(xPos, yPos, 1, 1)))
		{
			if(currentLayer + 1 <= blockList.size() - 1)
			{
				currentLayer++;
			}
		}

		if(downButton.intersects(new Rectangle(xPos, yPos, 1, 1)))
		{
			if(currentLayer - 1 >= 1)
			{
				currentLayer--;
			}
		}
	}

	@Override
	public boolean doesGuiPauseGame()
	{
		return false;
	}
}
