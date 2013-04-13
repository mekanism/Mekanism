package mekanism.client;

import mekanism.common.ContainerEnergyCube;
import mekanism.common.TileEntityEnergyCube;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.StatCollector;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import universalelectricity.core.electricity.ElectricityDisplay;
import universalelectricity.core.electricity.ElectricityDisplay.ElectricUnit;

@SideOnly(Side.CLIENT)
public class GuiEnergyCube extends GuiContainer
{
	private TileEntityEnergyCube tileEntity;
	
	private int guiWidth;
	private int guiHeight;
	
	public GuiEnergyCube(InventoryPlayer inventory, TileEntityEnergyCube tentity)
	{
		super(new ContainerEnergyCube(inventory, tentity));
		tileEntity = tentity;
	}
	
	@Override
	protected void drawGuiContainerForegroundLayer(int par1, int par2)
	{
		String capacityInfo = ElectricityDisplay.getDisplayShort(tileEntity.electricityStored, ElectricUnit.JOULES) + "/" + ElectricityDisplay.getDisplayShort(tileEntity.tier.MAX_ELECTRICITY, ElectricUnit.JOULES);
		String outputInfo = "Voltage: " + tileEntity.getVoltage() + "v";
		fontRenderer.drawString(tileEntity.tier.name + " Energy Cube", 43, 6, 0x404040);
		fontRenderer.drawString(capacityInfo, 45, 40, 0x00CD00);
		fontRenderer.drawString(outputInfo, 45, 49, 0x00CD00);
		fontRenderer.drawString(StatCollector.translateToLocal("container.inventory"), 8, ySize - 96 + 2, 0x00CD00);
	}
	
	@Override
    protected void drawGuiContainerBackgroundLayer(float par1, int par2, int par3)
    {
		mc.renderEngine.bindTexture("/mods/mekanism/gui/GuiEnergyCube.png");
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        
        guiWidth = (width - xSize) / 2;
        guiHeight = (height - ySize) / 2;
        drawTexturedModalRect(guiWidth, guiHeight, 0, 0, xSize, ySize);
        int scale = (int)(((double)tileEntity.electricityStored / tileEntity.tier.MAX_ELECTRICITY) * 72);
        drawTexturedModalRect(guiWidth + 65, guiHeight + 17, 176, 0, scale, 20);
    }
}
