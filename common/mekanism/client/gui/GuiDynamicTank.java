package mekanism.client.gui;

import mekanism.client.render.MekanismRenderer;
import mekanism.common.TankUpdateProtocol;
import mekanism.common.inventory.container.ContainerDynamicTank;
import mekanism.common.tileentity.TileEntityDynamicTank;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraftforge.fluids.FluidStack;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiDynamicTank extends GuiMekanism
{
	public TileEntityDynamicTank tileEntity;
	
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
		
        fontRenderer.drawString(tileEntity.getInvName(), 45, 6, 0x404040);
        fontRenderer.drawString(MekanismUtils.localize("container.inventory"), 8, (ySize - 94) + 2, 0x404040);
        fontRenderer.drawString(MekanismUtils.localize("gui.volume") + ": " + tileEntity.clientCapacity/TankUpdateProtocol.FLUID_PER_TANK, 53, 26, 0x00CD00);
        fontRenderer.drawString(tileEntity.structure.fluidStored != null ? tileEntity.structure.fluidStored.getFluid().getName() + ":" : MekanismUtils.localize("gui.noFluid"), 53, 44, 0x00CD00);
        
        if(tileEntity.structure.fluidStored != null)
        {
	        fontRenderer.drawString(tileEntity.structure.fluidStored.amount + "mB", 53, 53, 0x00CD00);
        }
        
		if(xAxis >= 7 && xAxis <= 39 && yAxis >= 14 && yAxis <= 72)
		{
			drawCreativeTabHoveringText(tileEntity.structure.fluidStored != null ? tileEntity.structure.fluidStored.getFluid().getLocalizedName() + ": " + tileEntity.structure.fluidStored.amount + "mB" : MekanismUtils.localize("gui.empty"), xAxis, yAxis);
		}
		
		super.drawGuiContainerForegroundLayer(mouseX, mouseY);
    }

	@Override
    protected void drawGuiContainerBackgroundLayer(float partialTick, int mouseX, int mouseY)
    {
    	super.drawGuiContainerBackgroundLayer(partialTick, mouseX, mouseY);
    	
		mc.renderEngine.bindTexture(MekanismUtils.getResource(ResourceType.GUI, "GuiDynamicTank.png"));
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        int guiWidth = (width - xSize) / 2;
        int guiHeight = (height - ySize) / 2;
        drawTexturedModalRect(guiWidth, guiHeight, 0, 0, xSize, ySize);
        
        if(tileEntity.getScaledFluidLevel(58) > 0) 
        {
			displayGauge(7, 14, tileEntity.getScaledFluidLevel(58), tileEntity.structure.fluidStored, 0);
			displayGauge(23, 14, tileEntity.getScaledFluidLevel(58), tileEntity.structure.fluidStored, 1);
		}
    }
	
	public void displayGauge(int xPos, int yPos, int scale, FluidStack fluid, int side /*0-left, 1-right*/)
	{
	    if(fluid == null)
	    {
	        return;
	    }
	    
	    int guiWidth = (width - xSize) / 2;
        int guiHeight = (height - ySize) / 2;
	    
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

		mc.renderEngine.bindTexture(MekanismUtils.getResource(ResourceType.GUI, "GuiDynamicTank.png"));
		drawTexturedModalRect(guiWidth + xPos, guiHeight + yPos, 176, side == 0 ? 0 : 54, 16, 54);
	}
}
