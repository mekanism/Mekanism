package mekanism.generators.client;

import org.lwjgl.opengl.GL11;

import universalelectricity.core.electricity.ElectricInfo;
import universalelectricity.core.electricity.ElectricInfo.ElectricUnit;

import mekanism.common.MekanismUtils;
import mekanism.generators.common.ContainerHeatGenerator;
import mekanism.generators.common.ContainerSolarGenerator;
import mekanism.generators.common.TileEntityHeatGenerator;
import mekanism.generators.common.TileEntitySolarGenerator;
import net.minecraft.src.*;

public class GuiSolarGenerator extends GuiContainer
{
	public TileEntitySolarGenerator tileEntity;
	
	private int guiWidth;
	private int guiHeight;
	
	public GuiSolarGenerator(InventoryPlayer inventory, TileEntitySolarGenerator tentity)
    {
        super(new ContainerSolarGenerator(inventory, tentity));
        tileEntity = tentity;
    }

	@Override
	protected void drawGuiContainerForegroundLayer(int par1, int par2)
    {
        fontRenderer.drawString(tileEntity.fullName, tileEntity.fullName != "Advanced Solar Generator" ? 45 : 30, 6, 0x404040);
        fontRenderer.drawString("Inventory", 8, (ySize - 96) + 2, 0x404040);
        fontRenderer.drawString(ElectricInfo.getDisplayShort(tileEntity.electricityStored, ElectricUnit.JOULES), 51, 26, 0x00CD00);
        fontRenderer.drawString("Sun: " + tileEntity.seesSun, 51, 35, 0x00CD00);
        fontRenderer.drawString(tileEntity.getVoltage() + "v", 51, 44, 0x00CD00);
    }

	@Override
    protected void drawGuiContainerBackgroundLayer(float par1, int par2, int par3)
    {
        int texture = mc.renderEngine.getTexture("/resources/mekanism/gui/GuiSolarGenerator.png");
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        mc.renderEngine.bindTexture(texture);
        guiWidth = (width - xSize) / 2;
        guiHeight = (height - ySize) / 2;
        drawTexturedModalRect(guiWidth, guiHeight, 0, 0, xSize, ySize);
        int displayInt;
        
        displayInt = tileEntity.getScaledEnergyLevel(52);
        drawTexturedModalRect(guiWidth + 165, guiHeight + 17 + 52 - displayInt, 176, 52 - displayInt, 4, displayInt);
        
        drawTexturedModalRect(guiWidth + 20, guiHeight + 37, 176, (tileEntity.seesSun ? 52 : 64), 12, 12);
    }
}
