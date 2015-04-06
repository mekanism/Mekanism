package mekanism.client.gui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import mekanism.api.Coord4D;
import mekanism.api.EnumColor;
import mekanism.client.gui.element.GuiRedstoneControl;
import mekanism.client.render.MekanismRenderer;
import mekanism.client.sound.SoundHandler;
import mekanism.common.Mekanism;
import mekanism.common.OreDictCache;
import mekanism.common.content.transporter.TItemStackFilter;
import mekanism.common.content.transporter.TMaterialFilter;
import mekanism.common.content.transporter.TModIDFilter;
import mekanism.common.content.transporter.TOreDictFilter;
import mekanism.common.content.transporter.TransporterFilter;
import mekanism.common.inventory.container.ContainerNull;
import mekanism.common.network.PacketLogisticalSorterGui.LogisticalSorterGuiMessage;
import mekanism.common.network.PacketLogisticalSorterGui.SorterGuiPacket;
import mekanism.common.network.PacketTileEntity.TileEntityMessage;
import mekanism.common.tile.TileEntityLogisticalSorter;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiLogisticalSorter extends GuiMekanism
{
	public TileEntityLogisticalSorter tileEntity;

	/**
	 * True if the left mouse button was held down last time drawScreen was
	 * called.
	 */
	private boolean wasClicking;

	// Buttons
	int BUTTON_NEW = 0;

	/** Amount scrolled in filter list (0 = top, 1 = bottom) */
	public float scroll;

	/** True if the scrollbar is being dragged */
	public boolean isDragging = false;

	// Scrollbar dimensions
	private final int scrollX = 154;
	private final int scrollY = 18;

	private final int scrollW = 12;
	private final int scrollH = 138;

	// Filter dimensions
	private final int filterX = 56;
	private final int filterY = 18;

	private final int filterW = 96;
	private final int filterH = 29;

	public int dragOffset = 0;

	public int stackSwitch = 0;

	public Map<TOreDictFilter, StackData> oreDictStacks = new HashMap<TOreDictFilter, StackData>();

	public Map<TModIDFilter, StackData> modIDStacks = new HashMap<TModIDFilter, StackData>();

	public GuiLogisticalSorter(EntityPlayer player, TileEntityLogisticalSorter entity)
	{
		super(entity, new ContainerNull(player, entity));
		tileEntity = entity;

		// Set size of gui
		// xSize = 189;
		// ySize = 166;

		// Add common Mekanism gui elements
		guiElements.add(new GuiRedstoneControl(this, tileEntity, MekanismUtils.getResource(ResourceType.GUI, "GuiLogisticalSorter.png")));
	}

	public int getScroll()
	{
		// Calculate thumb position along scrollbar
		return Math.max(Math.min((int) (scroll * 123), 123), 0);
	}

	// Get index to displayed filters
	public int getFilterIndex()
	{
		if(needsScrollBars())
		{
			final int scrollSize = tileEntity.filters.size() - 4;
			return (int)((scrollSize + 0.5) * scroll);
		}
		
		return 0;
	}

	@Override
	public void updateScreen()
	{
		super.updateScreen();

		// Decrease timer for stack display rotation
		if(stackSwitch > 0)
		{
			stackSwitch--;
		}

		// Update displayed stacks
		if(stackSwitch == 0)
		{
			for(final Map.Entry<TOreDictFilter, StackData> entry : oreDictStacks.entrySet())
			{
				if(entry.getValue().iterStacks != null && entry.getValue().iterStacks.size() > 0)
				{
					if(entry.getValue().stackIndex == -1 || entry.getValue().stackIndex == entry.getValue().iterStacks.size() - 1)
					{
						entry.getValue().stackIndex = 0;
					}
					else if(entry.getValue().stackIndex < entry.getValue().iterStacks.size() - 1)
					{
						entry.getValue().stackIndex++;
					}

					entry.getValue().renderStack = entry.getValue().iterStacks.get(entry.getValue().stackIndex);
				}
			}
			
			for(final Map.Entry<TModIDFilter, StackData> entry : modIDStacks.entrySet())
			{
				if(entry.getValue().iterStacks != null && entry.getValue().iterStacks.size() > 0)
				{
					if(entry.getValue().stackIndex == -1 || entry.getValue().stackIndex == entry.getValue().iterStacks.size() - 1)
					{
						entry.getValue().stackIndex = 0;
					}
					else if(entry.getValue().stackIndex < entry.getValue().iterStacks.size() - 1)
					{
						entry.getValue().stackIndex++;
					}

					entry.getValue().renderStack = entry.getValue().iterStacks.get(entry.getValue().stackIndex);
				}
			}

			stackSwitch = 20;
		}
		else {
			for(final Map.Entry<TOreDictFilter, StackData> entry : oreDictStacks.entrySet())
			{
				if(entry.getValue().iterStacks != null && entry.getValue().iterStacks.size() == 0)
				{
					entry.getValue().renderStack = null;
				}
			}

			for(final Map.Entry<TModIDFilter, StackData> entry : modIDStacks.entrySet())
			{
				if(entry.getValue().iterStacks != null && entry.getValue().iterStacks.size() == 0)
				{
					entry.getValue().renderStack = null;
				}
			}
		}

		final Set<TOreDictFilter> oreDictFilters = new HashSet<TOreDictFilter>();
		final Set<TModIDFilter> modIDFilters = new HashSet<TModIDFilter>();

		for(int i = 0; i < 4; i++)
		{
			if(tileEntity.filters.get(getFilterIndex() + i) instanceof TOreDictFilter)
			{
				oreDictFilters.add((TOreDictFilter) tileEntity.filters.get(getFilterIndex() + i));
			}
			else if(tileEntity.filters.get(getFilterIndex() + i) instanceof TModIDFilter)
			{
				modIDFilters.add((TModIDFilter) tileEntity.filters.get(getFilterIndex() + i));
			}
		}

		for(final TransporterFilter filter : tileEntity.filters)
		{
			if(filter instanceof TOreDictFilter && !oreDictFilters.contains(filter))
			{
				if(oreDictStacks.containsKey(filter))
				{
					oreDictStacks.remove(filter);
				}
			}
			else if(filter instanceof TModIDFilter && !modIDFilters.contains(filter))
			{
				if(modIDStacks.containsKey(filter))
				{
					modIDStacks.remove(filter);
				}
			}
		}
	}

	@Override
	public void mouseClicked(int mouseX, int mouseY, int mouseBtn)
	{
		super.mouseClicked(mouseX, mouseY, mouseBtn);

		// Get mouse position relative to gui
		final int xAxis = mouseX - guiLeft;
		final int yAxis = mouseY - guiTop;

		if(mouseBtn == 0)
		{
			// Check for scrollbar interaction
			if(xAxis >= 154 && xAxis <= 166 && yAxis >= getScroll() + 18 && yAxis <= getScroll() + 18 + 15)
			{
				if(needsScrollBars())
				{
					dragOffset = yAxis - (getScroll() + 18);
					isDragging = true;
				}
				else {
					scroll = 0;
				}
			}

			// Check for filter interaction
			for(int i = 0; i < 4; i++)
			{
				if(tileEntity.filters.get(getFilterIndex() + i) != null)
				{
					final int yStart = i * 29 + 18;

					if(xAxis >= 56 && xAxis <= 152 && yAxis >= yStart && yAxis <= yStart + 29)
					{
						// Check for sorting button
						final int arrowX = filterX + filterW - 12;
						
						if(getFilterIndex() + i > 0)
						{
							if(xAxis >= arrowX && xAxis <= arrowX + 10 && yAxis >= yStart + 14 && yAxis <= yStart + 20)
							{
								// Process up button click
								final ArrayList data = new ArrayList();
								data.add(3);
								data.add(getFilterIndex() + i);

								Mekanism.packetHandler.sendToServer(new TileEntityMessage(Coord4D.get(tileEntity), data));
								SoundHandler.playSound("gui.button.press");
								
								return;
							}
						}
						
						if(getFilterIndex() + i < tileEntity.filters.size() - 1)
						{
							if(xAxis >= arrowX && xAxis <= arrowX + 10 && yAxis >= yStart + 21 && yAxis <= yStart + 27)
							{
								// Process down button click
								final ArrayList data = new ArrayList();
								data.add(4);
								data.add(getFilterIndex() + i);

								Mekanism.packetHandler.sendToServer(new TileEntityMessage(Coord4D.get(tileEntity), data));
								SoundHandler.playSound("gui.button.press");
								
								return;
							}
						}

						final TransporterFilter filter = tileEntity.filters.get(getFilterIndex() + i);

						if(filter instanceof TItemStackFilter)
						{
							SoundHandler.playSound("gui.button.press");
							Mekanism.packetHandler.sendToServer(new LogisticalSorterGuiMessage(SorterGuiPacket.SERVER_INDEX, Coord4D.get(tileEntity), 1, getFilterIndex() + i, 0));
						}
						else if(filter instanceof TOreDictFilter)
						{
							SoundHandler.playSound("gui.button.press");
							Mekanism.packetHandler.sendToServer(new LogisticalSorterGuiMessage(SorterGuiPacket.SERVER_INDEX, Coord4D.get(tileEntity), 2, getFilterIndex() + i, 0));
						}
						else if(filter instanceof TMaterialFilter)
						{
							SoundHandler.playSound("gui.button.press");
							Mekanism.packetHandler.sendToServer(new LogisticalSorterGuiMessage(SorterGuiPacket.SERVER_INDEX, Coord4D.get(tileEntity), 3, getFilterIndex() + i, 0));
						}
						else if(filter instanceof TModIDFilter)
						{
							SoundHandler.playSound("gui.button.press");
							Mekanism.packetHandler.sendToServer(new LogisticalSorterGuiMessage(SorterGuiPacket.SERVER_INDEX, Coord4D.get(tileEntity), 5, getFilterIndex() + i, 0));
						}
					}
				}
			}
	
			// Check for auto eject button
			if(xAxis >= 12 && xAxis <= 26 && yAxis >= 110 && yAxis <= 124)
			{
				final ArrayList data = new ArrayList();
				data.add(1);

				Mekanism.packetHandler.sendToServer(new TileEntityMessage(Coord4D.get(tileEntity), data));
				SoundHandler.playSound("gui.button.press");
			}

			// Check for round robin button
			if(xAxis >= 12 && xAxis <= 26 && yAxis >= 84 && yAxis <= 98)
			{
				final ArrayList data = new ArrayList();
				data.add(2);

				Mekanism.packetHandler.sendToServer(new TileEntityMessage(Coord4D.get(tileEntity), data));
				SoundHandler.playSound("gui.button.press");
			}
		}

		if(Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) && mouseBtn == 0)
		{
			mouseBtn = 2;
		}

		// Check for default colour button
		if(xAxis >= 13 && xAxis <= 29 && yAxis >= 137 && yAxis <= 153)
		{
			final ArrayList data = new ArrayList();
			data.add(0);
			data.add(mouseBtn);

			Mekanism.packetHandler.sendToServer(new TileEntityMessage(Coord4D.get(tileEntity), data));
			SoundHandler.playSound("mekanism:etc.Ding");
		}
	}

	@Override
	protected void mouseClickMove(int mouseX, int mouseY, int button, long ticks)
	{
		super.mouseClickMove(mouseX, mouseY, button, ticks);

		// Get mouse position relative to gui
		final int xAxis = mouseX - guiLeft;
		final int yAxis = mouseY - guiTop;

		if(isDragging)
		{
			scroll = Math.min(Math.max((yAxis - 18 - dragOffset) / 123F, 0), 1);
		}
	}

	@Override
	protected void mouseMovedOrUp(int mouseX, int mouseY, int type)
	{
		super.mouseMovedOrUp(mouseX, mouseY, type);

		if(type == 0 && isDragging)
		{
			dragOffset = 0;
			isDragging = false;
		}
	}

	/**
	 * Handles mouse input.
	 */
	@Override
	public void handleMouseInput()
	{
		super.handleMouseInput();
		
		int i = Mouse.getEventDWheel();

		if(i != 0 && needsScrollBars())
		{
			final int j = tileEntity.filters.size() - 4;

			if(i > 0)
			{
				i = 1;
			}

			if(i < 0)
			{
				i = -1;
			}

			scroll = (float)(scroll - (double) i / (double) j);

			if(scroll < 0.0F)
			{
				scroll = 0.0F;
			}

			if(scroll > 1.0F)
			{
				scroll = 1.0F;
			}
		}
	}

	@Override
	public void initGui()
	{
		super.initGui();

		// Add buttons to gui
		buttonList.clear();
		buttonList.add(new GuiButton(BUTTON_NEW, guiLeft + 56, guiTop + 136, 96, 20, MekanismUtils.localize("gui.newFilter")));
	}

	@Override
	protected void actionPerformed(GuiButton guibutton)
	{
		super.actionPerformed(guibutton);

		if(guibutton.id == BUTTON_NEW)
		{
			Mekanism.packetHandler.sendToServer(new LogisticalSorterGuiMessage(SorterGuiPacket.SERVER, Coord4D.get(tileEntity), 4, 0, 0));
		}
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY)
	{
		// Get mouse position relative to gui
		final int xAxis = mouseX - guiLeft;
		final int yAxis = mouseY - guiTop;

		// Write to info display
		fontRendererObj.drawString(tileEntity.getInventoryName(), 43, 6, 0x404040);

		fontRendererObj.drawString(MekanismUtils.localize("gui.filters") + ":", 11, 19, 0x00CD00);
		fontRendererObj.drawString("T: " + tileEntity.filters.size(), 11, 28, 0x00CD00);

		fontRendererObj.drawString("RR:", 12, 74, 0x00CD00);
		fontRendererObj.drawString(MekanismUtils.localize("gui." + (tileEntity.roundRobin ? "on" : "off")), 27, 86, 0x00CD00);

		fontRendererObj.drawString(MekanismUtils.localize("gui.logisticalSorter.auto") + ":", 12, 100, 0x00CD00);
		fontRendererObj.drawString(MekanismUtils.localize("gui." + (tileEntity.autoEject ? "on" : "off")), 27, 112, 0x00CD00);

		fontRendererObj.drawString(MekanismUtils.localize("gui.logisticalSorter.default") + ":", 12, 126, 0x00CD00);

		// Draw filters
		for(int i = 0; i < 4; i++)
		{
			if(tileEntity.filters.get(getFilterIndex() + i) != null)
			{
				final TransporterFilter filter = tileEntity.filters.get(getFilterIndex() + i);
				final int yStart = i * filterH + filterY;

				if(filter instanceof TItemStackFilter)
				{
					final TItemStackFilter itemFilter = (TItemStackFilter) filter;

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
					final TOreDictFilter oreFilter = (TOreDictFilter) filter;

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
						} catch(final Exception e) {}
					}

					fontRendererObj.drawString(MekanismUtils.localize("gui.oredictFilter"), 78, yStart + 2, 0x404040);
					fontRendererObj.drawString(filter.color != null ? filter.color.getName() : MekanismUtils.localize("gui.none"), 78, yStart + 11, 0x404040);
				}
				else if(filter instanceof TMaterialFilter)
				{
					final TMaterialFilter itemFilter = (TMaterialFilter) filter;

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
				else if(filter instanceof TModIDFilter)
				{
					final TModIDFilter modFilter = (TModIDFilter) filter;

					if(!modIDStacks.containsKey(modFilter))
					{
						updateStackList(modFilter);
					}

					if(modIDStacks.get(filter).renderStack != null)
					{
						try {
							GL11.glPushMatrix();
							GL11.glEnable(GL11.GL_LIGHTING);
							itemRender.renderItemAndEffectIntoGUI(fontRendererObj, mc.getTextureManager(), modIDStacks.get(filter).renderStack, 59, yStart + 3);
							GL11.glDisable(GL11.GL_LIGHTING);
							GL11.glPopMatrix();
						} catch(final Exception e) {}
					}

					fontRendererObj.drawString(MekanismUtils.localize("gui.modIDFilter"), 78, yStart + 2, 0x404040);
					fontRendererObj.drawString(filter.color != null ? filter.color.getName() : MekanismUtils.localize("gui.none"), 78, yStart + 11, 0x404040);
				}

				// Draw hovertext for sorting buttons
				final int arrowX = filterX + filterW - 12;
				
				if(getFilterIndex() + i > 0)
				{
					if(xAxis >= arrowX && xAxis <= arrowX + 10 && yAxis >= yStart + 14 && yAxis <= yStart + 20)
					{
						drawCreativeTabHoveringText(MekanismUtils.localize("gui.moveUp"), xAxis, yAxis);
					}
				}
				
				if(getFilterIndex() + i < tileEntity.filters.size() - 1)
				{
					if(xAxis >= arrowX && xAxis <= arrowX + 10 && yAxis >= yStart + 21 && yAxis <= yStart + 27)
					{
						drawCreativeTabHoveringText(MekanismUtils.localize("gui.moveDown"), xAxis, yAxis);
					}
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

		// Draw tooltips for buttons
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

		// Draw main gui background
		mc.renderEngine.bindTexture(MekanismUtils.getResource(ResourceType.GUI, "GuiLogisticalSorter.png"));
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);

		// Draw scrollbar
		drawTexturedModalRect(guiLeft + scrollX, guiTop + scrollY + getScroll(), 232 + (needsScrollBars() ? 0 : 12), 0, 12, 15);

		// Get mouse position relative to gui
		final int xAxis = mouseX - guiLeft;
		final int yAxis = mouseY - guiTop;

		// Draw filter backgrounds
		for(int i = 0; i < 4; i++)
		{
			if(tileEntity.filters.get(getFilterIndex() + i) != null)
			{
				final TransporterFilter filter = tileEntity.filters.get(getFilterIndex() + i);
				final int yStart = i * filterH + filterY;

				// Flag for mouse over this filter
				boolean mouseOver = xAxis >= filterX && xAxis <= filterX + filterW && yAxis >= yStart && yAxis <= yStart + filterH;

				// Change colour based on filter type
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
				else if(filter instanceof TModIDFilter)
				{
					MekanismRenderer.color(EnumColor.PINK, 1.0F, 2.5F);
				}

				drawTexturedModalRect(guiLeft + filterX, guiTop + yStart, mouseOver ? 0 : filterW, 166, filterW, filterH);
				MekanismRenderer.resetColor();

				// Draw sort buttons
				final int arrowX = filterX + filterW - 12;
				
				if(getFilterIndex() + i > 0)
				{
					mouseOver = xAxis >= arrowX && xAxis <= arrowX + 10 && yAxis >= yStart + 14 && yAxis <= yStart + 20;
					drawTexturedModalRect(guiLeft + arrowX, guiTop + yStart + 14, 190, mouseOver ? 143 : 115, 11, 7);
				}
				
				if(getFilterIndex() + i < tileEntity.filters.size() - 1)
				{
					mouseOver = xAxis >= arrowX && xAxis <= arrowX + 10 && yAxis >= yStart + 21 && yAxis <= yStart + 27;
					drawTexturedModalRect(guiLeft + arrowX, guiTop + yStart + 21, 190, mouseOver ? 157 : 129, 11, 7);
				}
			}
		}

		// Draw gui buttons
		if(xAxis >= 12 && xAxis <= 26 && yAxis >= 110 && yAxis <= 124)
		{
			drawTexturedModalRect(guiLeft + 12, guiTop + 110, 176, 0, 14, 14);
		}
		else {
			drawTexturedModalRect(guiLeft + 12, guiTop + 110, 176, 14, 14, 14);
		}

		if(xAxis >= 12 && xAxis <= 26 && yAxis >= 84 && yAxis <= 98)
		{
			drawTexturedModalRect(guiLeft + 12, guiTop + 84, 176 + 14, 0, 14, 14);
		}
		else {
			drawTexturedModalRect(guiLeft + 12, guiTop + 84, 176 + 14, 14, 14, 14);
		}
	}

	private void updateStackList(TOreDictFilter filter)
	{
		if(!oreDictStacks.containsKey(filter))
		{
			oreDictStacks.put(filter, new StackData());
		}

		oreDictStacks.get(filter).iterStacks = OreDictCache.getOreDictStacks(filter.oreDictName, false);

		stackSwitch = 0;
		updateScreen();
		oreDictStacks.get(filter).stackIndex = -1;
	}

	private void updateStackList(TModIDFilter filter)
	{
		if(!modIDStacks.containsKey(filter))
		{
			modIDStacks.put(filter, new StackData());
		}

		modIDStacks.get(filter).iterStacks = OreDictCache.getModIDStacks(filter.modID, false);

		stackSwitch = 0;
		updateScreen();
		modIDStacks.get(filter).stackIndex = -1;
	}

	public static class StackData
	{
		public List<ItemStack> iterStacks;

		public int stackIndex;

		public ItemStack renderStack;
	}

	/**
	 * returns true if there are more filters than can fit in the gui
	 */
	private boolean needsScrollBars()
	{
		return tileEntity.filters.size() > 4;
	}
}