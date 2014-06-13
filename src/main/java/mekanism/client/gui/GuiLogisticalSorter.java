package mekanism.client.gui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import mekanism.api.Coord4D;
import mekanism.api.EnumColor;
import mekanism.client.render.MekanismRenderer;
import mekanism.client.sound.SoundHandler;
import mekanism.common.Mekanism;
import mekanism.common.inventory.container.ContainerNull;
import mekanism.common.network.PacketLogisticalSorterGui.LogisticalSorterGuiMessage;
import mekanism.common.network.PacketLogisticalSorterGui.SorterGuiPacket;
import mekanism.common.network.PacketTileEntity.TileEntityMessage;
import mekanism.common.tile.TileEntityLogisticalSorter;
import mekanism.common.transporter.TItemStackFilter;
import mekanism.common.transporter.TMaterialFilter;
import mekanism.common.transporter.TOreDictFilter;
import mekanism.common.transporter.TransporterFilter;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiLogisticalSorter extends GuiMekanism
{
	public TileEntityLogisticalSorter tileEntity;

	public boolean isDragging = false;

	public int dragOffset = 0;

	public int stackSwitch = 0;

	public Map<TOreDictFilter, StackData> oreDictStacks = new HashMap<TOreDictFilter, StackData>();

	public float scroll;

	public GuiLogisticalSorter(EntityPlayer player, TileEntityLogisticalSorter tentity)
	{
		super(new ContainerNull(player, tentity));
		tileEntity = tentity;
		guiElements.add(new GuiRedstoneControl(this, tileEntity, MekanismUtils.getResource(ResourceType.GUI, "GuiLogisticalSorter.png")));
	}

	public int getScroll()
	{
		return Math.max(Math.min((int)(scroll*123), 123), 0);
	}

	public int getFilterIndex()
	{
		if(tileEntity.filters.size() <= 4)
		{
			return 0;
		}

		return (int)((tileEntity.filters.size()*scroll) - ((4F/(float)tileEntity.filters.size()))*scroll);
	}

	@Override
	public void updateScreen()
	{
		super.updateScreen();

		if(stackSwitch > 0)
		{
			stackSwitch--;
		}

		if(stackSwitch == 0)
		{
			for(Map.Entry<TOreDictFilter, StackData> entry : oreDictStacks.entrySet())
			{
				if(entry.getValue().iterStacks != null && entry.getValue().iterStacks.size() > 0)
				{
					if(entry.getValue().stackIndex == -1 || entry.getValue().stackIndex == entry.getValue().iterStacks.size()-1)
					{
						entry.getValue().stackIndex = 0;
					}
					else if(entry.getValue().stackIndex < entry.getValue().iterStacks.size()-1)
					{
						entry.getValue().stackIndex++;
					}

					entry.getValue().renderStack = entry.getValue().iterStacks.get(entry.getValue().stackIndex);
				}
			}

			stackSwitch = 20;
		}
		else {
			for(Map.Entry<TOreDictFilter, StackData> entry : oreDictStacks.entrySet())
			{
				if(entry.getValue().iterStacks != null && entry.getValue().iterStacks.size() == 0)
				{
					entry.getValue().renderStack = null;
				}
			}
		}

		Set<TOreDictFilter> filtersVisible = new HashSet<TOreDictFilter>();

		for(int i = 0; i < 4; i++)
		{
			if(tileEntity.filters.get(getFilterIndex()+i) instanceof TOreDictFilter)
			{
				filtersVisible.add((TOreDictFilter)tileEntity.filters.get(getFilterIndex()+i));
			}
		}

		for(TransporterFilter filter : tileEntity.filters)
		{
			if(filter instanceof TOreDictFilter && !filtersVisible.contains(filter))
			{
				if(oreDictStacks.containsKey(filter))
				{
					oreDictStacks.remove(filter);
				}
			}
		}
	}

	@Override
	public void mouseClicked(int mouseX, int mouseY, int button)
	{
		super.mouseClicked(mouseX, mouseY, button);

		int xAxis = (mouseX - (width - xSize) / 2);
		int yAxis = (mouseY - (height - ySize) / 2);

		if(button == 0)
		{
			if(xAxis >= 154 && xAxis <= 166 && yAxis >= getScroll()+18 && yAxis <= getScroll()+18+15)
			{
				dragOffset = yAxis - (getScroll()+18);
				isDragging = true;
			}

			for(int i = 0; i < 4; i++)
			{
				if(tileEntity.filters.get(getFilterIndex()+i) != null)
				{
					int yStart = i*29 + 18;

					if(xAxis >= 56 && xAxis <= 152 && yAxis >= yStart && yAxis <= yStart+29)
					{
						TransporterFilter filter = tileEntity.filters.get(getFilterIndex()+i);

						if(filter instanceof TItemStackFilter)
						{
							SoundHandler.playSound("gui.button.press");
							Mekanism.packetHandler.sendToServer(new LogisticalSorterGuiMessage(SorterGuiPacket.SERVER_INDEX, Coord4D.get(tileEntity), 1, getFilterIndex()+i, 0));
						}
						else if(filter instanceof TOreDictFilter)
						{
							SoundHandler.playSound("gui.button.press");
							Mekanism.packetHandler.sendToServer(new LogisticalSorterGuiMessage(SorterGuiPacket.SERVER_INDEX, Coord4D.get(tileEntity), 2, getFilterIndex()+i, 0));
						}
						else if(filter instanceof TMaterialFilter)
						{
							SoundHandler.playSound("gui.button.press");
							Mekanism.packetHandler.sendToServer(new LogisticalSorterGuiMessage(SorterGuiPacket.SERVER_INDEX, Coord4D.get(tileEntity), 3, getFilterIndex()+i, 0));
						}
					}
				}
			}

			if(xAxis >= 12 && xAxis <= 26 && yAxis >= 110 && yAxis <= 124)
			{
				ArrayList data = new ArrayList();
				data.add(1);

				Mekanism.packetHandler.sendToServer(new TileEntityMessage(Coord4D.get(tileEntity), data));
				SoundHandler.playSound("gui.button.press");
			}

			if(xAxis >= 12 && xAxis <= 26 && yAxis >= 84 && yAxis <= 98)
			{
				ArrayList data = new ArrayList();
				data.add(2);

				Mekanism.packetHandler.sendToServer(new TileEntityMessage(Coord4D.get(tileEntity), data));
				SoundHandler.playSound("gui.button.press");
			}
		}

		if(Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) && button == 0)
		{
			button = 2;
		}

		if(xAxis >= 13 && xAxis <= 29 && yAxis >= 137 && yAxis <= 153)
		{
			ArrayList data = new ArrayList();
			data.add(0);
			data.add(button);

			Mekanism.packetHandler.sendToServer(new TileEntityMessage(Coord4D.get(tileEntity), data));
			SoundHandler.playSound("mekanism:etc.Ding");
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
			scroll = Math.min(Math.max((float)(yAxis-18-dragOffset)/123F, 0), 1);
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

	@Override
	public void initGui()
	{
		super.initGui();

		int guiWidth = (width - xSize) / 2;
		int guiHeight = (height - ySize) / 2;

		buttonList.clear();
		buttonList.add(new GuiButton(0, guiWidth + 56, guiHeight + 136, 96, 20, MekanismUtils.localize("gui.newFilter")));
	}

	@Override
	protected void actionPerformed(GuiButton guibutton)
	{
		super.actionPerformed(guibutton);

		if(guibutton.id == 0)
		{
			Mekanism.packetHandler.sendToServer(new LogisticalSorterGuiMessage(SorterGuiPacket.SERVER, Coord4D.get(tileEntity), 4, 0, 0));
		}
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY)
	{
		int xAxis = (mouseX - (width - xSize) / 2);
		int yAxis = (mouseY - (height - ySize) / 2);

		fontRendererObj.drawString(tileEntity.getInventoryName(), 43, 6, 0x404040);

		fontRendererObj.drawString(MekanismUtils.localize("gui.filters") + ":", 11, 19, 0x00CD00);
		fontRendererObj.drawString("IS: " + getItemStackFilters().size(), 11, 28, 0x00CD00);
		fontRendererObj.drawString("OD: " + getOreDictFilters().size(), 11, 37, 0x00CD00);
		fontRendererObj.drawString("M: " + getMaterialFilters().size(), 11, 46, 0x00CD00);

		fontRendererObj.drawString("RR:", 12, 74, 0x00CD00);
		fontRendererObj.drawString(MekanismUtils.localize("gui." + (tileEntity.roundRobin ? "on" : "off")), 27, 86, 0x00CD00);

		fontRendererObj.drawString(MekanismUtils.localize("gui.logisticalSorter.auto") + ":", 12, 100, 0x00CD00);
		fontRendererObj.drawString(MekanismUtils.localize("gui." + (tileEntity.autoEject ? "on" : "off")), 27, 112, 0x00CD00);

		fontRendererObj.drawString(MekanismUtils.localize("gui.logisticalSorter.default") + ":", 12, 126, 0x00CD00);

		for(int i = 0; i < 4; i++)
		{
			if(tileEntity.filters.get(getFilterIndex()+i) != null)
			{
				TransporterFilter filter = tileEntity.filters.get(getFilterIndex()+i);
				int yStart = i*29 + 18;

				if(filter instanceof TItemStackFilter)
				{
					TItemStackFilter itemFilter = (TItemStackFilter)filter;

					if(itemFilter.itemType != null)
					{
						GL11.glPushMatrix();
						GL11.glEnable(GL11.GL_LIGHTING);
						itemRender.renderItemAndEffectIntoGUI(fontRendererObj, mc.getTextureManager(), itemFilter.itemType, 59, yStart + 3);
						GL11.glDisable(GL11.GL_LIGHTING);
						GL11.glPopMatrix();
					}

					fontRendererObj.drawString(MekanismUtils.localize("gui.itemFilter"), 78, yStart + 2, 0x404040);
					fontRendererObj.drawString(filter.color != null ? filter.color.getName() : MekanismUtils.localize("gui.none"), 78, yStart + 11, 0x404040);
				}
				else if(filter instanceof TOreDictFilter)
				{
					TOreDictFilter oreFilter = (TOreDictFilter)filter;

					if(!oreDictStacks.containsKey(oreFilter))
					{
						updateStackList(oreFilter);
					}

					if(oreDictStacks.get(filter).renderStack != null)
					{
						try {
							GL11.glPushMatrix();
							GL11.glEnable(GL11.GL_LIGHTING);
							itemRender.renderItemAndEffectIntoGUI(fontRendererObj, mc.getTextureManager(), oreDictStacks.get(filter).renderStack, 59, yStart + 3);
							GL11.glDisable(GL11.GL_LIGHTING);
							GL11.glPopMatrix();
						} catch(Exception e) {}
					}

					fontRendererObj.drawString(MekanismUtils.localize("gui.oredictFilter"), 78, yStart + 2, 0x404040);
					fontRendererObj.drawString(filter.color != null ? filter.color.getName() : MekanismUtils.localize("gui.none"), 78, yStart + 11, 0x404040);
				}
				else if(filter instanceof TMaterialFilter)
				{
					TMaterialFilter itemFilter = (TMaterialFilter)filter;

					if(itemFilter.materialItem != null)
					{
						GL11.glPushMatrix();
						GL11.glEnable(GL11.GL_LIGHTING);
						itemRender.renderItemAndEffectIntoGUI(fontRendererObj, mc.getTextureManager(), itemFilter.materialItem, 59, yStart + 3);
						GL11.glDisable(GL11.GL_LIGHTING);
						GL11.glPopMatrix();
					}

					fontRendererObj.drawString(MekanismUtils.localize("gui.materialFilter"), 78, yStart + 2, 0x404040);
					fontRendererObj.drawString(filter.color != null ? filter.color.getName() : MekanismUtils.localize("gui.none"), 78, yStart + 11, 0x404040);
				}
			}
		}

		if(tileEntity.color != null)
		{
			GL11.glPushMatrix();
			GL11.glColor4f(1, 1, 1, 1);
			GL11.glEnable(GL11.GL_LIGHTING);
			GL11.glEnable(GL12.GL_RESCALE_NORMAL);

			mc.getTextureManager().bindTexture(MekanismRenderer.getBlocksTexture());
			itemRender.renderIcon(13, 137, MekanismRenderer.getColorIcon(tileEntity.color), 16, 16);

			GL11.glDisable(GL11.GL_LIGHTING);
			GL11.glPopMatrix();
		}

		if(xAxis >= 13 && xAxis <= 29 && yAxis >= 137 && yAxis <= 153)
		{
			if(tileEntity.color != null)
			{
				drawCreativeTabHoveringText(tileEntity.color.getName(), xAxis, yAxis);
			}
			else {
				drawCreativeTabHoveringText(MekanismUtils.localize("gui.none"), xAxis, yAxis);
			}
		}

		if(xAxis >= 12 && xAxis <= 26 && yAxis >= 110 && yAxis <= 124)
		{
			drawCreativeTabHoveringText(MekanismUtils.localize("gui.autoEject"), xAxis, yAxis);
		}

		if(xAxis >= 12 && xAxis <= 26 && yAxis >= 84 && yAxis <= 98)
		{
			drawCreativeTabHoveringText(MekanismUtils.localize("gui.logisticalSorter.roundRobin"), xAxis, yAxis);
		}

		super.drawGuiContainerForegroundLayer(mouseX, mouseY);
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTick, int mouseX, int mouseY)
	{
		super.drawGuiContainerBackgroundLayer(partialTick, mouseX, mouseY);

		mc.renderEngine.bindTexture(MekanismUtils.getResource(ResourceType.GUI, "GuiLogisticalSorter.png"));
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		int guiWidth = (width - xSize) / 2;
		int guiHeight = (height - ySize) / 2;
		drawTexturedModalRect(guiWidth, guiHeight, 0, 0, xSize, ySize);

		drawTexturedModalRect(guiWidth + 154, guiHeight + 18 + getScroll(), 232, 0, 12, 15);

		int xAxis = (mouseX - (width - xSize) / 2);
		int yAxis = (mouseY - (height - ySize) / 2);

		for(int i = 0; i < 4; i++)
		{
			if(tileEntity.filters.get(getFilterIndex()+i) != null)
			{
				TransporterFilter filter = tileEntity.filters.get(getFilterIndex()+i);
				int yStart = i*29 + 18;

				boolean mouseOver = xAxis >= 56 && xAxis <= 152 && yAxis >= yStart && yAxis <= yStart+29;

				if(filter instanceof TItemStackFilter)
				{
					MekanismRenderer.color(EnumColor.INDIGO, 1.0F, 2.5F);
				}
				else if(filter instanceof TOreDictFilter)
				{
					MekanismRenderer.color(EnumColor.BRIGHT_GREEN, 1.0F, 2.5F);
				}
				else if(filter instanceof TMaterialFilter)
				{
					MekanismRenderer.color(EnumColor.PURPLE, 1.0F, 4F);
				}
				
				drawTexturedModalRect(guiWidth + 56, guiHeight + yStart, mouseOver ? 0 : 96, 166, 96, 29);
				MekanismRenderer.resetColor();
			}
		}

		if(xAxis >= 12 && xAxis <= 26 && yAxis >= 110 && yAxis <= 124)
		{
			drawTexturedModalRect(guiWidth + 12, guiHeight + 110, 176, 0, 14, 14);
		}
		else {
			drawTexturedModalRect(guiWidth + 12, guiHeight + 110, 176, 14, 14, 14);
		}

		if(xAxis >= 12 && xAxis <= 26 && yAxis >= 84 && yAxis <= 98)
		{
			drawTexturedModalRect(guiWidth + 12, guiHeight + 84, 176 + 14, 0, 14, 14);
		}
		else {
			drawTexturedModalRect(guiWidth + 12, guiHeight + 84, 176 + 14, 14, 14, 14);
		}
	}

	public ArrayList getItemStackFilters()
	{
		ArrayList list = new ArrayList();

		for(TransporterFilter filter : tileEntity.filters)
		{
			if(filter instanceof TItemStackFilter)
			{
				list.add(filter);
			}
		}

		return list;
	}

	public ArrayList getOreDictFilters()
	{
		ArrayList list = new ArrayList();

		for(TransporterFilter filter : tileEntity.filters)
		{
			if(filter instanceof TOreDictFilter)
			{
				list.add(filter);
			}
		}

		return list;
	}
	
	public ArrayList getMaterialFilters()
	{
		ArrayList list = new ArrayList();

		for(TransporterFilter filter : tileEntity.filters)
		{
			if(filter instanceof TMaterialFilter)
			{
				list.add(filter);
			}
		}

		return list;
	}

	private void updateStackList(TOreDictFilter filter)
	{
		if(!oreDictStacks.containsKey(filter))
		{
			oreDictStacks.put(filter, new StackData());
		}

		if(oreDictStacks.get(filter).iterStacks == null)
		{
			oreDictStacks.get(filter).iterStacks = new ArrayList<ItemStack>();
		}
		else {
			oreDictStacks.get(filter).iterStacks.clear();
		}

		List<String> keys = new ArrayList<String>();

		for(String s : OreDictionary.getOreNames())
		{
			if(filter.oreDictName.equals(s) || filter.oreDictName.equals("*"))
			{
				keys.add(s);
			}
			else if(filter.oreDictName.endsWith("*") && !filter.oreDictName.startsWith("*"))
			{
				if(s.startsWith(filter.oreDictName.substring(0, filter.oreDictName.length()-1)))
				{
					keys.add(s);
				}
			}
			else if(filter.oreDictName.startsWith("*") && !filter.oreDictName.endsWith("*"))
			{
				if(s.endsWith(filter.oreDictName.substring(1)))
				{
					keys.add(s);
				}
			}
			else if(filter.oreDictName.startsWith("*") && filter.oreDictName.endsWith("*"))
			{
				if(s.contains(filter.oreDictName.substring(1, filter.oreDictName.length()-1)))
				{
					keys.add(s);
				}
			}
		}

		for(String key : keys)
		{
			for(ItemStack stack : OreDictionary.getOres(key))
			{
				ItemStack toAdd = stack.copy();

				if(!oreDictStacks.get(filter).iterStacks.contains(stack))
				{
					oreDictStacks.get(filter).iterStacks.add(stack.copy());
				}
			}
		}

		stackSwitch = 0;
		updateScreen();
		oreDictStacks.get(filter).stackIndex = -1;
	}

	public static class StackData
	{
		public List<ItemStack> iterStacks;
		public int stackIndex;
		public ItemStack renderStack;
	}
}