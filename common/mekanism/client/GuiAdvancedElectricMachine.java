package mekanism.client;

import mekanism.common.ContainerAdvancedElectricMachine;
import mekanism.common.TileEntityAdvancedElectricMachine;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.InventoryPlayer;

import org.lwjgl.opengl.GL11;

import universalelectricity.core.electricity.ElectricityDisplay;
import universalelectricity.core.electricity.ElectricityDisplay.ElectricUnit;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiAdvancedElectricMachine extends GuiContainer
{
    public TileEntityAdvancedElectricMachine tileEntity;
    
    public GuiRedstoneControl redstoneControl;
    public GuiUpgradeManagement upgradeManagement;

    public GuiAdvancedElectricMachine(InventoryPlayer inventory, TileEntityAdvancedElectricMachine tentity)
    {
        super(new ContainerAdvancedElectricMachine(inventory, tentity));
        tileEntity = tentity;
        
        redstoneControl = new GuiRedstoneControl(this, tileEntity, tileEntity.guiLocation);
        upgradeManagement = new GuiUpgradeManagement(this, tileEntity, tileEntity.guiLocation);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY)
    {
		int xAxis = (mouseX - (width - xSize) / 2);
		int yAxis = (mouseY - (height - ySize) / 2);
		
        fontRenderer.drawString(tileEntity.fullName, 45, 6, 0x404040);
        fontRenderer.drawString("Inventory", 8, (ySize - 96) + 2, 0x404040);
        
		if(xAxis >= 165 && xAxis <= 169 && yAxis >= 17 && yAxis <= 69)
		{
			drawCreativeTabHoveringText(ElectricityDisplay.getDisplayShort(tileEntity.getEnergyStored(), ElectricUnit.JOULES), xAxis, yAxis);
		}
		
		redstoneControl.renderForeground(xAxis, yAxis);
		upgradeManagement.renderForeground(xAxis, yAxis);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float par1, int mouseX, int mouseY)
    {
    	mc.renderEngine.func_110577_a(tileEntity.guiLocation);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        int guiWidth = (width - xSize) / 2;
        int guiHeight = (height - ySize) / 2;
        drawTexturedModalRect(guiWidth, guiHeight, 0, 0, xSize, ySize);
        
    	int xAxis = (mouseX - (width - xSize) / 2);
		int yAxis = (mouseY - (height - ySize) / 2);
		
        int displayInt;
        
        displayInt = tileEntity.getScaledEnergyLevel(52);
        drawTexturedModalRect(guiWidth + 165, guiHeight + 17 + 52 - displayInt, 176, 19 + 52 - displayInt, 4, displayInt);

        displayInt = tileEntity.getScaledSecondaryEnergyLevel(12);
        drawTexturedModalRect(guiWidth + 61, guiHeight + 37 + 12 - displayInt, 176, 7 + 12 - displayInt, 5, displayInt);

        displayInt = tileEntity.getScaledProgress(24);
        drawTexturedModalRect(guiWidth + 79, guiHeight + 39, 176, 0, displayInt + 1, 7);
        
        redstoneControl.renderBackground(xAxis, yAxis, guiWidth, guiHeight);
        upgradeManagement.renderBackground(xAxis, yAxis, guiWidth, guiHeight);
    }
    
	@Override
	protected void mouseClicked(int mouseX, int mouseY, int button)
	{
		xSize += 26;
		super.mouseClicked(mouseX, mouseY, button);
		xSize -= 26;
		
		if(button == 0)
		{
			int xAxis = (mouseX - (width - xSize) / 2);
			int yAxis = (mouseY - (height - ySize) / 2);
			
			redstoneControl.mouseClicked(xAxis, yAxis);
			upgradeManagement.mouseClicked(xAxis, yAxis);
		}
	}
}