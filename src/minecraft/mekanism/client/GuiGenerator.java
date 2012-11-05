package mekanism.client;

import org.lwjgl.opengl.GL11;

import mekanism.common.ContainerGenerator;
import mekanism.common.MekanismUtils;
import mekanism.common.TileEntityGenerator;
import net.minecraft.src.*;

public class GuiGenerator extends GuiContainer
{
	public TileEntityGenerator tileEntity;
	
	private int guiWidth;
	private int guiHeight;
	
	public GuiGenerator(InventoryPlayer inventory, TileEntityGenerator tentity)
    {
        super(new ContainerGenerator(inventory, tentity));
        tileEntity = tentity;
    }

    /**
     * Draw the foreground layer for the GuiContainer (everythin in front of the items)
     */
	protected void drawGuiContainerForegroundLayer(int par1, int par2)
    {
        fontRenderer.drawString(tileEntity.fullName, 45, 6, 0x404040);
        fontRenderer.drawString("Inventory", 8, (ySize - 96) + 2, 0x404040);
        fontRenderer.drawString(MekanismUtils.getDisplayedEnergyNoColor(tileEntity.energyStored) + "/" + MekanismUtils.getDisplayedEnergyNoColor(tileEntity.MAX_ENERGY), 51, 26, 0x404040);
        fontRenderer.drawString("Fuel: " + tileEntity.fuelStored, 51, 35, 0x404040);
        fontRenderer.drawString("Out: " + MekanismUtils.getDisplayedEnergyNoColor(tileEntity.output), 51, 44, 0x404040);
    }

    /**
     * Draw the background layer for the GuiContainer (everything behind the items)
     */
    protected void drawGuiContainerBackgroundLayer(float par1, int par2, int par3)
    {
        int texture = mc.renderEngine.getTexture("/gui/GuiGenerator.png");
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        mc.renderEngine.bindTexture(texture);
        guiWidth = (width - xSize) / 2;
        guiHeight = (height - ySize) / 2;
        drawTexturedModalRect(guiWidth, guiHeight, 0, 0, xSize, ySize);
        int displayInt;
        
        displayInt = tileEntity.getScaledFuelLevel(52);
        drawTexturedModalRect(guiWidth + 7, guiHeight + 17 + 52 - displayInt, 176, 52 + 52 - displayInt, 4, displayInt);
        
        displayInt = tileEntity.getScaledEnergyLevel(52);
        drawTexturedModalRect(guiWidth + 165, guiHeight + 17 + 52 - displayInt, 176, 52 - displayInt, 4, displayInt);
    }
}
