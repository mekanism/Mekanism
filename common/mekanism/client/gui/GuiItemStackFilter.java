package mekanism.client.gui;

import mekanism.api.EnumColor;
import mekanism.api.Object3D;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.Mekanism;
import mekanism.common.PacketHandler;
import mekanism.common.PacketHandler.Transmission;
import mekanism.common.inventory.container.ContainerFilter;
import mekanism.common.network.PacketLogisticalSorterGui;
import mekanism.common.network.PacketNewFilter;
import mekanism.common.network.PacketRobit;
import mekanism.common.network.PacketRobit.RobitPacketType;
import mekanism.common.tileentity.TileEntityLogisticalSorter;
import mekanism.common.transporter.ItemStackFilter;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import mekanism.common.util.TransporterUtils;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

public class GuiItemStackFilter extends GuiMekanism
{
	public TileEntityLogisticalSorter tileEntity;
	
	public boolean isNew = false;
	
	public ItemStackFilter filter = new ItemStackFilter();
	
	public String status = EnumColor.DARK_GREEN + "All OK";
	
	public int ticker;
	
	public GuiItemStackFilter(EntityPlayer player, TileEntityLogisticalSorter tentity, int index)
	{
		super(new ContainerFilter(player.inventory));
		tileEntity = tentity;
		
		filter = (ItemStackFilter)tileEntity.filters.get(index);
	}
	
	public GuiItemStackFilter(EntityPlayer player, TileEntityLogisticalSorter tentity)
	{
		super(new ContainerFilter(player.inventory));
		tileEntity = tentity;
		
		isNew = true;
		
		filter.color = TransporterUtils.colors.get(0);
	}
	
	@Override
	public void initGui()
	{
		super.initGui();
		
        int guiWidth = (width - xSize) / 2;
        int guiHeight = (height - ySize) / 2;
		
		buttonList.clear();
		buttonList.add(new GuiButton(0, guiWidth + 58, guiHeight + 63, 60, 18, "Save"));
	}
	
	@Override
	protected void actionPerformed(GuiButton guibutton)
	{
		super.actionPerformed(guibutton);
		
		if(guibutton.id == 0)
		{
			if(filter.itemType != null)
			{
				PacketHandler.sendPacket(Transmission.SERVER, new PacketNewFilter().setParams(Object3D.get(tileEntity), filter));
				PacketHandler.sendPacket(Transmission.SERVER, new PacketLogisticalSorterGui().setParams(Object3D.get(tileEntity), 0));
				mc.thePlayer.openGui(Mekanism.instance, 26, mc.theWorld, tileEntity.xCoord, tileEntity.yCoord, tileEntity.zCoord);
			}
			else {
				status = EnumColor.DARK_RED + "No item";
				ticker = 20;
			}
		}
	}
	
	@Override
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY)
    {	
		int xAxis = (mouseX - (width - xSize) / 2);
		int yAxis = (mouseY - (height - ySize) / 2);
		
		fontRenderer.drawString((isNew ? "New" : "Edit") + " ItemStack Filter", 43, 6, 0x404040);
		fontRenderer.drawString("Status: " + status, 35, 20, 0x00CD00);
		fontRenderer.drawString("ItemStack Details:", 35, 32, 0x00CD00);
		
		if(filter.itemType != null)
		{
			fontRenderer.drawString("ID: " + filter.itemType.itemID + ", meta: " + filter.itemType.getItemDamage(), 35, 41, 0x00CD00);
		}
		
		if(filter.itemType != null)
		{
			GL11.glPushMatrix();
			GL11.glEnable(GL11.GL_LIGHTING);
			itemRenderer.renderItemAndEffectIntoGUI(fontRenderer, mc.getTextureManager(), filter.itemType, 12, 19);
			GL11.glDisable(GL11.GL_LIGHTING);
			GL11.glPopMatrix();
		}
		
		GL11.glPushMatrix();
		GL11.glColor4f(1, 1, 1, 1);
        GL11.glEnable(GL11.GL_LIGHTING);
        GL11.glEnable(GL12.GL_RESCALE_NORMAL);
        
        mc.getTextureManager().bindTexture(MekanismRenderer.getColorResource(filter.color));
		itemRenderer.renderIcon(12, 45, MekanismRenderer.getColorIcon(filter.color), 16, 16);
		
		GL11.glDisable(GL11.GL_LIGHTING);
		GL11.glPopMatrix();
		
		if(xAxis >= 12 && xAxis <= 28 && yAxis >= 45 && yAxis <= 61)
		{
			drawCreativeTabHoveringText(filter.color.getName(), xAxis, yAxis);
		}
		
		super.drawGuiContainerForegroundLayer(mouseX, mouseY);
    }
	
	@Override
	public void updateScreen()
	{
		super.updateScreen();
		
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
		
		mc.renderEngine.bindTexture(MekanismUtils.getResource(ResourceType.GUI, "GuiItemStackFilter.png"));
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
        
    	if(button == 0)
		{
			int xAxis = (mouseX - (width - xSize) / 2);
			int yAxis = (mouseY - (height - ySize) / 2);
			
			if(xAxis >= 5 && xAxis <= 16 && yAxis >= 5 && yAxis <= 16)
			{
				mc.sndManager.playSoundFX("random.click", 1.0F, 1.0F);
				PacketHandler.sendPacket(Transmission.SERVER, new PacketLogisticalSorterGui().setParams(Object3D.get(tileEntity), 0));
				mc.thePlayer.openGui(Mekanism.instance, 26, mc.theWorld, tileEntity.xCoord, tileEntity.yCoord, tileEntity.zCoord);
			}
			
			if(xAxis >= 12 && xAxis <= 28 && yAxis >= 19 && yAxis <= 35)
			{
				ItemStack stack = mc.thePlayer.inventory.getItemStack();
				
				if(stack != null)
				{
					filter.itemType = stack.copy();
					filter.itemType.stackSize = 1;
				}
			}
			
			if(xAxis >= 12 && xAxis <= 28 && yAxis >= 45 && yAxis <= 61)
			{
				filter.color = TransporterUtils.increment(filter.color);
			}
		}
    }
}
