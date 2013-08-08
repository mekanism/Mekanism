package mekanism.generators.client;

import mekanism.client.GuiRedstoneControl;
import mekanism.common.MekanismUtils;
import mekanism.common.MekanismUtils.ResourceType;
import mekanism.generators.common.ContainerSolarGenerator;
import mekanism.generators.common.TileEntitySolarGenerator;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import universalelectricity.core.electricity.ElectricityDisplay;
import universalelectricity.core.electricity.ElectricityDisplay.ElectricUnit;

@SideOnly(Side.CLIENT)
public class GuiSolarGenerator extends GuiContainer
{
	public TileEntitySolarGenerator tileEntity;
	
	public GuiRedstoneControl redstoneControl;
	
	public GuiSolarGenerator(InventoryPlayer inventory, TileEntitySolarGenerator tentity)
    {
        super(new ContainerSolarGenerator(inventory, tentity));
        tileEntity = tentity;
        redstoneControl = new GuiRedstoneControl(this, tileEntity, MekanismUtils.getResource(ResourceType.GUI, "GuiSolarGenerator.png"));
    }

	@Override
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY)
    {
		int xAxis = (mouseX - (width - xSize) / 2);
		int yAxis = (mouseY - (height - ySize) / 2);
		
        fontRenderer.drawString(tileEntity.fullName, tileEntity.fullName != "Advanced Solar Generator" ? 45 : 30, 6, 0x404040);
        fontRenderer.drawString("Inventory", 8, (ySize - 96) + 2, 0x404040);
        fontRenderer.drawString(ElectricityDisplay.getDisplayShort(tileEntity.getEnergyStored(), ElectricUnit.JOULES), 51, 26, 0x00CD00);
        fontRenderer.drawString("Sun: " + tileEntity.seesSun, 51, 35, 0x00CD00);
        fontRenderer.drawString(tileEntity.getVoltage() + "v", 51, 44, 0x00CD00);
        
    	if(xAxis >= 165 && xAxis <= 169 && yAxis >= 17 && yAxis <= 69)
		{
			drawCreativeTabHoveringText(ElectricityDisplay.getDisplayShort(tileEntity.getEnergyStored(), ElectricUnit.JOULES), xAxis, yAxis);
		}
    	
    	redstoneControl.renderForeground(xAxis, yAxis);
    }

	@Override
    protected void drawGuiContainerBackgroundLayer(float par1, int mouseX, int mouseY)
    {
		mc.renderEngine.func_110577_a(MekanismUtils.getResource(ResourceType.GUI, "GuiSolarGenerator.png"));
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        int guiWidth = (width - xSize) / 2;
        int guiHeight = (height - ySize) / 2;
        drawTexturedModalRect(guiWidth, guiHeight, 0, 0, xSize, ySize);
        
        int xAxis = (mouseX - (width - xSize) / 2);
 		int yAxis = (mouseY - (height - ySize) / 2);
 		
        int displayInt;
        
        displayInt = tileEntity.getScaledEnergyLevel(52);
        drawTexturedModalRect(guiWidth + 165, guiHeight + 17 + 52 - displayInt, 176, 52 - displayInt, 4, displayInt);
        
        drawTexturedModalRect(guiWidth + 20, guiHeight + 37, 176, (tileEntity.seesSun ? 52 : 64), 12, 12);
        
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
