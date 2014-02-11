package mekanism.client.gui;

import java.util.ArrayList;
import java.util.List;

import mekanism.api.Coord4D;
import mekanism.api.ListUtils;
import mekanism.api.gas.GasStack;
import mekanism.client.gui.GuiEnergyInfo.IInfoHandler;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.Mekanism;
import mekanism.common.PacketHandler;
import mekanism.common.PacketHandler.Transmission;
import mekanism.common.inventory.container.ContainerElectrolyticSeparator;
import mekanism.common.network.PacketTileEntity;
import mekanism.common.tile.TileEntityElectrolyticSeparator;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraftforge.fluids.FluidStack;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiElectrolyticSeparator extends GuiMekanism
{
	public TileEntityElectrolyticSeparator tileEntity;
	
	public GuiElectrolyticSeparator(InventoryPlayer inventory, TileEntityElectrolyticSeparator tentity)
    {
        super(new ContainerElectrolyticSeparator(inventory, tentity));
        
        guiElements.add(new GuiEnergyInfo(new IInfoHandler() {
        	@Override
        	public List<String> getInfo()
        	{
        		String multiplier = MekanismUtils.getEnergyDisplay(Mekanism.electrolyticSeparatorUsage);
        		return ListUtils.asList("Using: " + multiplier + "/t", "Needed: " + MekanismUtils.getEnergyDisplay(tileEntity.getMaxEnergy()-tileEntity.getEnergy()));
        	}
        }, this, tileEntity, MekanismUtils.getResource(ResourceType.GUI, "GuiElectrolyticSeparator.png")));
        
        tileEntity = tentity;
    }
	
	@Override
    protected void mouseClicked(int x, int y, int button)
    {
		super.mouseClicked(x, y, button);
		
		int xAxis = (x - (width - xSize) / 2);
		int yAxis = (y - (height - ySize) / 2);
		
		if(xAxis > 8 && xAxis < 17 && yAxis > 73 && yAxis < 82)
		{
			ArrayList data = new ArrayList();
			data.add((byte)0);

			PacketHandler.sendPacket(Transmission.SERVER, new PacketTileEntity().setParams(Coord4D.get(tileEntity), data));
			mc.sndManager.playSoundFX("random.click", 1.0F, 1.0F);

		}
		else if(xAxis > 160 && xAxis < 169 && yAxis > 73 && yAxis < 82)
		{
			ArrayList data = new ArrayList();
			data.add((byte)1);
			
			PacketHandler.sendPacket(Transmission.SERVER, new PacketTileEntity().setParams(Coord4D.get(tileEntity), data));
			mc.sndManager.playSoundFX("random.click", 1.0F, 1.0F);

		}
    }

	@Override
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY)
    {
		int xAxis = (mouseX - (width - xSize) / 2);
		int yAxis = (mouseY - (height - ySize) / 2);
		
        fontRenderer.drawString(tileEntity.getInvName(), 45, 6, 0x404040);
        
		String name = tileEntity.dumpLeft ? "Dumping..." : tileEntity.leftTank.getGas() == null ? MekanismUtils.localize("gui.none") : tileEntity.leftTank.getGas().getGas().getLocalizedName();
        fontRenderer.drawString(name, 21, 73, 0x404040);
        
		name = tileEntity.dumpRight ? "Dumping..." : tileEntity.rightTank.getGas() == null ? MekanismUtils.localize("gui.none") : tileEntity.rightTank.getGas().getGas().getLocalizedName();
        fontRenderer.drawString(name, 156-fontRenderer.getStringWidth(name), 73, 0x404040);

		if(xAxis >= 6 && xAxis <= 22 && yAxis >= 11 && yAxis <= 69)
		{
			drawCreativeTabHoveringText(tileEntity.fluidTank.getFluid() != null ? tileEntity.fluidTank.getFluid().getFluid().getLocalizedName() + ": " + tileEntity.fluidTank.getFluidAmount() + "mB" : MekanismUtils.localize("gui.empty"), xAxis, yAxis);
		}
		if(xAxis >= 59 && xAxis <= 75 && yAxis >= 19 && yAxis <= 47)
		{
			drawCreativeTabHoveringText(tileEntity.leftTank.getGas() != null ? tileEntity.leftTank.getGas().getGas().getLocalizedName() + ": " + tileEntity.leftTank.getStored() : MekanismUtils.localize("gui.empty"), xAxis, yAxis);
		}
		if(xAxis >= 101 && xAxis <= 117 && yAxis >= 19 && yAxis <= 47)
		{
			drawCreativeTabHoveringText(tileEntity.rightTank.getGas() != null ? tileEntity.rightTank.getGas().getGas().getLocalizedName() + ": " + tileEntity.rightTank.getStored() : MekanismUtils.localize("gui.empty"), xAxis, yAxis);
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
		
		mc.renderEngine.bindTexture(MekanismUtils.getResource(ResourceType.GUI, "GuiElectrolyticSeparator.png"));
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        int guiWidth = (width - xSize) / 2;
        int guiHeight = (height - ySize) / 2;
        drawTexturedModalRect(guiWidth, guiHeight, 0, 0, xSize, ySize);
        
        int displayInt = tileEntity.dumpLeft ? 60 : 52;
        drawTexturedModalRect(guiWidth + 8, guiHeight + 73, 176, displayInt, 8, 8);
        
        displayInt = tileEntity.dumpRight ? 60 : 52;
        drawTexturedModalRect(guiWidth + 160, guiHeight + 73, 176, displayInt, 8, 8);
        
        if(tileEntity.fluidTank.getFluid() != null)
        {
        	displayGauge(58, 6, 11, tileEntity.getScaledFluidLevel(58), tileEntity.fluidTank.getFluid(), null);
        }
        
        if(tileEntity.leftTank.getGas() != null)
        {
        	displayGauge(28, 59, 19, tileEntity.getLeftScaledLevel(28), null, tileEntity.leftTank.getGas());
        }
        
        if(tileEntity.rightTank.getGas() != null)
        {
        	displayGauge(28, 101, 19, tileEntity.getRightScaledLevel(28), null, tileEntity.rightTank.getGas());
        }
        
        displayInt = tileEntity.getScaledEnergyLevel(52);
        drawTexturedModalRect(guiWidth + 165, guiHeight + 17 + 52 - displayInt, 176, 52 - displayInt, 4, displayInt);
    }
	
	public void displayGauge(int length, int xPos, int yPos, int scale, FluidStack fluid, GasStack gas)
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
				drawTexturedModelRectFromIcon(guiWidth + xPos, guiHeight + yPos + length - renderRemaining - start, fluid.getFluid().getIcon(), 16, 16 - (16 - renderRemaining));
			}
			else if(gas != null)
			{
				drawTexturedModelRectFromIcon(guiWidth + xPos, guiHeight + yPos + length - renderRemaining - start, gas.getGas().getIcon(), 16, 16 - (16 - renderRemaining));
			}
			
			start+=16;

			if(renderRemaining == 0 || scale == 0)
			{
				break;
			}
		}

		mc.renderEngine.bindTexture(MekanismUtils.getResource(ResourceType.GUI, "GuiElectrolyticSeparator.png"));
		drawTexturedModalRect(guiWidth + xPos, guiHeight + yPos, 176, 68, 16, length+1);
	}
}
