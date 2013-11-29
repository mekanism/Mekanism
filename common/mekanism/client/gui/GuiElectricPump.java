package mekanism.client.gui;

import mekanism.client.render.MekanismRenderer;
import mekanism.common.Mekanism;
import mekanism.common.inventory.container.ContainerElectricPump;
import mekanism.common.tileentity.TileEntityElectricPump;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraftforge.fluids.FluidStack;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiElectricPump extends GuiMekanism
{
	public TileEntityElectricPump tileEntity;
	
	private int guiWidth;
	private int guiHeight;
	
	public GuiElectricPump(InventoryPlayer inventory, TileEntityElectricPump tentity)
    {
        super(new ContainerElectricPump(inventory, tentity));
        tileEntity = tentity;
    }
	
	@Override
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY)
    {
		int xAxis = (mouseX - (width - xSize) / 2);
		int yAxis = (mouseY - (height - ySize) / 2);
		
        fontRenderer.drawString(tileEntity.fullName, 45, 6, 0x404040);
        fontRenderer.drawString("Inventory", 8, (ySize - 94) + 2, 0x404040);
        fontRenderer.drawString(MekanismUtils.getEnergyDisplay(tileEntity.getEnergy()), 51, 26, 0x00CD00);
        fontRenderer.drawString(tileEntity.fluidTank.getFluid() != null ? tileEntity.fluidTank.getFluid().getFluid().getName() + ": " + tileEntity.fluidTank.getFluid().amount : "No fluid.", 51, 35, 0x00CD00);
        
		if(xAxis >= 7 && xAxis <= 23 && yAxis >= 14 && yAxis <= 72)
		{
			drawCreativeTabHoveringText(tileEntity.fluidTank.getFluid() != null ? tileEntity.fluidTank.getFluid().getFluid().getLocalizedName() + ": " + tileEntity.fluidTank.getFluid().amount + "mB" : "Empty", xAxis, yAxis);
		}
		
		if(xAxis >= 165 && xAxis <= 169 && yAxis >= 17 && yAxis <= 69)
		{
			drawCreativeTabHoveringText(MekanismUtils.getEnergyDisplay(tileEntity.getEnergy()), xAxis, yAxis);
		}
		
		super.drawGuiContainerForegroundLayer(mouseX, mouseY);
    }

	@Override
    protected void drawGuiContainerBackgroundLayer(float partialTick, int mouseX, int mouseY)
    {
    	super.drawGuiContainerBackgroundLayer(partialTick, mouseX, mouseY);
    	
		mc.renderEngine.bindTexture(MekanismUtils.getResource(ResourceType.GUI, "GuiElectricPump.png"));
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        guiWidth = (width - xSize) / 2;
        guiHeight = (height - ySize) / 2;
        drawTexturedModalRect(guiWidth, guiHeight, 0, 0, xSize, ySize);
        int displayInt;
        
        displayInt = tileEntity.getScaledEnergyLevel(52);
        drawTexturedModalRect(guiWidth + 165, guiHeight + 17 + 52 - displayInt, 176, 52 - displayInt, 4, displayInt);
        
        if(tileEntity.getScaledFluidLevel(58) > 0) 
        {
			displayGauge(7, 14, tileEntity.getScaledFluidLevel(58), tileEntity.fluidTank.getFluid());
		}
    }
	
	/*
	 * Credit to BuildCraft for both the gauge texture and parts of the code.
	 */
	public void displayGauge(int xPos, int yPos, int scale, FluidStack fluid)
	{
	    if(fluid == null)
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

			mc.renderEngine.bindTexture(MekanismRenderer.getBlocksTexture());
			drawTexturedModelRectFromIcon(guiWidth + xPos, guiHeight + yPos + 58 - renderRemaining - start, fluid.getFluid().getIcon(), 16, 16 - (16 - renderRemaining));
			start+=16;

			if(renderRemaining == 0 || scale == 0)
			{
				break;
			}
		}

		mc.renderEngine.bindTexture(MekanismUtils.getResource(ResourceType.GUI, "GuiElectricPump.png"));
		drawTexturedModalRect(guiWidth + xPos, guiHeight + yPos, 176, 52, 16, 60);
	}
}
