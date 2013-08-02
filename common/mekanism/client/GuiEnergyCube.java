package mekanism.client;

import mekanism.common.ContainerEnergyCube;
import mekanism.common.Mekanism;
import mekanism.common.MekanismUtils;
import mekanism.common.MekanismUtils.ResourceType;
import mekanism.common.TileEntityEnergyCube;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.StatCollector;

import org.lwjgl.opengl.GL11;

import universalelectricity.core.electricity.ElectricityDisplay;
import universalelectricity.core.electricity.ElectricityDisplay.ElectricUnit;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiEnergyCube extends GuiContainer
{
	public TileEntityEnergyCube tileEntity;
	
	public GuiRedstoneControl redstoneControl;
	
	public GuiEnergyCube(InventoryPlayer inventory, TileEntityEnergyCube tentity)
	{
		super(new ContainerEnergyCube(inventory, tentity));
		tileEntity = tentity;
		redstoneControl = new GuiRedstoneControl(this, tileEntity, MekanismUtils.getResource(ResourceType.GUI, "GuiEnergyCube.png"));
	}
	
	@Override
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY)
	{
		int xAxis = (mouseX - (width - xSize) / 2);
		int yAxis = (mouseY - (height - ySize) / 2);
		
		String capacityInfo = ElectricityDisplay.getDisplayShort(tileEntity.getEnergyStored(), ElectricUnit.JOULES) + "/" + ElectricityDisplay.getDisplayShort(tileEntity.getMaxEnergyStored(), ElectricUnit.JOULES);
		String outputInfo = "Voltage: " + tileEntity.getVoltage() + "v";
		
		fontRenderer.drawString(tileEntity.tier.name + " Energy Cube", 43, 6, 0x404040);
		fontRenderer.drawString(capacityInfo, 45, 40, 0x00CD00);
		fontRenderer.drawString(outputInfo, 45, 49, 0x00CD00);
		fontRenderer.drawString(StatCollector.translateToLocal("container.inventory"), 8, ySize - 96 + 2, 0x00CD00);
		
		redstoneControl.renderForeground(xAxis, yAxis);
	}
	
	@Override
    protected void drawGuiContainerBackgroundLayer(float par1, int mouseX, int mouseY)
    {
		mc.renderEngine.func_110577_a(MekanismUtils.getResource(ResourceType.GUI, "GuiEnergyCube.png"));
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        int guiWidth = (width - xSize) / 2;
        int guiHeight = (height - ySize) / 2;
        drawTexturedModalRect(guiWidth, guiHeight, 0, 0, xSize, ySize);
        
        int xAxis = (mouseX - (width - xSize) / 2);
 		int yAxis = (mouseY - (height - ySize) / 2);
        
        int scale = (int)(((double)tileEntity.electricityStored / tileEntity.tier.MAX_ELECTRICITY) * 72);
        drawTexturedModalRect(guiWidth + 65, guiHeight + 17, 176, 0, scale, 20);
        
        redstoneControl.renderBackground(xAxis, yAxis, guiWidth, guiHeight);
    }
	
	@Override
	protected void mouseClicked(int mouseX, int mouseY, int button)
	{
		super.mouseClicked(mouseX, mouseY, button);
		
		if(button == 0)
		{
			int xAxis = (mouseX - (width - xSize) / 2);
			int yAxis = (mouseY - (height - ySize) / 2);
			
			redstoneControl.mouseClicked(xAxis, yAxis);
		}
	}
}
