package mekanism.client.gui;

import java.util.ArrayList;

import mekanism.api.Coord4D;
import mekanism.api.gas.GasStack;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.PacketHandler;
import mekanism.common.PacketHandler.Transmission;
import mekanism.common.inventory.container.ContainerChemicalInfuser;
import mekanism.common.network.PacketTileEntity;
import mekanism.common.tileentity.TileEntityChemicalInfuser;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraftforge.fluids.FluidStack;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiChemicalInfuser extends GuiMekanism
{
    public TileEntityChemicalInfuser tileEntity;

    public GuiChemicalInfuser(InventoryPlayer inventory, TileEntityChemicalInfuser tentity)
    {
        super(tentity, new ContainerChemicalInfuser(inventory, tentity));
        tileEntity = tentity;
        
        guiElements.add(new GuiRedstoneControl(this, tileEntity, MekanismUtils.getResource(ResourceType.GUI, "GuiChemicalInfuser.png")));
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY)
    {    	
		int xAxis = (mouseX - (width - xSize) / 2);
		int yAxis = (mouseY - (height - ySize) / 2);
		
        fontRenderer.drawString(MekanismUtils.localize("gui.chemicalInfuser.short"), 5, 5, 0x404040);
        fontRenderer.drawString(MekanismUtils.localize("container.inventory"), 8, (ySize - 96) + 4, 0x404040);
		
		if(xAxis >= 116 && xAxis <= 168 && yAxis >= 76 && yAxis <= 80)
		{
			drawCreativeTabHoveringText(MekanismUtils.getEnergyDisplay(tileEntity.getEnergy()), xAxis, yAxis);
		}
		
		if(xAxis >= 26 && xAxis <= 42 && yAxis >= 14 && yAxis <= 72)
		{
			drawCreativeTabHoveringText(tileEntity.leftTank.getGas() != null ? tileEntity.leftTank.getGas().getGas().getLocalizedName() + ": " + tileEntity.leftTank.getStored() : MekanismUtils.localize("gui.empty"), xAxis, yAxis);
		}
		
		if(xAxis >= 134 && xAxis <= 150 && yAxis >= 14 && yAxis <= 72)
		{
			drawCreativeTabHoveringText(tileEntity.rightTank.getGas() != null ? tileEntity.rightTank.getGas().getGas().getLocalizedName() + ": " + tileEntity.rightTank.getStored() : MekanismUtils.localize("gui.empty"), xAxis, yAxis);
		}
		
		if(xAxis >= 80 && xAxis <= 96 && yAxis >= 5 && yAxis <= 63)
		{
			drawCreativeTabHoveringText(tileEntity.centerTank.getGas() != null ? tileEntity.centerTank.getGas().getGas().getLocalizedName() + ": " + tileEntity.centerTank.getStored() : MekanismUtils.localize("gui.empty"), xAxis, yAxis);
		}
		
    	super.drawGuiContainerForegroundLayer(mouseX, mouseY);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTick, int mouseX, int mouseY)
    {
    	super.drawGuiContainerBackgroundLayer(partialTick, mouseX, mouseY);
    	
    	mc.renderEngine.bindTexture(MekanismUtils.getResource(ResourceType.GUI, "GuiChemicalInfuser.png"));
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        int guiWidth = (width - xSize) / 2;
        int guiHeight = (height - ySize) / 2;
        drawTexturedModalRect(guiWidth, guiHeight, 0, 0, xSize, ySize);
        
        int xAxis = mouseX - guiWidth;
		int yAxis = mouseY - guiHeight;
		
        int displayInt;
        
        displayInt = tileEntity.getScaledEnergyLevel(52);
        drawTexturedModalRect(guiWidth + 116, guiHeight + 76, 176, 0, displayInt, 4);

        if(tileEntity.isActive)
        {
	        drawTexturedModalRect(guiWidth + 47, guiHeight + 39, 176, 71, 28, 8);
	        drawTexturedModalRect(guiWidth + 101, guiHeight + 39, 176, 63, 28, 8);
        }
        
        if(tileEntity.getScaledLeftGasLevel(58) > 0)
        {
        	displayGauge(26, 14, tileEntity.getScaledLeftGasLevel(58), null, tileEntity.leftTank.getGas());
        }
        
        if(tileEntity.getScaledRightGasLevel(58) > 0)
        {
        	displayGauge(134, 14, tileEntity.getScaledRightGasLevel(58), null, tileEntity.rightTank.getGas());
        }
        
        if(tileEntity.getScaledCenterGasLevel(58) > 0)
        {
        	displayGauge(80, 5, tileEntity.getScaledCenterGasLevel(58), null, tileEntity.centerTank.getGas());
        }
    }
    
    @Override
    protected void mouseClicked(int x, int y, int button)
    {
		super.mouseClicked(x, y, button);
		
		if(button == 0)
		{
			int xAxis = (x - (width - xSize) / 2);
			int yAxis = (y - (height - ySize) / 2);
			
			if(xAxis > 44 && xAxis < 62 && yAxis > 13 && yAxis < 21)
			{
				ArrayList data = new ArrayList();
				data.add(0);
				
				PacketHandler.sendPacket(Transmission.SERVER, new PacketTileEntity().setParams(Coord4D.get(tileEntity), data));
				mc.sndManager.playSoundFX("random.click", 1.0F, 1.0F);
			}
			else if(xAxis > 114 && xAxis < 132 && yAxis > 13 && yAxis < 21)
			{
				ArrayList data = new ArrayList();
				data.add(1);
				
				PacketHandler.sendPacket(Transmission.SERVER, new PacketTileEntity().setParams(Coord4D.get(tileEntity), data));
				mc.sndManager.playSoundFX("random.click", 1.0F, 1.0F);
			}
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

		mc.renderEngine.bindTexture(MekanismUtils.getResource(ResourceType.GUI, "GuiChemicalInfuser.png"));
		drawTexturedModalRect(guiWidth + xPos, guiHeight + yPos, 176, 4, 16, 59);
	}
}
