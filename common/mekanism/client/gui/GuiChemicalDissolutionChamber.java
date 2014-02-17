package mekanism.client.gui;

import java.util.List;

import mekanism.api.ListUtils;
import mekanism.api.gas.GasStack;
import mekanism.client.gui.GuiEnergyInfo.IInfoHandler;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.inventory.container.ContainerChemicalDissolutionChamber;
import mekanism.common.tile.TileEntityChemicalDissolutionChamber;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraftforge.fluids.FluidStack;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiChemicalDissolutionChamber extends GuiMekanism
{
    public TileEntityChemicalDissolutionChamber tileEntity;

    public GuiChemicalDissolutionChamber(InventoryPlayer inventory, TileEntityChemicalDissolutionChamber tentity)
    {
        super(tentity, new ContainerChemicalDissolutionChamber(inventory, tentity));
        tileEntity = tentity;
        
        guiElements.add(new GuiRedstoneControl(this, tileEntity, MekanismUtils.getResource(ResourceType.GUI, "GuiChemicalDissolutionChamber.png")));
        guiElements.add(new GuiEnergyInfo(new IInfoHandler() {
        	@Override
        	public List<String> getInfo()
        	{
        		String multiplier = MekanismUtils.getEnergyDisplay(tileEntity.ENERGY_USAGE);
        		return ListUtils.asList("Using: " + multiplier + "/t", "Needed: " + MekanismUtils.getEnergyDisplay(tileEntity.getMaxEnergy()-tileEntity.getEnergy()));
        	}
        }, this, tileEntity, MekanismUtils.getResource(ResourceType.GUI, "GuiChemicalDissolutionChamber.png")));
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY)
    {    	
		int xAxis = (mouseX - (width - xSize) / 2);
		int yAxis = (mouseY - (height - ySize) / 2);
		
        fontRenderer.drawString(MekanismUtils.localize("gui.chemicalDissolutionChamber.short"), 35, 4, 0x404040);
		
		if(xAxis >= 116 && xAxis <= 168 && yAxis >= 76 && yAxis <= 80)
		{
			drawCreativeTabHoveringText(MekanismUtils.getEnergyDisplay(tileEntity.getEnergy()), xAxis, yAxis);
		}
		
		if(xAxis >= 6 && xAxis <= 22 && yAxis >= 5 && yAxis <= 63)
		{
			drawCreativeTabHoveringText(tileEntity.injectTank.getGas() != null ? tileEntity.injectTank.getGas().getGas().getLocalizedName() + ": " + tileEntity.injectTank.getStored() : MekanismUtils.localize("gui.empty"), xAxis, yAxis);
		}
		
		if(xAxis >= 134 && xAxis <= 150 && yAxis >= 14 && yAxis <= 72)
		{
			drawCreativeTabHoveringText(tileEntity.outputTank.getGas() != null ? tileEntity.outputTank.getGas().getGas().getLocalizedName() + ": " + tileEntity.outputTank.getStored() : MekanismUtils.localize("gui.empty"), xAxis, yAxis);
		}
		
    	super.drawGuiContainerForegroundLayer(mouseX, mouseY);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTick, int mouseX, int mouseY)
    {
    	super.drawGuiContainerBackgroundLayer(partialTick, mouseX, mouseY);
    	
    	mc.renderEngine.bindTexture(MekanismUtils.getResource(ResourceType.GUI, "GuiChemicalDissolutionChamber.png"));
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        int guiWidth = (width - xSize) / 2;
        int guiHeight = (height - ySize) / 2;
        drawTexturedModalRect(guiWidth, guiHeight, 0, 0, xSize, ySize);
        
        int xAxis = mouseX - guiWidth;
		int yAxis = mouseY - guiHeight;
		
        int displayInt;
        
        displayInt = tileEntity.getScaledEnergyLevel(52);
        drawTexturedModalRect(guiWidth + 116, guiHeight + 76, 176, 0, displayInt, 4);

        displayInt = tileEntity.getScaledProgress(48);
        drawTexturedModalRect(guiWidth + 64, guiHeight + 40, 176, 63, displayInt, 8);
        
        if(tileEntity.getScaledInjectGasLevel(58) > 0)
        {
        	displayGauge(6, 5, tileEntity.getScaledInjectGasLevel(58), null, tileEntity.injectTank.getGas());
        }
        
        if(tileEntity.getScaledOutputGasLevel(58) > 0)
        {
        	displayGauge(134, 14, tileEntity.getScaledOutputGasLevel(58), null, tileEntity.outputTank.getGas());
        }
    }
    
	public void displayGauge(int xPos, int yPos, int scale, FluidStack fluid, GasStack gas)
	{
	    if(fluid == null && gas == null)
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
			
			if(fluid != null)
			{
				drawTexturedModelRectFromIcon(guiWidth + xPos, guiHeight + yPos + 58 - renderRemaining - start, fluid.getFluid().getIcon(), 16, 16 - (16 - renderRemaining));
			}
			else if(gas != null)
			{
				drawTexturedModelRectFromIcon(guiWidth + xPos, guiHeight + yPos + 58 - renderRemaining - start, gas.getGas().getIcon(), 16, 16 - (16 - renderRemaining));
			}
			
			start+=16;

			if(renderRemaining == 0 || scale == 0)
			{
				break;
			}
		}

		mc.renderEngine.bindTexture(MekanismUtils.getResource(ResourceType.GUI, "GuiChemicalDissolutionChamber.png"));
		drawTexturedModalRect(guiWidth + xPos, guiHeight + yPos, 176, 4, 16, 59);
	}
}
