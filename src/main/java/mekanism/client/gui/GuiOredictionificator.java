package mekanism.client.gui;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import mekanism.api.Coord4D;
import mekanism.api.EnumColor;
import mekanism.client.gui.element.GuiProgress;
import mekanism.client.gui.element.GuiProgress.IProgressInfoHandler;
import mekanism.client.gui.element.GuiProgress.ProgressBar;
import mekanism.client.gui.element.GuiSlot;
import mekanism.client.gui.element.GuiSlot.SlotType;
import mekanism.client.render.MekanismRenderer;
import mekanism.client.sound.SoundHandler;
import mekanism.common.Mekanism;
import mekanism.common.inventory.container.ContainerOredictionificator;
import mekanism.common.network.PacketOredictionificatorGui.OredictionificatorGuiMessage;
import mekanism.common.network.PacketOredictionificatorGui.OredictionificatorGuiPacket;
import mekanism.common.tile.TileEntityOredictionificator;
import mekanism.common.tile.TileEntityOredictionificator.OredictionificatorFilter;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiOredictionificator extends GuiMekanism
{
	public TileEntityOredictionificator tileEntity;
	
	public Map<OredictionificatorFilter, ItemStack> renderStacks = new HashMap<OredictionificatorFilter, ItemStack>();
	
	public boolean isDragging = false;

	public int dragOffset = 0;
	
	public float scroll;
	
	public GuiOredictionificator(InventoryPlayer inventory, TileEntityOredictionificator tentity)
	{
		super(tentity, new ContainerOredictionificator(inventory, tentity));
		tileEntity = tentity;
		
		guiElements.add(new GuiProgress(new IProgressInfoHandler()
		{
			@Override
			public double getProgress()
			{
				return tileEntity.didProcess ? 1 : 0;
			}
		}, ProgressBar.LARGE_RIGHT, this, MekanismUtils.getResource(ResourceType.GUI, "GuiOredictionificator.png"), 62, 118));
		guiElements.add(new GuiSlot(SlotType.NORMAL, this, MekanismUtils.getResource(ResourceType.GUI, "GuiOredictionificator.png"), 25, 114));
		guiElements.add(new GuiSlot(SlotType.NORMAL, this, MekanismUtils.getResource(ResourceType.GUI, "GuiOredictionificator.png"), 133, 114));
		
		ySize+=64;
	}
	
	public int getScroll()
	{
		return Math.max(Math.min((int)(scroll*88), 88), 0);
	}
	
	public int getFilterIndex()
	{
		if(tileEntity.filters.size() <= 3)
		{
			return 0;
		}

		return (int)((tileEntity.filters.size()*scroll) - ((3F/(float)tileEntity.filters.size()))*scroll);
	}
	
	@Override
	public void initGui()
	{
		super.initGui();

		int guiWidth = (width - xSize) / 2;
		int guiHeight = (height - ySize) / 2;

		buttonList.clear();
		buttonList.add(new GuiButton(0, guiWidth + 10, guiHeight + 86, 142, 20, MekanismUtils.localize("gui.newFilter")));
	}
	
	@Override
	protected void actionPerformed(GuiButton guibutton)
	{
		super.actionPerformed(guibutton);

		if(guibutton.id == 0)
		{
			Mekanism.packetHandler.sendToServer(new OredictionificatorGuiMessage(OredictionificatorGuiPacket.SERVER, Coord4D.get(tileEntity), 1, 0, 0));
		}
	}
	
	@Override
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY)
	{
		int xAxis = (mouseX - (width - xSize) / 2);
		int yAxis = (mouseY - (height - ySize) / 2);

		fontRendererObj.drawString(tileEntity.getInventoryName(), (xSize/2)-(fontRendererObj.getStringWidth(tileEntity.getInventoryName())/2), 6, 0x404040);
		fontRendererObj.drawString(MekanismUtils.localize("container.inventory"), 8, (ySize - 96) + 2, 0x404040);

		for(int i = 0; i < 3; i++)
		{
			if(tileEntity.filters.get(getFilterIndex()+i) != null)
			{
				OredictionificatorFilter filter = tileEntity.filters.get(getFilterIndex()+i);
				int yStart = i*22 + 18;
				
				if(!renderStacks.containsKey(filter))
				{
					updateRenderStacks();
				}
				
				ItemStack stack = renderStacks.get(filter);
				
				if(stack != null)
				{
					GL11.glPushMatrix();
					GL11.glEnable(GL11.GL_LIGHTING);
					itemRender.renderItemAndEffectIntoGUI(fontRendererObj, mc.getTextureManager(), stack, 13, yStart + 3);
					GL11.glDisable(GL11.GL_LIGHTING);
					GL11.glPopMatrix();
				}

				fontRendererObj.drawString(MekanismUtils.localize("gui.filter"), 32, yStart + 2, 0x404040);
				renderScaledText(filter.filter, 32, yStart + 2 + 9, 0x404040, 117);
			}
		}
		
		super.drawGuiContainerForegroundLayer(mouseX, mouseY);
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTick, int mouseX, int mouseY)
	{
		mc.renderEngine.bindTexture(MekanismUtils.getResource(ResourceType.GUI, "GuiOredictionificator.png"));
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		int guiWidth = (width - xSize) / 2;
		int guiHeight = (height - ySize) / 2;
		drawTexturedModalRect(guiWidth, guiHeight, 0, 0, xSize, ySize);

		int xAxis = mouseX - guiWidth;
		int yAxis = mouseY - guiHeight;
		
		drawTexturedModalRect(guiWidth + 154, guiHeight + 18 + getScroll(), 232, 0, 12, 15);
		
		super.drawGuiContainerBackgroundLayer(partialTick, mouseX, mouseY);
		
		for(int i = 0; i < 3; i++)
		{
			if(tileEntity.filters.get(getFilterIndex()+i) != null)
			{
				int yStart = i*22 + 18;
				boolean mouseOver = xAxis >= 10 && xAxis <= 152 && yAxis >= yStart && yAxis <= yStart+22;
				
				if(mouseOver)
				{
					MekanismRenderer.color(EnumColor.GREY, 3.0F);
				}
				
				drawTexturedModalRect(guiWidth + 10, guiHeight + yStart, 0, 230, 142, 22);
				
				MekanismRenderer.resetColor();
			}
		}
	}
	
	@Override
	public void mouseClicked(int mouseX, int mouseY, int button)
	{
		super.mouseClicked(mouseX, mouseY, button);

		if(button == 0)
		{
			int xAxis = (mouseX - (width - xSize) / 2);
			int yAxis = (mouseY - (height - ySize) / 2);

			if(xAxis >= 154 && xAxis <= 166 && yAxis >= getScroll()+18 && yAxis <= getScroll()+18+15)
			{
				if(tileEntity.filters.size()>3)
				{
					dragOffset = yAxis - (getScroll()+18);
					isDragging = true;
				}
				else {
					scroll = 0;
				}
			}

			for(int i = 0; i < 3; i++)
			{
				if(tileEntity.filters.get(getFilterIndex()+i) != null)
				{
					int yStart = i*29 + 18;

					if(xAxis >= 10 && xAxis <= 152 && yAxis >= yStart && yAxis <= yStart+22)
					{
						OredictionificatorFilter filter = tileEntity.filters.get(getFilterIndex()+i);

                        SoundHandler.playSound("gui.button.press");
						Mekanism.packetHandler.sendToServer(new OredictionificatorGuiMessage(OredictionificatorGuiPacket.SERVER_INDEX, Coord4D.get(tileEntity), 1, getFilterIndex()+i, 0));
					}
				}
			}
		}
	}
	
	@Override
	protected void mouseClickMove(int mouseX, int mouseY, int button, long ticks)
	{
		super.mouseClickMove(mouseX, mouseY, button, ticks);

		int xAxis = (mouseX - (width - xSize) / 2);
		int yAxis = (mouseY - (height - ySize) / 2);

		if(isDragging)
		{
			scroll = Math.min(Math.max((float)(yAxis-18-dragOffset)/88F, 0), 1);
		}
	}
	
	@Override
	protected void mouseMovedOrUp(int x, int y, int type)
	{
		super.mouseMovedOrUp(x, y, type);

		if(type == 0 && isDragging)
		{
			dragOffset = 0;
			isDragging = false;
		}
	}
	
	public void updateRenderStacks()
	{
		renderStacks.clear();
		
		for(OredictionificatorFilter filter : tileEntity.filters)
		{
			if(filter.filter == null || filter.filter.isEmpty())
			{
				renderStacks.put(filter, null);
				continue;
			}
			
			List<ItemStack> stacks = OreDictionary.getOres(filter.filter);
			
			if(stacks.isEmpty())
			{
				renderStacks.put(filter, null);
				continue;
			}
			
			if(stacks.size()-1 >= filter.index)
			{
				renderStacks.put(filter, stacks.get(filter.index).copy());
			}
			else {
				renderStacks.put(filter, null);
			}
		}
	}
}
