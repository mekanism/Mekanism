package mekanism.generators.client.gui;

import mekanism.api.Coord4D;
import mekanism.api.gas.Gas;
import mekanism.common.PacketHandler;
import mekanism.common.PacketHandler.Transmission;
import mekanism.common.network.PacketTileEntity;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import mekanism.generators.common.inventory.container.ContainerElectrolyticSeparator;
import mekanism.generators.common.tileentity.TileEntityElectrolyticSeparator;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.InventoryPlayer;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import java.util.ArrayList;

@SideOnly(Side.CLIENT)
public class GuiElectrolyticSeparator extends GuiContainer
{
	public TileEntityElectrolyticSeparator tileEntity;
	
	public GuiElectrolyticSeparator(InventoryPlayer inventory, TileEntityElectrolyticSeparator tentity)
    {
        super(new ContainerElectrolyticSeparator(inventory, tentity));
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
		String name = tileEntity.leftTank.getGas() == null ? MekanismUtils.localize("gui.none") : tileEntity.leftTank.getGas().getGas().getLocalizedName();
        fontRenderer.drawString(name, 21, 73, 0x404040);
		name = tileEntity.rightTank.getGas() == null ? MekanismUtils.localize("gui.none") : tileEntity.rightTank.getGas().getGas().getLocalizedName();
        fontRenderer.drawString(name, 152-(name.length()*5), 73, 0x404040);

		if(xAxis >= 7 && xAxis <= 11 && yAxis >= 17 && yAxis <= 69)
		{
			drawCreativeTabHoveringText(tileEntity.fluidTank.getFluid() != null ? tileEntity.fluidTank.getFluid().getFluid().getLocalizedName() + ": " + tileEntity.fluidTank.getFluidAmount() + "mB" : MekanismUtils.localize("gui.empty"), xAxis, yAxis);
		}
		if(xAxis >= 65 && xAxis <= 69 && yAxis >= 17 && yAxis <= 48)
		{
			drawCreativeTabHoveringText(tileEntity.leftTank.getGas() != null ? tileEntity.leftTank.getGas().getGas().getLocalizedName() + ": " + tileEntity.leftTank.getStored() : MekanismUtils.localize("gui.empty"), xAxis, yAxis);
		}
		if(xAxis >= 107 && xAxis <= 111 && yAxis >= 17 && yAxis <= 48)
		{
			drawCreativeTabHoveringText(tileEntity.rightTank.getGas() != null ? tileEntity.rightTank.getGas().getGas().getLocalizedName() + ": " + tileEntity.rightTank.getStored() : MekanismUtils.localize("gui.empty"), xAxis, yAxis);
		}
		if(xAxis >= 165 && xAxis <= 169 && yAxis >= 17 && yAxis <= 69)
		{
			drawCreativeTabHoveringText(MekanismUtils.getEnergyDisplay(tileEntity.getEnergy()), xAxis, yAxis);
		}
    }

	@Override
    protected void drawGuiContainerBackgroundLayer(float par1, int par2, int par3)
    {
		mc.renderEngine.bindTexture(MekanismUtils.getResource(ResourceType.GUI, "GuiElectrolyticSeparator.png"));
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        int guiWidth = (width - xSize) / 2;
        int guiHeight = (height - ySize) / 2;
        drawTexturedModalRect(guiWidth, guiHeight, 0, 0, xSize, ySize);
        
        int leftDisplay = tileEntity.dumpLeft ? 90 : 82;
        drawTexturedModalRect(guiWidth + 8, guiHeight + 73, 176, leftDisplay, 8, 8);
        
        int rightDisplay = tileEntity.dumpRight ? 90 : 82;
        drawTexturedModalRect(guiWidth + 160, guiHeight + 73, 176, rightDisplay, 8, 8);
        
        int displayInt;
        
        displayInt = tileEntity.getScaledFluidLevel(52);
        drawTexturedModalRect(guiWidth + 7, guiHeight + 17 + 52 - displayInt, 176 + 4, 52 - displayInt, 4, displayInt);
        
        displayInt = tileEntity.getLeftScaledLevel(30);
        drawTexturedModalRect(guiWidth + 65, guiHeight + 17 + 30 - displayInt, 176, 52 + 30 - displayInt, 4, displayInt);
        
        displayInt = tileEntity.getRightScaledLevel(30);
        drawTexturedModalRect(guiWidth + 107, guiHeight + 17 + 30 - displayInt, 176 + 4, 52 + 30 - displayInt, 4, displayInt);
        
        displayInt = tileEntity.getScaledEnergyLevel(52);
        drawTexturedModalRect(guiWidth + 165, guiHeight + 17 + 52 - displayInt, 176, 52 - displayInt, 4, displayInt);
    }
}
