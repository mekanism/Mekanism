package mekanism.client;

import java.util.ArrayList;

import mekanism.common.ContainerTeleporter;
import mekanism.common.PacketHandler;
import mekanism.common.TileEntityTeleporter;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.InventoryPlayer;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiTeleporter extends GuiContainer
{
    public TileEntityTeleporter tileEntity;

    public GuiTeleporter(InventoryPlayer inventory, TileEntityTeleporter tentity)
    {
        super(new ContainerTeleporter(inventory, tentity));
        tileEntity = tentity;
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int par1, int par2)
    {
        fontRenderer.drawString(tileEntity.fullName, 45, 6, 0x404040);
        fontRenderer.drawString("Inventory", 8, (ySize - 96) + 2, 0x404040);
        fontRenderer.drawString(tileEntity.status, 66, 19, 0x00CD00);
    }
    
	@Override
    protected void mouseClicked(int x, int y, int button)
    {
		super.mouseClicked(x, y, button);
		
		int xAxis = (x - (width - xSize) / 2);
		int yAxis = (y - (height - ySize) / 2);
		
		ArrayList data = new ArrayList();
		
		if(xAxis > 23 && xAxis < 37 && yAxis > 44 && yAxis < 58)
		{
			data.add(0);
			data.add(getIncrementedNumber(tileEntity.code.digitOne));
			
			PacketHandler.sendTileEntityPacketToServer(tileEntity, data);
			tileEntity.code.digitOne = getIncrementedNumber(tileEntity.code.digitOne);
			mc.sndManager.playSoundFX("random.click", 1.0F, 1.0F);
		}
		else if(xAxis > 62 && xAxis < 76 && yAxis > 44 && yAxis < 58)
		{
			data.add(1);
			data.add(getIncrementedNumber(tileEntity.code.digitTwo));
			
			PacketHandler.sendTileEntityPacketToServer(tileEntity, data);
			tileEntity.code.digitTwo = getIncrementedNumber(tileEntity.code.digitTwo);
			mc.sndManager.playSoundFX("random.click", 1.0F, 1.0F);
		}
		else if(xAxis > 101 && xAxis < 115 && yAxis > 44 && yAxis < 58)
		{
			data.add(2);
			data.add(getIncrementedNumber(tileEntity.code.digitThree));
			
			PacketHandler.sendTileEntityPacketToServer(tileEntity, data);
			tileEntity.code.digitThree = getIncrementedNumber(tileEntity.code.digitThree);
			mc.sndManager.playSoundFX("random.click", 1.0F, 1.0F);
		}
		else if(xAxis > 140 && xAxis < 154 && yAxis > 44 && yAxis < 58)
		{
			data.add(3);
			data.add(getIncrementedNumber(tileEntity.code.digitFour));
			
			PacketHandler.sendTileEntityPacketToServer(tileEntity, data);
			tileEntity.code.digitFour = getIncrementedNumber(tileEntity.code.digitFour);
			mc.sndManager.playSoundFX("random.click", 1.0F, 1.0F);
		}
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float par1, int par2, int par3)
    {
    	mc.renderEngine.bindTexture("/mods/mekanism/gui/GuiTeleporter.png");
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        int guiWidth = (width - xSize) / 2;
        int guiHeight = (height - ySize) / 2;
        drawTexturedModalRect(guiWidth, guiHeight, 0, 0, xSize, ySize);
        int displayInt;
        
        displayInt = tileEntity.getScaledEnergyLevel(52);
        drawTexturedModalRect(guiWidth + 165, guiHeight + 17 + 52 - displayInt, 176 + 13, 52 - displayInt, 4, displayInt);
        
        displayInt = getYAxisForNumber(tileEntity.code.digitOne);
        drawTexturedModalRect(guiWidth + 23, guiHeight + 44, 176, displayInt, 13, 13);
        
        displayInt = getYAxisForNumber(tileEntity.code.digitTwo);
        drawTexturedModalRect(guiWidth + 62, guiHeight + 44, 176, displayInt, 13, 13);
        
        displayInt = getYAxisForNumber(tileEntity.code.digitThree);
        drawTexturedModalRect(guiWidth + 101, guiHeight + 44, 176, displayInt, 13, 13);
        
        displayInt = getYAxisForNumber(tileEntity.code.digitFour);
        drawTexturedModalRect(guiWidth + 140, guiHeight + 44, 176, displayInt, 13, 13);
    }
    
    public int getIncrementedNumber(int i)
    {
    	if(i < 9) i++;
    	else if(i == 9) i=0;
    	
    	return i;
    }
    
    public int getYAxisForNumber(int i)
    {
    	return i*13;
    }
}
