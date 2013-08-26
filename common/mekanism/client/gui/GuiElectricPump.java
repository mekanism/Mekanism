package mekanism.client.gui;

import mekanism.client.render.MekanismRenderer;
import mekanism.common.inventory.container.ContainerElectricPump;
import mekanism.common.tileentity.TileEntityElectricPump;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;

import org.lwjgl.opengl.GL11;

import universalelectricity.core.electricity.ElectricityDisplay;
import universalelectricity.core.electricity.ElectricityDisplay.ElectricUnit;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiElectricPump extends GuiContainer
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
        fontRenderer.drawString(ElectricityDisplay.getDisplayShort(tileEntity.getEnergyStored(), ElectricUnit.JOULES), 51, 26, 0x00CD00);
        fontRenderer.drawString(tileEntity.fluidTank.getFluid() != null ? FluidRegistry.getFluidName(tileEntity.fluidTank.getFluid()) + ": " + tileEntity.fluidTank.getFluid().amount : "No fluid.", 51, 35, 0x00CD00);
        fontRenderer.drawString(tileEntity.getVoltage() + "v", 51, 44, 0x00CD00);
        
		if(xAxis >= 7 && xAxis <= 23 && yAxis >= 14 && yAxis <= 72)
		{
			drawCreativeTabHoveringText(tileEntity.fluidTank.getFluid() != null ? FluidRegistry.getFluidName(tileEntity.fluidTank.getFluid()) + ": " + tileEntity.fluidTank.getFluid().amount + "mB" : "Empty", xAxis, yAxis);
		}
		
		if(xAxis >= 165 && xAxis <= 169 && yAxis >= 17 && yAxis <= 69)
		{
			drawCreativeTabHoveringText(ElectricityDisplay.getDisplayShort(tileEntity.getEnergyStored(), ElectricUnit.JOULES), xAxis, yAxis);
		}
    }

	@Override
    protected void drawGuiContainerBackgroundLayer(float par1, int par2, int par3)
    {
		mc.renderEngine.func_110577_a(MekanismUtils.getResource(ResourceType.GUI, "GuiElectricPump.png"));
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        guiWidth = (width - xSize) / 2;
        guiHeight = (height - ySize) / 2;
        drawTexturedModalRect(guiWidth, guiHeight, 0, 0, xSize, ySize);
        int displayInt;
        
        displayInt = tileEntity.getScaledEnergyLevel(52);
        drawTexturedModalRect(guiWidth + 165, guiHeight + 17 + 52 - displayInt, 176, 52 - displayInt, 4, displayInt);
        
        if(tileEntity.getScaledFluidLevel(58) > 0) 
        {
			displayGauge(guiWidth, guiHeight, 14, 7, tileEntity.getScaledFluidLevel(58), tileEntity.fluidTank.getFluid());
		}
    }
	
	/*
	 * Credit to BuildCraft for both the gauge texture and parts of the code.
	 */
	public void displayGauge(int width, int height, int xPos, int yPos, int scale, FluidStack fluid)
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

			mc.renderEngine.func_110577_a(MekanismRenderer.getLiquidTexture());
			drawTexturedModelRectFromIcon(width + yPos, height + xPos + 58 - renderRemaining - start, fluid.getFluid().getIcon(), 16, 16 - (16 - renderRemaining));
			start+=16;

			if(renderRemaining == 0 || scale == 0)
			{
				break;
			}
		}

		mc.renderEngine.func_110577_a(MekanismUtils.getResource(ResourceType.GUI, "GuiElectricPump.png"));
		drawTexturedModalRect(width + yPos, height + xPos, 176, 52, 16, 60);
	}
}
