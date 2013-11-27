package mekanism.client.gui;

import mekanism.api.EnumColor;
import mekanism.api.Object3D;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.PacketHandler;
import mekanism.common.PacketHandler.Transmission;
import mekanism.common.inventory.container.ContainerFilter;
import mekanism.common.network.PacketEditFilter;
import mekanism.common.network.PacketLogisticalSorterGui;
import mekanism.common.network.PacketLogisticalSorterGui.SorterGuiPacket;
import mekanism.common.network.PacketNewFilter;
import mekanism.common.tileentity.TileEntityLogisticalSorter;
import mekanism.common.transporter.TItemStackFilter;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import mekanism.common.util.TransporterUtils;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

public class GuiTItemStackFilter extends GuiMekanism
{
	public TileEntityLogisticalSorter tileEntity;
	
	public boolean isNew = false;
	
	public TItemStackFilter origFilter;
	
	public TItemStackFilter filter = new TItemStackFilter();
	
	public String status = EnumColor.DARK_GREEN + "All OK";
		
	public int ticker;
	
	private GuiTextField minField;
	private GuiTextField maxField;
	
	public GuiTItemStackFilter(EntityPlayer player, TileEntityLogisticalSorter tentity, int index)
	{
		super(new ContainerFilter(player.inventory, tentity));
		tileEntity = tentity;
		
		origFilter = (TItemStackFilter)tileEntity.filters.get(index);
		filter = ((TItemStackFilter)tileEntity.filters.get(index)).clone();
	}
	
	public GuiTItemStackFilter(EntityPlayer player, TileEntityLogisticalSorter tentity)
	{
		super(new ContainerFilter(player.inventory, tentity));
		tileEntity = tentity;
		
		isNew = true;
	}
	
	@Override
	public void initGui()
	{
		super.initGui();
		
        int guiWidth = (width - xSize) / 2;
        int guiHeight = (height - ySize) / 2;
		
		buttonList.clear();
		buttonList.add(new GuiButton(0, guiWidth + 27, guiHeight + 62, 60, 20, "Save"));
		buttonList.add(new GuiButton(1, guiWidth + 89, guiHeight + 62, 60, 20, "Delete"));
		
		if(isNew)
		{
			((GuiButton)buttonList.get(1)).enabled = false;
		}
		
		minField = new GuiTextField(fontRenderer, guiWidth + 149, guiHeight + 19, 20, 11);
		minField.setMaxStringLength(2);
		minField.setText("" + filter.min);
		
		maxField = new GuiTextField(fontRenderer, guiWidth + 149, guiHeight + 31, 20, 11);
		maxField.setMaxStringLength(2);
		maxField.setText("" + filter.max);
	}
	
	@Override
	protected void actionPerformed(GuiButton guibutton)
	{
		super.actionPerformed(guibutton);
		
		if(guibutton.id == 0)
		{
			if(filter.itemType != null && !minField.getText().isEmpty() && !maxField.getText().isEmpty())
			{
				int min = Integer.parseInt(minField.getText());
				int max = Integer.parseInt(maxField.getText());
				
				if(max >= min && max <= 64 && min <= 64)
				{
					filter.min = Integer.parseInt(minField.getText());
					filter.max = Integer.parseInt(maxField.getText());
					
					if(isNew)
					{
						PacketHandler.sendPacket(Transmission.SERVER, new PacketNewFilter().setParams(Object3D.get(tileEntity), filter));
					}
					else {
						PacketHandler.sendPacket(Transmission.SERVER, new PacketEditFilter().setParams(Object3D.get(tileEntity), false, origFilter, filter));
					}
					
					PacketHandler.sendPacket(Transmission.SERVER, new PacketLogisticalSorterGui().setParams(SorterGuiPacket.SERVER, Object3D.get(tileEntity), 0));
				}
				else if(min > max)
				{
					status = EnumColor.DARK_RED + "Max<min";
					ticker = 20;
				}
				else if(max > 64 || min > 64)
				{
					status = EnumColor.DARK_RED + "Max>64";
					ticker = 20;
				}
			}
			else if(filter.itemType == null)
			{
				status = EnumColor.DARK_RED + "No item";
				ticker = 20;
			}
			else if(minField.getText().isEmpty() || maxField.getText().isEmpty())
			{
				status = EnumColor.DARK_RED + "Max/min";
				ticker = 20;
			}
		}
		else if(guibutton.id == 1)
		{
			PacketHandler.sendPacket(Transmission.SERVER, new PacketEditFilter().setParams(Object3D.get(tileEntity), true, origFilter));
			PacketHandler.sendPacket(Transmission.SERVER, new PacketLogisticalSorterGui().setParams(SorterGuiPacket.SERVER, Object3D.get(tileEntity), 0));
		}
	}
	
	@Override
	public void keyTyped(char c, int i)
	{
		if((!minField.isFocused() && !maxField.isFocused()) || i == Keyboard.KEY_ESCAPE)
		{
			super.keyTyped(c, i);
		}
		
		if(Character.isDigit(c) || i == Keyboard.KEY_BACK || i == Keyboard.KEY_DELETE || i == Keyboard.KEY_LEFT || i == Keyboard.KEY_RIGHT)
		{
			minField.textboxKeyTyped(c, i);
			maxField.textboxKeyTyped(c, i);
		}
	}
	
	@Override
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY)
    {	
		int xAxis = (mouseX - (width - xSize) / 2);
		int yAxis = (mouseY - (height - ySize) / 2);
		
		fontRenderer.drawString((isNew ? "New" : "Edit") + " Item Filter", 43, 6, 0x404040);
		fontRenderer.drawString("Status: " + status, 35, 20, 0x00CD00);
		fontRenderer.drawString("ItemStack Details:", 35, 32, 0x00CD00);
		
		fontRenderer.drawString("Min:", 128, 20, 0x404040);
		fontRenderer.drawString("Max:", 128, 32, 0x404040);
		fontRenderer.drawString(filter.sizeMode ? "On" : "Off", 141, 46, 0x404040);
		
		if(filter.itemType != null)
		{
			fontRenderer.drawString("Item: " + filter.itemType.getDisplayName(), 35, 41, 0x00CD00);
		}
		
		if(filter.itemType != null)
		{
			GL11.glPushMatrix();
			GL11.glEnable(GL11.GL_LIGHTING);
			itemRenderer.renderItemAndEffectIntoGUI(fontRenderer, mc.getTextureManager(), filter.itemType, 12, 19);
			GL11.glDisable(GL11.GL_LIGHTING);
			GL11.glPopMatrix();
		}
		
		if(filter.color != null)
		{
			GL11.glPushMatrix();
			GL11.glColor4f(1, 1, 1, 1);
	        GL11.glEnable(GL11.GL_LIGHTING);
	        GL11.glEnable(GL12.GL_RESCALE_NORMAL);
	        
	        mc.getTextureManager().bindTexture(MekanismRenderer.getBlocksTexture());
			itemRenderer.renderIcon(12, 44, MekanismRenderer.getColorIcon(filter.color), 16, 16);
			
			GL11.glDisable(GL11.GL_LIGHTING);
			GL11.glPopMatrix();
		}
		
		if(xAxis >= 12 && xAxis <= 28 && yAxis >= 44 && yAxis <= 60)
		{
			if(filter.color != null)
			{
				drawCreativeTabHoveringText(filter.color.getName(), xAxis, yAxis);
			}
			else {
				drawCreativeTabHoveringText("None", xAxis, yAxis);
			}
		}
		
		super.drawGuiContainerForegroundLayer(mouseX, mouseY);
    }
	
	@Override
	public void updateScreen()
	{
		super.updateScreen();
		
		minField.updateCursorCounter();
		maxField.updateCursorCounter();
		
		if(ticker > 0)
		{
			ticker--;
		}
		else {
			status = EnumColor.DARK_GREEN + "All OK";
		}
	}

	@Override
    protected void drawGuiContainerBackgroundLayer(float partialTick, int mouseX, int mouseY)
    {
		super.drawGuiContainerBackgroundLayer(partialTick, mouseX, mouseY);
		
		mc.renderEngine.bindTexture(MekanismUtils.getResource(ResourceType.GUI, "GuiTItemStackFilter.png"));
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        int guiWidth = (width - xSize) / 2;
        int guiHeight = (height - ySize) / 2;
        drawTexturedModalRect(guiWidth, guiHeight, 0, 0, xSize, ySize);
        
		int xAxis = (mouseX - (width - xSize) / 2);
		int yAxis = (mouseY - (height - ySize) / 2);
		
		if(xAxis >= 5 && xAxis <= 16 && yAxis >= 5 && yAxis <= 16)
		{
			drawTexturedModalRect(guiWidth + 5, guiHeight + 5, 176, 0, 11, 11);
		}
		else {
			drawTexturedModalRect(guiWidth + 5, guiHeight + 5, 176, 11, 11, 11);
		}
		
		if(xAxis >= 128 && xAxis <= 139 && yAxis >= 44 && yAxis <= 55)
		{
			drawTexturedModalRect(guiWidth + 128, guiHeight + 44, 187, 0, 11, 11);
		}
		else {
			drawTexturedModalRect(guiWidth + 128, guiHeight + 44, 187, 11, 11, 11);
		}
		
		minField.drawTextBox();
		maxField.drawTextBox();
        
		if(xAxis >= 12 && xAxis <= 28 && yAxis >= 19 && yAxis <= 35)
		{
			GL11.glPushMatrix();
	        GL11.glDisable(GL11.GL_LIGHTING);
	        GL11.glDisable(GL11.GL_DEPTH_TEST);
	        
	        int x = guiWidth + 12;
	        int y = guiHeight + 19;
	        drawGradientRect(x, y, x + 16, y + 16, -2130706433, -2130706433);
	        
	        GL11.glEnable(GL11.GL_LIGHTING);
	        GL11.glEnable(GL11.GL_DEPTH_TEST);
	        GL11.glPopMatrix();
		}
    }
	
    @Override
    protected void mouseClicked(int mouseX, int mouseY, int button)
    {
        super.mouseClicked(mouseX, mouseY, button);
        
        minField.mouseClicked(mouseX, mouseY, button);
        maxField.mouseClicked(mouseX, mouseY, button);
        
		int xAxis = (mouseX - (width - xSize) / 2);
		int yAxis = (mouseY - (height - ySize) / 2);
        
    	if(button == 0)
		{
			if(xAxis >= 5 && xAxis <= 16 && yAxis >= 5 && yAxis <= 16)
			{
				mc.sndManager.playSoundFX("random.click", 1.0F, 1.0F);
				PacketHandler.sendPacket(Transmission.SERVER, new PacketLogisticalSorterGui().setParams(SorterGuiPacket.SERVER, Object3D.get(tileEntity), 0));
			}
			
			if(xAxis >= 12 && xAxis <= 28 && yAxis >= 19 && yAxis <= 35)
			{
				ItemStack stack = mc.thePlayer.inventory.getItemStack();
				
				if(stack != null && !Keyboard.isKeyDown(Keyboard.KEY_LSHIFT))
				{
					filter.itemType = stack.copy();
					filter.itemType.stackSize = 1;
				}
				else if(stack == null && Keyboard.isKeyDown(Keyboard.KEY_LSHIFT))
				{
					filter.itemType = null;
				}
				
	           	mc.sndManager.playSoundFX("random.click", 1.0F, 1.0F);
			}
			
			if(xAxis >= 128 && xAxis <= 139 && yAxis >= 44 && yAxis <= 55)
			{
				mc.sndManager.playSoundFX("random.click", 1.0F, 1.0F);
				filter.sizeMode = !filter.sizeMode;
			}
		}
    	
		if(xAxis >= 12 && xAxis <= 28 && yAxis >= 44 && yAxis <= 60)
		{
           	mc.sndManager.playSoundFX("mekanism:etc.Ding", 1.0F, 1.0F);
           	
           	if(button == 0)
           	{
           		filter.color = TransporterUtils.increment(filter.color);
           	}
           	else if(button == 1)
           	{
           		filter.color = TransporterUtils.decrement(filter.color);
           	}
           	else if(button == 2)
           	{
           		filter.color = null;
           	}
		}
    }
}
