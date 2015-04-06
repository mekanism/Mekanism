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
import mekanism.common.OreDictCache;
import mekanism.common.content.miner.MItemStackFilter;
import mekanism.common.content.miner.MMaterialFilter;
import mekanism.common.content.miner.MModIDFilter;
import mekanism.common.content.miner.MOreDictFilter;
import mekanism.common.content.miner.MinerFilter;
import mekanism.common.inventory.container.ContainerNull;
import mekanism.common.network.PacketDigitalMinerGui.DigitalMinerGuiMessage;
import mekanism.common.network.PacketDigitalMinerGui.MinerGuiPacket;
import mekanism.common.network.PacketTileEntity.TileEntityMessage;
import mekanism.common.tile.TileEntityDigitalMiner;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

@SideOnly(Side.CLIENT)
public class GuiDigitalMinerConfig extends GuiMekanism
{
	public TileEntityDigitalMiner tileEntity;

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

	public Map<MOreDictFilter, StackData> oreDictStacks = new HashMap<MOreDictFilter, StackData>();
	public Map<MModIDFilter, StackData> modIDStacks = new HashMap<MModIDFilter, StackData>();

	public float scroll;

	private GuiTextField radiusField;
	private GuiTextField minField;
	private GuiTextField maxField;

	public GuiDigitalMinerConfig(EntityPlayer player, TileEntityDigitalMiner tentity)
	{
		super(tentity, new ContainerNull(player, tentity));
		tileEntity = tentity;
	}

	public int getScroll()
	{
		return Math.max(Math.min((int)(scroll*123), 123), 0);
	}

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

		radiusField.updateCursorCounter();
		minField.updateCursorCounter();
		maxField.updateCursorCounter();

		if(stackSwitch > 0)
		{
			stackSwitch--;
		}

		if(stackSwitch == 0)
		{
			for(Map.Entry<MOreDictFilter, StackData> entry : oreDictStacks.entrySet())
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
			
			for(Map.Entry<MModIDFilter, StackData> entry : modIDStacks.entrySet())
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
			for(Map.Entry<MOreDictFilter, StackData> entry : oreDictStacks.entrySet())
			{
				if(entry.getValue().iterStacks != null && entry.getValue().iterStacks.size() == 0)
				{
					entry.getValue().renderStack = null;
				}
			}
			
			for(Map.Entry<MModIDFilter, StackData> entry : modIDStacks.entrySet())
			{
				if(entry.getValue().iterStacks != null && entry.getValue().iterStacks.size() == 0)
				{
					entry.getValue().renderStack = null;
				}
			}
		}

		Set<MOreDictFilter> oreDictFilters = new HashSet<MOreDictFilter>();
		Set<MModIDFilter> modIDFilters = new HashSet<MModIDFilter>();

		for(int i = 0; i < 4; i++)
		{
			if(tileEntity.filters.get(getFilterIndex()+i) instanceof MOreDictFilter)
			{
				oreDictFilters.add((MOreDictFilter)tileEntity.filters.get(getFilterIndex()+i));
			}
			else if(tileEntity.filters.get(getFilterIndex()+i) instanceof MModIDFilter)
			{
				modIDFilters.add((MModIDFilter)tileEntity.filters.get(getFilterIndex()+i));
			}
		}

		for(MinerFilter filter : tileEntity.filters)
		{
			if(filter instanceof MOreDictFilter && !oreDictFilters.contains(filter))
			{
				if(oreDictStacks.containsKey(filter))
				{
					oreDictStacks.remove(filter);
				}
			}
			else if(filter instanceof MModIDFilter && !modIDFilters.contains(filter))
			{
				if(modIDStacks.containsKey(filter))
				{
					modIDStacks.remove(filter);
				}
			}
		}
	}

	@Override
	public void mouseClicked(int mouseX, int mouseY, int button)
	{
		super.mouseClicked(mouseX, mouseY, button);

		radiusField.mouseClicked(mouseX, mouseY, button);
		minField.mouseClicked(mouseX, mouseY, button);
		maxField.mouseClicked(mouseX, mouseY, button);

		if(button == 0)
		{
			int xAxis = (mouseX - (width - xSize) / 2);
			int yAxis = (mouseY - (height - ySize) / 2);

			if(xAxis >= 154 && xAxis <= 166 && yAxis >= getScroll()+18 && yAxis <= getScroll()+18+15)
			{
				if(needsScrollBars())
				{
					dragOffset = yAxis - (getScroll()+18);
					isDragging = true;
				}
				else {
					scroll = 0;
				}
			}

			for(int i = 0; i < 4; i++)
			{
				if(tileEntity.filters.get(getFilterIndex()+i) != null)
				{
					int yStart = i*29 + 18;

					// Check for sorting button
					final int arrowX = filterX + filterW - 12;
					
					if(getFilterIndex() + i > 0)
					{
						if(xAxis >= arrowX && xAxis <= arrowX + 10 && yAxis >= yStart + 14 && yAxis <= yStart + 20)
						{
							// Process up button click
							final ArrayList data = new ArrayList();
							data.add(11);
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
							data.add(12);
							data.add(getFilterIndex() + i);

							Mekanism.packetHandler.sendToServer(new TileEntityMessage(Coord4D.get(tileEntity), data));
							SoundHandler.playSound("gui.button.press");
							
							return;
						}
					}

					if(xAxis >= 56 && xAxis <= 152 && yAxis >= yStart && yAxis <= yStart+29)
					{
						MinerFilter filter = tileEntity.filters.get(getFilterIndex()+i);

						if(filter instanceof MItemStackFilter)
						{
	                        SoundHandler.playSound("gui.button.press");
							Mekanism.packetHandler.sendToServer(new DigitalMinerGuiMessage(MinerGuiPacket.SERVER_INDEX, Coord4D.get(tileEntity), 1, getFilterIndex()+i, 0));
						}
						else if(filter instanceof MOreDictFilter)
						{
	                        SoundHandler.playSound("gui.button.press");
							Mekanism.packetHandler.sendToServer(new DigitalMinerGuiMessage(MinerGuiPacket.SERVER_INDEX, Coord4D.get(tileEntity), 2, getFilterIndex()+i, 0));
						}
						else if(filter instanceof MMaterialFilter)
						{
	                        SoundHandler.playSound("gui.button.press");
							Mekanism.packetHandler.sendToServer(new DigitalMinerGuiMessage(MinerGuiPacket.SERVER_INDEX, Coord4D.get(tileEntity), 3, getFilterIndex()+i, 0));
						}
						else if(filter instanceof MModIDFilter)
						{
	                        SoundHandler.playSound("gui.button.press");
							Mekanism.packetHandler.sendToServer(new DigitalMinerGuiMessage(MinerGuiPacket.SERVER_INDEX, Coord4D.get(tileEntity), 6, getFilterIndex()+i, 0));
						}
					}
				}
			}

			if(xAxis >= 5 && xAxis <= 16 && yAxis >= 5 && yAxis <= 16)
			{
                SoundHandler.playSound("gui.button.press");
				Mekanism.packetHandler.sendToServer(new DigitalMinerGuiMessage(MinerGuiPacket.SERVER, Coord4D.get(tileEntity), 4, 0, 0));
			}

			if(xAxis >= 39 && xAxis <= 50 && yAxis >= 67 && yAxis <= 78)
			{
                SoundHandler.playSound("gui.button.press");
				setRadius();
			}

			if(xAxis >= 39 && xAxis <= 50 && yAxis >= 92 && yAxis <= 103)
			{
                SoundHandler.playSound("gui.button.press");
				setMinY();
			}

			if(xAxis >= 39 && xAxis <= 50 && yAxis >= 117 && yAxis <= 128)
			{
                SoundHandler.playSound("gui.button.press");
				setMaxY();
			}

			if(xAxis >= 11 && xAxis <= 25 && yAxis >= 141 && yAxis <= 155)
			{
				ArrayList data = new ArrayList();
				data.add(10);

				Mekanism.packetHandler.sendToServer(new TileEntityMessage(Coord4D.get(tileEntity), data));
				SoundHandler.playSound("gui.button.press");
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

		int guiWidth = (width - xSize) / 2;
		int guiHeight = (height - ySize) / 2;

		buttonList.clear();
		buttonList.add(new GuiButton(0, guiWidth + 56, guiHeight + 136, 96, 20, MekanismUtils.localize("gui.newFilter")));

		String prevRad = radiusField != null ? radiusField.getText() : "";
		String prevMin = minField != null ? minField.getText() : "";
		String prevMax = maxField != null ? maxField.getText() : "";

		radiusField = new GuiTextField(fontRendererObj, guiWidth + 12, guiHeight + 67, 26, 11);
		radiusField.setMaxStringLength(2);
		radiusField.setText(prevRad);

		minField = new GuiTextField(fontRendererObj, guiWidth + 12, guiHeight + 92, 26, 11);
		minField.setMaxStringLength(3);
		minField.setText(prevMin);

		maxField = new GuiTextField(fontRendererObj, guiWidth + 12, guiHeight + 117, 26, 11);
		maxField.setMaxStringLength(3);
		maxField.setText(prevMax);
	}

	@Override
	protected void actionPerformed(GuiButton guibutton)
	{
		super.actionPerformed(guibutton);

		if(guibutton.id == 0)
		{
			Mekanism.packetHandler.sendToServer(new DigitalMinerGuiMessage(MinerGuiPacket.SERVER, Coord4D.get(tileEntity), 5, 0, 0));
		}
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY)
	{
		int xAxis = (mouseX - (width - xSize) / 2);
		int yAxis = (mouseY - (height - ySize) / 2);

		fontRendererObj.drawString(MekanismUtils.localize("gui.digitalMinerConfig"), 43, 6, 0x404040);

		fontRendererObj.drawString(MekanismUtils.localize("gui.filters") + ":", 11, 19, 0x00CD00);
		fontRendererObj.drawString("T: " + tileEntity.filters.size(), 11, 28, 0x00CD00);
		
		fontRendererObj.drawString("I: " + (tileEntity.inverse ? MekanismUtils.localize("gui.on") : MekanismUtils.localize("gui.off")), 11, 131, 0x00CD00);

		fontRendererObj.drawString("Radi: " + tileEntity.radius, 11, 58, 0x00CD00);

		fontRendererObj.drawString("Min: " + tileEntity.minY, 11, 83, 0x00CD00);

		fontRendererObj.drawString("Max: " + tileEntity.maxY, 11, 108, 0x00CD00);

		for(int i = 0; i < 4; i++)
		{
			if(tileEntity.filters.get(getFilterIndex()+i) != null)
			{
				MinerFilter filter = tileEntity.filters.get(getFilterIndex()+i);
				int yStart = i*29 + 18;

				if(filter instanceof MItemStackFilter)
				{
					MItemStackFilter itemFilter = (MItemStackFilter)filter;

					if(itemFilter.itemType != null)
					{
						GL11.glPushMatrix();
						GL11.glEnable(GL11.GL_LIGHTING);
						itemRender.renderItemAndEffectIntoGUI(fontRendererObj, mc.getTextureManager(), itemFilter.itemType, 59, yStart + 3);
						GL11.glDisable(GL11.GL_LIGHTING);
						GL11.glPopMatrix();
					}

					fontRendererObj.drawString(MekanismUtils.localize("gui.itemFilter"), 78, yStart + 2, 0x404040);
				}
				else if(filter instanceof MOreDictFilter)
				{
					MOreDictFilter oreFilter = (MOreDictFilter)filter;

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
				}
				else if(filter instanceof MMaterialFilter)
				{
					MMaterialFilter itemFilter = (MMaterialFilter)filter;

					if(itemFilter.materialItem != null)
					{
						GL11.glPushMatrix();
						GL11.glEnable(GL11.GL_LIGHTING);
						itemRender.renderItemAndEffectIntoGUI(fontRendererObj, mc.getTextureManager(), itemFilter.materialItem, 59, yStart + 3);
						GL11.glDisable(GL11.GL_LIGHTING);
						GL11.glPopMatrix();
					}

					fontRendererObj.drawString(MekanismUtils.localize("gui.materialFilter"), 78, yStart + 2, 0x404040);
				}
				else if(filter instanceof MModIDFilter)
				{
					MModIDFilter modFilter = (MModIDFilter)filter;

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
						} catch(Exception e) {}
					}

					fontRendererObj.drawString(MekanismUtils.localize("gui.modIDFilter"), 78, yStart + 2, 0x404040);
				}
			}
		}

		if(xAxis >= 11 && xAxis <= 25 && yAxis >= 141 && yAxis <= 155)
		{
			drawCreativeTabHoveringText(MekanismUtils.localize("gui.digitalMiner.inverse"), xAxis, yAxis);
		}

		super.drawGuiContainerForegroundLayer(mouseX, mouseY);
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTick, int mouseX, int mouseY)
	{
		super.drawGuiContainerBackgroundLayer(partialTick, mouseX, mouseY);

		mc.renderEngine.bindTexture(MekanismUtils.getResource(ResourceType.GUI, "GuiDigitalMinerConfig.png"));
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		int guiWidth = (width - xSize) / 2;
		int guiHeight = (height - ySize) / 2;
		drawTexturedModalRect(guiWidth, guiHeight, 0, 0, xSize, ySize);

		drawTexturedModalRect( guiLeft + scrollX, guiTop + scrollY + getScroll(), 232 + ( needsScrollBars() ? 0 : 12 ), 0, 12, 15 );

		int xAxis = (mouseX - (width - xSize) / 2);
		int yAxis = (mouseY - (height - ySize) / 2);

		for(int i = 0; i < 4; i++)
		{
			if(tileEntity.filters.get(getFilterIndex()+i) != null)
			{
				MinerFilter filter = tileEntity.filters.get(getFilterIndex()+i);
				int yStart = i*29 + 18;

				boolean mouseOver = xAxis >= 56 && xAxis <= 152 && yAxis >= yStart && yAxis <= yStart+29;

				if(filter instanceof MItemStackFilter)
				{
					MekanismRenderer.color(EnumColor.INDIGO, 1.0F, 2.5F);
				}
				else if(filter instanceof MOreDictFilter)
				{
					MekanismRenderer.color(EnumColor.BRIGHT_GREEN, 1.0F, 2.5F);
				}
				else if(filter instanceof MMaterialFilter)
				{
					MekanismRenderer.color(EnumColor.PURPLE, 1.0F, 4F);
				}
				else if(filter instanceof MModIDFilter)
				{
					MekanismRenderer.color(EnumColor.PINK, 1.0F, 2.5F);
				}
				
				drawTexturedModalRect(guiWidth + 56, guiHeight + yStart, mouseOver ? 0 : 96, 166, 96, 29);
				MekanismRenderer.resetColor();

				// Draw sort buttons
				final int arrowX = filterX + filterW - 12;
				if( getFilterIndex() + i > 0 )
				{
					mouseOver = xAxis >= arrowX && xAxis <= arrowX + 10 && yAxis >= yStart + 14 && yAxis <= yStart + 20;
					drawTexturedModalRect( guiLeft + arrowX, guiTop + yStart + 14, 190, mouseOver ? 143 : 115, 11, 7 );
				}
				if( getFilterIndex() + i < tileEntity.filters.size() - 1 )
				{
					mouseOver = xAxis >= arrowX && xAxis <= arrowX + 10 && yAxis >= yStart + 21 && yAxis <= yStart + 27;
					drawTexturedModalRect( guiLeft + arrowX, guiTop + yStart + 21, 190, mouseOver ? 157 : 129, 11, 7 );
				}
			}
		}

		if(xAxis >= 5 && xAxis <= 16 && yAxis >= 5 && yAxis <= 16)
		{
			drawTexturedModalRect(guiWidth + 5, guiHeight + 5, 176, 0, 11, 11);
		}
		else {
			drawTexturedModalRect(guiWidth + 5, guiHeight + 5, 176, 11, 11, 11);
		}

		if(xAxis >= 39 && xAxis <= 50 && yAxis >= 67 && yAxis <= 78)
		{
			drawTexturedModalRect(guiWidth + 39, guiHeight + 67, 176 + 11, 0, 11, 11);
		}
		else {
			drawTexturedModalRect(guiWidth + 39, guiHeight + 67, 176 + 11, 11, 11, 11);
		}

		if(xAxis >= 39 && xAxis <= 50 && yAxis >= 92 && yAxis <= 103)
		{
			drawTexturedModalRect(guiWidth + 39, guiHeight + 92, 176 + 11, 0, 11, 11);
		}
		else {
			drawTexturedModalRect(guiWidth + 39, guiHeight + 92, 176 + 11, 11, 11, 11);
		}

		if(xAxis >= 39 && xAxis <= 50 && yAxis >= 117 && yAxis <= 128)
		{
			drawTexturedModalRect(guiWidth + 39, guiHeight + 117, 176 + 11, 0, 11, 11);
		}
		else {
			drawTexturedModalRect(guiWidth + 39, guiHeight + 117, 176 + 11, 11, 11, 11);
		}

		if(xAxis >= 11 && xAxis <= 25 && yAxis >= 141 && yAxis <= 155)
		{
			drawTexturedModalRect(guiWidth + 11, guiHeight + 141, 176 + 22, 0, 14, 14);
		}
		else {
			drawTexturedModalRect(guiWidth + 11, guiHeight + 141, 176 + 22, 14, 14, 14);
		}

		radiusField.drawTextBox();
		minField.drawTextBox();
		maxField.drawTextBox();
	}

	@Override
	public void keyTyped(char c, int i)
	{
		if((!radiusField.isFocused() && !minField.isFocused() && !maxField.isFocused()) || i == Keyboard.KEY_ESCAPE)
		{
			super.keyTyped(c, i);
		}

		if(i == Keyboard.KEY_RETURN)
		{
			if(radiusField.isFocused())
			{
				setRadius();
			}
			else if(minField.isFocused())
			{
				setMinY();
			}
			else if(maxField.isFocused())
			{
				setMaxY();
			}
		}

		if(Character.isDigit(c) || i == Keyboard.KEY_BACK || i == Keyboard.KEY_DELETE || i == Keyboard.KEY_LEFT || i == Keyboard.KEY_RIGHT)
		{
			radiusField.textboxKeyTyped(c, i);
			minField.textboxKeyTyped(c, i);
			maxField.textboxKeyTyped(c, i);
		}
	}

	private void setRadius()
	{
		if(!radiusField.getText().isEmpty())
		{
			int toUse = Math.max(0, Math.min(Integer.parseInt(radiusField.getText()), 32));

			ArrayList data = new ArrayList();
			data.add(6);
			data.add(toUse);

			Mekanism.packetHandler.sendToServer(new TileEntityMessage(Coord4D.get(tileEntity), data));

			radiusField.setText("");
		}
	}

	private void setMinY()
	{
		if(!minField.getText().isEmpty())
		{
			int toUse = Math.max(0, Math.min(Integer.parseInt(minField.getText()), tileEntity.maxY));

			ArrayList data = new ArrayList();
			data.add(7);
			data.add(toUse);

			Mekanism.packetHandler.sendToServer(new TileEntityMessage(Coord4D.get(tileEntity), data));

			minField.setText("");
		}
	}

	private void setMaxY()
	{
		if(!maxField.getText().isEmpty())
		{
			int toUse = Math.max(tileEntity.minY, Math.min(Integer.parseInt(maxField.getText()), 255));

			ArrayList data = new ArrayList();
			data.add(8);
			data.add(toUse);

			Mekanism.packetHandler.sendToServer(new TileEntityMessage(Coord4D.get(tileEntity), data));

			maxField.setText("");
		}
	}

	private void updateStackList(MOreDictFilter filter)
	{
		if(!oreDictStacks.containsKey(filter))
		{
			oreDictStacks.put(filter, new StackData());
		}
		
		oreDictStacks.get(filter).iterStacks = OreDictCache.getOreDictStacks(filter.oreDictName, true);

		stackSwitch = 0;
		updateScreen();
		oreDictStacks.get(filter).stackIndex = -1;
	}
	
	private void updateStackList(MModIDFilter filter)
	{
		if(!modIDStacks.containsKey(filter))
		{
			modIDStacks.put(filter, new StackData());
		}
		
		modIDStacks.get(filter).iterStacks = OreDictCache.getModIDStacks(filter.modID, true);

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
