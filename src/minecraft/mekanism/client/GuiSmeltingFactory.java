package mekanism.client;

import mekanism.api.Tier.SmeltingFactoryTier;
import mekanism.common.ContainerSmeltingFactory;
import mekanism.common.TileEntitySmeltingFactory;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.InventoryPlayer;

import org.lwjgl.opengl.GL11;

public class GuiSmeltingFactory extends GuiContainer
{
    public TileEntitySmeltingFactory tileEntity;

    public GuiSmeltingFactory(InventoryPlayer inventory, TileEntitySmeltingFactory tentity)
    {
        super(new ContainerSmeltingFactory(inventory, tentity));
        tileEntity = tentity;
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int par1, int par2)
    {
        fontRenderer.drawString(tileEntity.fullName, 34, 4, 0x404040);
        fontRenderer.drawString("Inventory", 8, (ySize - 93) + 2, 0x404040);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float par1, int par2, int par3)
    {
        int texture = mc.renderEngine.getTexture("/resources/mekanism/gui/smelting/" + tileEntity.tier.guiTexturePath);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        mc.renderEngine.bindTexture(texture);
        int guiWidth = (width - xSize) / 2;
        int guiHeight = (height - ySize) / 2;
        drawTexturedModalRect(guiWidth, guiHeight, 0, 0, xSize, ySize);
        
        int displayInt;
        
        displayInt = tileEntity.getScaledEnergyLevel(52);
        drawTexturedModalRect(guiWidth + 165, guiHeight + 17 + 52 - displayInt, 176, 52 - displayInt, 4, displayInt);
        
        if(tileEntity.tier == SmeltingFactoryTier.BASIC)
        {
        	for(int i = 0; i < tileEntity.tier.processes; i++)
        	{
        		int xAxis = 59 + (i*38);
        		
	        	displayInt = tileEntity.getScaledProgress(20, i);
	        	drawTexturedModalRect(guiWidth + xAxis, guiHeight + 33, 176, 52, 8, displayInt);
        	}
        }
        
        if(tileEntity.tier == SmeltingFactoryTier.ADVANCED)
        {
        	for(int i = 0; i < tileEntity.tier.processes; i++)
        	{
        		int xAxis = 39 + (i*26);
        		
	        	displayInt = tileEntity.getScaledProgress(20, i);
	        	drawTexturedModalRect(guiWidth + xAxis, guiHeight + 33, 176, 52, 8, displayInt);
        	}
        }
        
        if(tileEntity.tier == SmeltingFactoryTier.ULTIMATE)
        {
        	for(int i = 0; i < tileEntity.tier.processes; i++)
        	{
        		int xAxis = 33 + (i*19);
        		
	        	displayInt = tileEntity.getScaledProgress(20, i);
	        	drawTexturedModalRect(guiWidth + xAxis, guiHeight + 33, 176, 52, 8, displayInt);
        	}
        }
    }
}
