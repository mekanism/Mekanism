package mekanism.client.gui;

import java.util.ArrayList;
import java.util.List;

import mekanism.api.Coord4D;
import mekanism.api.ListUtils;
import mekanism.api.gas.GasStack;
import mekanism.client.gui.GuiEnergyInfo.IInfoHandler;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.PacketHandler;
import mekanism.common.PacketHandler.Transmission;
import mekanism.common.inventory.container.ContainerChemicalWasher;
import mekanism.common.network.PacketTileEntity;
import mekanism.common.tile.TileEntityChemicalWasher;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraftforge.fluids.FluidStack;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiChemicalWasher extends GuiMekanism
{
    public TileEntityChemicalWasher tileEntity;

    public GuiChemicalWasher(InventoryPlayer inventory, TileEntityChemicalWasher tentity)
    {
        super(tentity, new ContainerChemicalWasher(inventory, tentity));
        tileEntity = tentity;
        
        guiElements.add(new GuiRedstoneControl(this, tileEntity, MekanismUtils.getResource(ResourceType.GUI, "GuiChemicalWasher.png")));
        guiElements.add(new GuiEnergyInfo(new IInfoHandler() {
        	@Override
        	public List<String> getInfo()
        	{
        		String multiplier = MekanismUtils.getEnergyDisplay(tileEntity.ENERGY_USAGE);
        		return ListUtils.asList("Using: " + multiplier + "/t", "Needed: " + MekanismUtils.getEnergyDisplay(tileEntity.getMaxEnergy()-tileEntity.getEnergy()));
        	}
        }, this, tileEntity, MekanismUtils.getResource(ResourceType.GUI, "GuiChemicalWasher.png")));
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY)
    {    	
		int xAxis = (mouseX - (width - xSize) / 2);
		int yAxis = (mouseY - (height - ySize) / 2);
		
        fontRenderer.drawString(tileEntity.getInvName(), 45, 4, 0x404040);
		
		if(xAxis >= 116 && xAxis <= 168 && yAxis >= 76 && yAxis <= 80)
		{
			drawCreativeTabHoveringText(MekanismUtils.getEnergyDisplay(tileEntity.getEnergy()), xAxis, yAxis);
		}
		
		if(xAxis >= 6 && xAxis <= 22 && yAxis >= 5 && yAxis <= 63)
		{
			drawCreativeTabHoveringText(tileEntity.fluidTank.getFluid() != null ? tileEntity.fluidTank.getFluid().getFluid().getLocalizedName() + ": " + tileEntity.fluidTank.getFluidAmount() : MekanismUtils.localize("gui.empty"), xAxis, yAxis);
		}
		
		if(xAxis >= 27 && xAxis <= 43 && yAxis >= 14 && yAxis <= 72)
		{
			drawCreativeTabHoveringText(tileEntity.inputTank.getGas() != null ? tileEntity.inputTank.getGas().getGas().getLocalizedName() + ": " + tileEntity.inputTank.getStored() : MekanismUtils.localize("gui.empty"), xAxis, yAxis);
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
    	
    	mc.renderEngine.bindTexture(MekanismUtils.getResource(ResourceType.GUI, "GuiChemicalWasher.png"));
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
	        drawTexturedModalRect(guiWidth + 61, guiHeight + 39, 176, 63, 55, 8);
        }
        
        if(tileEntity.getScaledFluidLevel(58) > 0)
        {
        	displayGauge(6, 5, tileEntity.getScaledFluidLevel(58), tileEntity.fluidTank.getFluid(), null);
        }
        
        if(tileEntity.getScaledInputGasLevel(58) > 0)
        {
        	displayGauge(27, 14, tileEntity.getScaledInputGasLevel(58), null, tileEntity.inputTank.getGas());
        }
        
        if(tileEntity.getScaledOutputGasLevel(58) > 0)
        {
        	displayGauge(134, 14, tileEntity.getScaledOutputGasLevel(58), null, tileEntity.outputTank.getGas());
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
			
			if(xAxis > 45 && xAxis < 63 && yAxis > 13 && yAxis < 21)
			{
				ArrayList data = new ArrayList();
				data.add(0);
				
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

		mc.renderEngine.bindTexture(MekanismUtils.getResource(ResourceType.GUI, "GuiChemicalWasher.png"));
		drawTexturedModalRect(guiWidth + xPos, guiHeight + yPos, 176, 4, 16, 59);
	}
}
