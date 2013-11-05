package mekanism.client.gui;

import java.util.HashMap;
import java.util.Map;

import mekanism.api.EnumColor;
import mekanism.api.IConfigurable;
import mekanism.api.Object3D;
import mekanism.api.SideData;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.PacketHandler;
import mekanism.common.PacketHandler.Transmission;
import mekanism.common.block.BlockMachine.MachineType;
import mekanism.common.inventory.container.ContainerNull;
import mekanism.common.network.PacketConfigurationUpdate;
import mekanism.common.network.PacketConfigurationUpdate.ConfigurationPacket;
import mekanism.common.network.PacketSimpleGui;
import mekanism.common.tileentity.TileEntityContainerBlock;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

public class GuiConfiguration extends GuiMekanism
{
	public Map<Integer, GuiPos> slotPosMap = new HashMap<Integer, GuiPos>();
	
	public Map<Integer, GuiPos> inputPosMap = new HashMap<Integer, GuiPos>();
    
    public IConfigurable configurable;
    
    public GuiConfiguration(EntityPlayer player, IConfigurable tile)
    {
    	super(new ContainerNull(player, (TileEntityContainerBlock)tile));
    	
    	ySize = 95;
    	
    	configurable = tile;
    	
    	slotPosMap.put(0, new GuiPos(126, 64));
    	slotPosMap.put(1, new GuiPos(126, 34));
    	slotPosMap.put(2, new GuiPos(126, 49));
    	slotPosMap.put(3, new GuiPos(111, 64));
    	slotPosMap.put(4, new GuiPos(111, 49));
    	slotPosMap.put(5, new GuiPos(141, 49));
    	
    	inputPosMap.put(0, new GuiPos(36, 64));
    	inputPosMap.put(1, new GuiPos(36, 34));
    	inputPosMap.put(2, new GuiPos(36, 49));
    	inputPosMap.put(3, new GuiPos(21, 64));
    	inputPosMap.put(4, new GuiPos(21, 49));
    	inputPosMap.put(5, new GuiPos(51, 49));
    }
    
	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTick, int mouseX, int mouseY)
	{
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        mc.renderEngine.bindTexture(MekanismUtils.getResource(ResourceType.GUI, "GuiConfiguration.png"));
        
        int guiWidth = (width - xSize) / 2;
        int guiHeight = (height - ySize) / 2;
        
        drawTexturedModalRect(guiWidth, guiHeight, 0, 0, xSize, ySize);
        
    	int xAxis = (mouseX - (width - xSize) / 2);
		int yAxis = (mouseY - (height - ySize) / 2);
        
        if(xAxis >= 156 && xAxis <= 170 && yAxis >= 6 && yAxis <= 20)
		{
    		drawTexturedModalRect(guiWidth + 156, guiHeight + 6, 176 + 14, 0, 14, 14);
		}
		else {
			drawTexturedModalRect(guiWidth + 156, guiHeight + 6, 176 + 14, 14, 14, 14);
		}
        
        if(xAxis >= 6 && xAxis <= 20 && yAxis >= 6 && yAxis <= 20)
 		{
        	drawTexturedModalRect(guiWidth + 6, guiHeight + 6, 176 + 28, 0, 14, 14);
 		}
        else {
			drawTexturedModalRect(guiWidth + 6, guiHeight + 6, 176 + 28, 14, 14, 14);
        }
        
        for(int i = 0; i < slotPosMap.size(); i++)
        {
        	GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        	
        	int x = slotPosMap.get(i).xPos;
        	int y = slotPosMap.get(i).yPos;
        	
        	SideData data = configurable.getSideData().get(configurable.getConfiguration()[i]);
        	
        	if(data.color != EnumColor.GREY)
        	{
        		GL11.glColor4f(data.color.getColor(0), data.color.getColor(1), data.color.getColor(2), 1);
        	}
        	
        	if(xAxis >= x && xAxis <= x+14 && yAxis >= y && yAxis <= y+14)
        	{
        		drawTexturedModalRect(guiWidth + x, guiHeight + y, 176, 0, 14, 14);
        	}
        	else {
    			drawTexturedModalRect(guiWidth + x, guiHeight + y, 176, 14, 14, 14);
        	}
        }
        
        for(int i = 0; i < inputPosMap.size(); i++)
        {
        	GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        	
        	int x = inputPosMap.get(i).xPos;
        	int y = inputPosMap.get(i).yPos;
        	
        	EnumColor color = configurable.getEjector().getInputColor(ForgeDirection.getOrientation(i));
        	
        	if(color != null)
        	{
        		GL11.glColor4f(color.getColor(0), color.getColor(1), color.getColor(2), 1);
        	}
        	
        	if(xAxis >= x && xAxis <= x+14 && yAxis >= y && yAxis <= y+14)
        	{
        		drawTexturedModalRect(guiWidth + x, guiHeight + y, 176, 0, 14, 14);
        	}
        	else {
    			drawTexturedModalRect(guiWidth + x, guiHeight + y, 176, 14, 14, 14);
        	}
        }
        
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
	}
	
	@Override
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY)
	{
		super.drawGuiContainerForegroundLayer(mouseX, mouseY);
		
		int xAxis = (mouseX - (width - xSize) / 2);
		int yAxis = (mouseY - (height - ySize) / 2);
		
        String ejecting = configurable.getEjector().isEjecting() ? "On" : "Off";
        
        fontRenderer.drawString("Configuration", 60, 5, 0x404040);
        fontRenderer.drawString("Eject: " + ejecting, 53, 17, 0x00CD00);
        
        fontRenderer.drawString("Input", 32, 81, 0x787878);
        fontRenderer.drawString("Output", 72, 68, 0x787878);
        fontRenderer.drawString("Slots", 122, 81, 0x787878);
        
        if(configurable.getEjector().getOutputColor() != null)
  		{
  			GL11.glPushMatrix();
  			GL11.glColor4f(1, 1, 1, 1);
  	        GL11.glEnable(GL11.GL_LIGHTING);
  	        GL11.glEnable(GL12.GL_RESCALE_NORMAL);
  	        
  	        mc.getTextureManager().bindTexture(MekanismRenderer.getColorResource(configurable.getEjector().getOutputColor()));
  			itemRenderer.renderIcon(80, 49, MekanismRenderer.getColorIcon(configurable.getEjector().getOutputColor()), 16, 16);
  			
  			GL11.glDisable(GL11.GL_LIGHTING);
  			GL11.glPopMatrix();
  		}
        
        for(int i = 0; i < slotPosMap.size(); i++)
        {
        	int x = slotPosMap.get(i).xPos;
        	int y = slotPosMap.get(i).yPos;
        	
        	SideData data = configurable.getSideData().get(configurable.getConfiguration()[i]);
        	
        	if(xAxis >= x && xAxis <= x+14 && yAxis >= y && yAxis <= y+14)
        	{
        		drawCreativeTabHoveringText(data.color != null ? data.color.getName() : "None", xAxis, yAxis);
        	}
        }
        
        for(int i = 0; i < inputPosMap.size(); i++)
        {
        	int x = inputPosMap.get(i).xPos;
        	int y = inputPosMap.get(i).yPos;
        	
        	EnumColor color = configurable.getEjector().getInputColor(ForgeDirection.getOrientation(i));
        	
        	if(xAxis >= x && xAxis <= x+14 && yAxis >= y && yAxis <= y+14)
        	{
        		drawCreativeTabHoveringText(color != null ? color.getName() : "None", xAxis, yAxis);
        	}
        }
        
        if(xAxis >= 80 && xAxis <= 96 && yAxis >= 49 && yAxis <= 65)
		{
			if(configurable.getEjector().getOutputColor() != null)
			{
				drawCreativeTabHoveringText(configurable.getEjector().getOutputColor().getName(), xAxis, yAxis);
			}
			else {
				drawCreativeTabHoveringText("None", xAxis, yAxis);
			}
		}
	}
	
	@Override
    public void updateScreen() 
	{
		super.updateScreen();
		
		TileEntity tile = (TileEntity)configurable;
		
		if(tile == null || mc.theWorld.getBlockTileEntity(tile.xCoord, tile.yCoord, tile.zCoord) == null)
		{
			mc.displayGuiScreen(null);
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
			
			TileEntity tile = (TileEntity)configurable;
			
			if(xAxis >= 6 && xAxis <= 20 && yAxis >= 6 && yAxis <= 20)
			{
				mc.sndManager.playSoundFX("random.click", 1.0F, 1.0F);
				PacketHandler.sendPacket(Transmission.SERVER, new PacketSimpleGui().setParams(Object3D.get(tile), MachineType.getFromMetadata(tile.getBlockMetadata()).guiId));
			}
			
	        if(xAxis >= 156 && xAxis <= 170 && yAxis >= 6 && yAxis <= 20)
	        {
	        	mc.sndManager.playSoundFX("random.click", 1.0F, 1.0F);
				PacketHandler.sendPacket(Transmission.SERVER, new PacketConfigurationUpdate().setParams(ConfigurationPacket.EJECT, Object3D.get(tile)));
	        }
	        
	        if(xAxis >= 80 && xAxis <= 96 && yAxis >= 49 && yAxis <= 65)
			{
	           	mc.sndManager.playSoundFX("random.click", 1.0F, 1.0F);
				PacketHandler.sendPacket(Transmission.SERVER, new PacketConfigurationUpdate().setParams(ConfigurationPacket.EJECT_COLOR, Object3D.get(tile)));
			}
	        
	        for(int i = 0; i < slotPosMap.size(); i++)
	        {
	        	int x = slotPosMap.get(i).xPos;
	        	int y = slotPosMap.get(i).yPos;
	        	
	         	if(xAxis >= x && xAxis <= x+14 && yAxis >= y && yAxis <= y+14)
	         	{
		        	mc.sndManager.playSoundFX("random.click", 1.0F, 1.0F);
					PacketHandler.sendPacket(Transmission.SERVER, new PacketConfigurationUpdate().setParams(ConfigurationPacket.SIDE_DATA, Object3D.get(tile), i));
	         	}
	        }
	        
	        for(int i = 0; i < inputPosMap.size(); i++)
	        {
	        	int x = inputPosMap.get(i).xPos;
	        	int y = inputPosMap.get(i).yPos;
	        	
	         	if(xAxis >= x && xAxis <= x+14 && yAxis >= y && yAxis <= y+14)
	         	{
		        	mc.sndManager.playSoundFX("random.click", 1.0F, 1.0F);
					PacketHandler.sendPacket(Transmission.SERVER, new PacketConfigurationUpdate().setParams(ConfigurationPacket.INPUT_COLOR, Object3D.get(tile), i));
	         	}
	        }
		}
	}
	
	public static class GuiPos
	{
		public int xPos;
		public int yPos;
		
		public GuiPos(int x, int y)
		{
			xPos = x;
			yPos = y;
		}
	}
}
