package mekanism.client.gui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import mekanism.api.Object3D;
import mekanism.common.PacketHandler;
import mekanism.common.PacketHandler.Transmission;
import mekanism.common.inventory.container.ContainerNull;
import mekanism.common.miner.MItemStackFilter;
import mekanism.common.miner.MOreDictFilter;
import mekanism.common.miner.MinerFilter;
import mekanism.common.network.PacketDigitalMinerGui;
import mekanism.common.network.PacketDigitalMinerGui.MinerGuiPacket;
import mekanism.common.network.PacketTileEntity;
import mekanism.common.tileentity.TileEntityDigitalMiner;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

public class GuiDigitalMinerConfig extends GuiMekanism
{
	public TileEntityDigitalMiner tileEntity;
	
	public boolean isDragging = false;
	
	public int dragOffset = 0;
	
	public int stackSwitch = 0;
	
	public Map<MOreDictFilter, StackData> oreDictStacks = new HashMap<MOreDictFilter, StackData>();
	
	public float scroll;
	
	private GuiTextField radiusField;
	private GuiTextField minField;
	private GuiTextField maxField;
	
	public GuiDigitalMinerConfig(EntityPlayer player, TileEntityDigitalMiner tentity)
	{
		super(new ContainerNull(player, tentity));
		tileEntity = tentity;
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
		}
		
		Set<MOreDictFilter> filtersVisible = new HashSet<MOreDictFilter>();
		
		for(int i = 0; i < 4; i++)
		{
			if(tileEntity.filters.get(getFilterIndex()+i) instanceof MOreDictFilter)
			{
				filtersVisible.add((MOreDictFilter)tileEntity.filters.get(getFilterIndex()+i));
			}
		}
		
		for(MinerFilter filter : tileEntity.filters)
		{
			if(filter instanceof MOreDictFilter && !filtersVisible.contains(filter))
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
		
		radiusField.mouseClicked(mouseX, mouseY, button);
		minField.mouseClicked(mouseX, mouseY, button);
		maxField.mouseClicked(mouseX, mouseY, button);
		
		if(button == 0)
		{
			int xAxis = (mouseX - (width - xSize) / 2);
			int yAxis = (mouseY - (height - ySize) / 2);
			
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
						MinerFilter filter = tileEntity.filters.get(getFilterIndex()+i);
						
						if(filter instanceof MItemStackFilter)
						{
							mc.sndManager.playSoundFX("random.click", 1.0F, 1.0F);
							PacketHandler.sendPacket(Transmission.SERVER, new PacketDigitalMinerGui().setParams(MinerGuiPacket.SERVER_INDEX, Object3D.get(tileEntity), 1, getFilterIndex()+i));
						}
						else if(filter instanceof MOreDictFilter)
						{
							mc.sndManager.playSoundFX("random.click", 1.0F, 1.0F);
							PacketHandler.sendPacket(Transmission.SERVER, new PacketDigitalMinerGui().setParams(MinerGuiPacket.SERVER_INDEX, Object3D.get(tileEntity), 2, getFilterIndex()+i));
						}
					}
				}
			}
			
			if(xAxis >= 5 && xAxis <= 16 && yAxis >= 5 && yAxis <= 16)
			{
				mc.sndManager.playSoundFX("random.click", 1.0F, 1.0F);
				PacketHandler.sendPacket(Transmission.SERVER, new PacketDigitalMinerGui().setParams(MinerGuiPacket.SERVER, Object3D.get(tileEntity), 3));
			}
			
			if(xAxis >= 39 && xAxis <= 50 && yAxis >= 67 && yAxis <= 78)
			{
				mc.sndManager.playSoundFX("random.click", 1.0F, 1.0F);
				setRadius();
			}
			
			if(xAxis >= 39 && xAxis <= 50 && yAxis >= 92 && yAxis <= 103)
			{
				mc.sndManager.playSoundFX("random.click", 1.0F, 1.0F);
				setMinY();
			}
			
			if(xAxis >= 39 && xAxis <= 50 && yAxis >= 117 && yAxis <= 128)
			{
				mc.sndManager.playSoundFX("random.click", 1.0F, 1.0F);
				setMaxY();
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
	
	@Override
	public void initGui()
	{
		super.initGui();
		
        int guiWidth = (width - xSize) / 2;
        int guiHeight = (height - ySize) / 2;
		
		buttonList.clear();
		buttonList.add(new GuiButton(0, guiWidth + 56, guiHeight + 136, 54, 20, "ItemStack"));
		buttonList.add(new GuiButton(1, guiWidth + 110, guiHeight + 136, 43, 20, "OreDict"));
		
		String prevRad = radiusField != null ? radiusField.getText() : "";
		String prevMin = minField != null ? minField.getText() : "";
		String prevMax = maxField != null ? maxField.getText() : "";
		
		radiusField = new GuiTextField(fontRenderer, guiWidth + 12, guiHeight + 67, 26, 11);
		radiusField.setMaxStringLength(2);
		radiusField.setText(prevRad);
		
		minField = new GuiTextField(fontRenderer, guiWidth + 12, guiHeight + 92, 26, 11);
		minField.setMaxStringLength(3);
		minField.setText(prevMin);
		
		maxField = new GuiTextField(fontRenderer, guiWidth + 12, guiHeight + 117, 26, 11);
		maxField.setMaxStringLength(3);
		maxField.setText(prevMax);
	}
	
	@Override
	protected void actionPerformed(GuiButton guibutton)
	{
		super.actionPerformed(guibutton);
		
		if(guibutton.id == 0)
		{ 
			PacketHandler.sendPacket(Transmission.SERVER, new PacketDigitalMinerGui().setParams(MinerGuiPacket.SERVER, Object3D.get(tileEntity), 1));
		}
		else if(guibutton.id == 1)
		{
			PacketHandler.sendPacket(Transmission.SERVER, new PacketDigitalMinerGui().setParams(MinerGuiPacket.SERVER, Object3D.get(tileEntity), 2));
		}
	}
	
	@Override
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY)
    {
		int xAxis = (mouseX - (width - xSize) / 2);
		int yAxis = (mouseY - (height - ySize) / 2);
		
		fontRenderer.drawString(MekanismUtils.localize("gui.digitalMinerConfig"), 43, 6, 0x404040);
		
		fontRenderer.drawString("Filters:", 11, 19, 0x00CD00);
		fontRenderer.drawString("T: " + tileEntity.filters.size(), 11, 28, 0x00CD00);
		fontRenderer.drawString("IS: " + getItemStackFilters().size(), 11, 37, 0x00CD00);
		fontRenderer.drawString("OD: " + getOreDictFilters().size(), 11, 46, 0x00CD00);
		
		fontRenderer.drawString("Radi: " + tileEntity.radius, 11, 58, 0x00CD00);
		
		fontRenderer.drawString("Min: " + tileEntity.minY, 11, 83, 0x00CD00);
		
		fontRenderer.drawString("Max: " + tileEntity.maxY, 11, 108, 0x00CD00);
		
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
						itemRenderer.renderItemAndEffectIntoGUI(fontRenderer, mc.getTextureManager(), itemFilter.itemType, 59, yStart + 3);
						GL11.glDisable(GL11.GL_LIGHTING);
						GL11.glPopMatrix();
					}
					
					fontRenderer.drawString("Item Filter", 78, yStart + 2, 0x404040);
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
						GL11.glPushMatrix();
						GL11.glEnable(GL11.GL_LIGHTING);
						itemRenderer.renderItemAndEffectIntoGUI(fontRenderer, mc.getTextureManager(), oreDictStacks.get(filter).renderStack, 59, yStart + 3);
						GL11.glDisable(GL11.GL_LIGHTING);
						GL11.glPopMatrix();
					}
					
					fontRenderer.drawString("OreDict Filter", 78, yStart + 2, 0x404040);
				}
			}
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
        
		drawTexturedModalRect(guiWidth + 154, guiHeight + 18 + getScroll(), 232, 0, 12, 15);
		
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
					drawTexturedModalRect(guiWidth + 56, guiHeight + yStart, mouseOver ? 0 : 96, 166, 96, 29);
				}
				else if(filter instanceof MOreDictFilter)
				{
					drawTexturedModalRect(guiWidth + 56, guiHeight + yStart, mouseOver ? 0 : 96, 195, 96, 29);
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
			
			PacketHandler.sendPacket(Transmission.SERVER, new PacketTileEntity().setParams(Object3D.get(tileEntity), data));
			
			radiusField.setText("");
		}
	}
	
	private void setMinY()
	{
		if(!minField.getText().isEmpty())
		{
			int toUse = Math.max(0, Math.min(Integer.parseInt(minField.getText()), 255));
			
			ArrayList data = new ArrayList();
			data.add(7);
			data.add(toUse);
			
			PacketHandler.sendPacket(Transmission.SERVER, new PacketTileEntity().setParams(Object3D.get(tileEntity), data));
			
			minField.setText("");
		}
	}
	
	private void setMaxY()
	{
		if(!maxField.getText().isEmpty())
		{
			int toUse = Math.max(0, Math.min(Integer.parseInt(maxField.getText()), 255));
			
			ArrayList data = new ArrayList();
			data.add(8);
			data.add(toUse);
			
			PacketHandler.sendPacket(Transmission.SERVER, new PacketTileEntity().setParams(Object3D.get(tileEntity), data));
			
			maxField.setText("");
		}
	}
	
	public ArrayList getItemStackFilters()
	{
		ArrayList list = new ArrayList();
		
		for(MinerFilter filter : tileEntity.filters)
		{
			if(filter instanceof MItemStackFilter)
			{
				list.add(filter);
			}
		}
		
		return list;
	}
	
	public ArrayList getOreDictFilters()
	{
		ArrayList list = new ArrayList();
		
		for(MinerFilter filter : tileEntity.filters)
		{
			if(filter instanceof MOreDictFilter)
			{
				list.add(filter);
			}
		}
		
		return list;
	}
	
	private void updateStackList(MOreDictFilter filter)
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
    			
    			if(!oreDictStacks.get(filter).iterStacks.contains(stack) && toAdd.getItem() instanceof ItemBlock)
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
