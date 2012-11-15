package mekanism.client;

import org.lwjgl.opengl.GL11;

import mekanism.common.ContainerElectrolyticSeparator;
import mekanism.common.ContainerHeatGenerator;
import mekanism.common.MekanismUtils;
import mekanism.common.TileEntityElectrolyticSeparator;
import net.minecraft.src.*;

public class GuiElectrolyticSeparator extends GuiContainer
{
	public TileEntityElectrolyticSeparator tileEntity;
	
	private int guiWidth;
	private int guiHeight;
	
	public GuiElectrolyticSeparator(InventoryPlayer inventory, TileEntityElectrolyticSeparator tentity)
    {
        super(new ContainerElectrolyticSeparator(inventory, tentity));
        tileEntity = tentity;
    }

	@Override
	protected void drawGuiContainerForegroundLayer(int par1, int par2)
    {
        fontRenderer.drawString(tileEntity.fullName, 45, 6, 0x404040);
        fontRenderer.drawString("Inventory", 8, (ySize - 96) + 2, 0x404040);
    }

	@Override
    protected void drawGuiContainerBackgroundLayer(float par1, int par2, int par3)
    {
        int texture = mc.renderEngine.getTexture("/resources/mekanism/gui/GuiElectrolyticSeparator.png");
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        mc.renderEngine.bindTexture(texture);
        guiWidth = (width - xSize) / 2;
        guiHeight = (height - ySize) / 2;
        drawTexturedModalRect(guiWidth, guiHeight, 0, 0, xSize, ySize);
        int displayInt;
        
        displayInt = tileEntity.getScaledWaterLevel(52);
        drawTexturedModalRect(guiWidth + 7, guiHeight + 17 + 52 - displayInt, 176 + 4, 52 - displayInt, 4, displayInt);
        
        displayInt = tileEntity.getScaledHydrogenLevel(30);
        drawTexturedModalRect(guiWidth + 65, guiHeight + 17 + 30 - displayInt, 176, 52 + 30 - displayInt, 4, displayInt);
        
        displayInt = tileEntity.getScaledOxygenLevel(30);
        drawTexturedModalRect(guiWidth + 107, guiHeight + 17 + 30 - displayInt, 176 + 4, 52 + 30 - displayInt, 4, displayInt);
        
        displayInt = tileEntity.getScaledEnergyLevel(52);
        drawTexturedModalRect(guiWidth + 165, guiHeight + 17 + 52 - displayInt, 176, 52 - displayInt, 4, displayInt);
    }
}
