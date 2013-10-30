package mekanism.client.gui;

import mekanism.api.IConfigurable;
import mekanism.api.Object3D;
import mekanism.common.PacketHandler;
import mekanism.common.PacketHandler.Transmission;
import mekanism.common.block.BlockMachine.MachineType;
import mekanism.common.network.PacketConfigurationUpdate;
import mekanism.common.network.PacketConfigurationUpdate.ConfigurationPacket;
import mekanism.common.network.PacketSimpleGui;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.tileentity.TileEntity;

import org.lwjgl.opengl.GL11;

public class GuiConfiguration extends GuiScreen
{
    public int xSize = 176;
    public int ySize = 95;
    
    public IConfigurable configurable;
    
    public GuiConfiguration(IConfigurable tile)
    {
    	configurable = tile;
    }
    
	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTick)
	{
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        mc.renderEngine.bindTexture(MekanismUtils.getResource(ResourceType.GUI, "GuiConfiguration.png"));
        
        int guiWidth = (width - xSize) / 2;
        int guiHeight = (height - ySize) / 2;
        
        drawTexturedModalRect(guiWidth, guiHeight, 0, 0, xSize, ySize);
        
    	int xAxis = (mouseX - (width - xSize) / 2);
		int yAxis = (mouseY - (height - ySize) / 2);
        
        super.drawScreen(mouseX, mouseY, partialTick);
        
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
        
        String ejecting = configurable.getEjector().isEjecting() ? "On" : "Off";
        
        fontRenderer.drawString("Configuration", guiWidth + 60, guiHeight + 5, 0x404040);
        fontRenderer.drawString("Eject: " + ejecting, guiWidth + 53, guiHeight + 15, 0x00CD00);
	}
	
	@Override
	public boolean doesGuiPauseGame()
	{
		return false;
	}
	
	@Override
    public void updateScreen() 
	{
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
		}
	}
}
