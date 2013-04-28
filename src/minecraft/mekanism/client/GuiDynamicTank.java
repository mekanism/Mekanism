package mekanism.client;

import mekanism.common.ContainerDynamicTank;
import mekanism.common.TileEntityDynamicTank;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraftforge.liquids.LiquidDictionary;
import net.minecraftforge.liquids.LiquidStack;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiDynamicTank extends GuiContainer
{
	public TileEntityDynamicTank tileEntity;
	
	private int guiWidth;
	private int guiHeight;
	
	public GuiDynamicTank(InventoryPlayer inventory, TileEntityDynamicTank tentity)
    {
        super(new ContainerDynamicTank(inventory, tentity));
        tileEntity = tentity;
    }
	
	@Override
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY)
    {
		int xAxis = (mouseX - (width - xSize) / 2);
		int yAxis = (mouseY - (height - ySize) / 2);
		
        fontRenderer.drawString(tileEntity.fullName, 45, 6, 0x404040);
        fontRenderer.drawString("Inventory", 8, (ySize - 94) + 2, 0x404040);
        fontRenderer.drawString("Volume: " + tileEntity.clientCapacity/16000, 53, 26, 0x00CD00);
        fontRenderer.drawString(tileEntity.structure.liquidStored != null ? LiquidDictionary.findLiquidName(tileEntity.structure.liquidStored) + ": " + tileEntity.structure.liquidStored.amount : "No liquid.", 53, 35, 0x00CD00);
        
        
		if(xAxis >= 7 && xAxis <= 39 && yAxis >= 14 && yAxis <= 72)
		{
			drawCreativeTabHoveringText(tileEntity.structure.liquidStored != null ? LiquidDictionary.findLiquidName(tileEntity.structure.liquidStored) + ": " + tileEntity.structure.liquidStored.amount + "mB" : "Empty", xAxis, yAxis);
		}
    }

	@Override
    protected void drawGuiContainerBackgroundLayer(float par1, int par2, int par3)
    {
		mc.renderEngine.bindTexture("/mods/mekanism/gui/GuiDynamicTank.png");
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        guiWidth = (width - xSize) / 2;
        guiHeight = (height - ySize) / 2;
        drawTexturedModalRect(guiWidth, guiHeight, 0, 0, xSize, ySize);
        
        if(tileEntity.getScaledLiquidLevel(58) > 0) 
        {
			displayGauge(guiWidth, guiHeight, 7, 14, tileEntity.getScaledLiquidLevel(58), tileEntity.structure.liquidStored, 0);
			displayGauge(guiWidth, guiHeight, 23, 14, tileEntity.getScaledLiquidLevel(58), tileEntity.structure.liquidStored, 1);
		}
    }
	
	/*
	 * Credit to BuildCraft for both the gauge texture and parts of the code.
	 */
	public void displayGauge(int width, int height, int xPos, int yPos, int scale, LiquidStack liquid, int side /*0-left, 1-right*/)
	{
	    if(liquid == null)
	    {
	        return;
	    }
	    
		int start = 0;

		while(true)
		{
			int renderRemaining = 0;

			if(scale > 16) 
			{
				renderRemaining = 16;
				scale -= 16;
			} 
			else {
				renderRemaining = scale;
				scale = 0;
			}

			mc.renderEngine.bindTexture(liquid.canonical().getTextureSheet());
			drawTexturedModelRectFromIcon(width + xPos, height + yPos + 58 - renderRemaining - start, liquid.canonical().getRenderingIcon(), 16, 16 - (16 - renderRemaining));
			start+=16;

			if(renderRemaining == 0 || scale == 0)
			{
				break;
			}
		}

		mc.renderEngine.bindTexture("/mods/mekanism/gui/GuiDynamicTank.png");
		
		drawTexturedModalRect(width + xPos, height + yPos, 176, side == 0 ? 0 : 54, 16, 54);
	}
}
