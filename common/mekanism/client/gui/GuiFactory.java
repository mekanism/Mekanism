package mekanism.client.gui;

import mekanism.common.IFactory.RecipeType;
import mekanism.common.Tier.FactoryTier;
import mekanism.common.inventory.container.ContainerFactory;
import mekanism.common.tileentity.TileEntityFactory;
import net.minecraft.entity.player.InventoryPlayer;

import org.lwjgl.opengl.GL11;

import universalelectricity.core.electricity.ElectricityDisplay;
import universalelectricity.core.electricity.ElectricityDisplay.ElectricUnit;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiFactory extends GuiMekanism
{
    public TileEntityFactory tileEntity;

    public GuiFactory(InventoryPlayer inventory, TileEntityFactory tentity)
    {
        super(new ContainerFactory(inventory, tentity));
        tileEntity = tentity;
        
        guiElements.add(new GuiRedstoneControl(this, tileEntity, tileEntity.tier.guiLocation));
        guiElements.add(new GuiUpgradeManagement(this, tileEntity, tileEntity.tier.guiLocation));
        guiElements.add(new GuiRecipeType(this, tileEntity, tileEntity.tier.guiLocation));
        guiElements.add(new GuiConfigurationTab(this, tileEntity, tileEntity.tier.guiLocation));
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY)
    {
    	super.drawGuiContainerForegroundLayer(mouseX, mouseY);
    	
		int xAxis = (mouseX - (width - xSize) / 2);
		int yAxis = (mouseY - (height - ySize) / 2);
		
        fontRenderer.drawString(tileEntity.fullName, 48, 4, 0x404040);
        fontRenderer.drawString("Inventory", 8, (ySize - 93) + 2, 0x404040);
        fontRenderer.drawString(RecipeType.values()[tileEntity.recipeType].getName(), 124, (ySize - 93) + 2, 0x404040);
        
		if(xAxis >= 165 && xAxis <= 169 && yAxis >= 17 && yAxis <= 69)
		{
			drawCreativeTabHoveringText(ElectricityDisplay.getDisplayShort(tileEntity.getEnergyStored(), ElectricUnit.JOULES), xAxis, yAxis);
		}
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTick, int mouseX, int mouseY)
    {
    	super.drawGuiContainerBackgroundLayer(partialTick, mouseX, mouseY);
    	
    	mc.renderEngine.bindTexture(tileEntity.tier.guiLocation);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        int guiWidth = (width - xSize) / 2;
        int guiHeight = (height - ySize) / 2;
        drawTexturedModalRect(guiWidth, guiHeight, 0, 0, xSize, ySize);
        
        int xAxis = mouseX - guiWidth;
 		int yAxis = mouseY - guiHeight;
        
        int displayInt;
        
        displayInt = tileEntity.getScaledEnergyLevel(52);
        drawTexturedModalRect(guiWidth + 165, guiHeight + 17 + 52 - displayInt, 176, 52 - displayInt, 4, displayInt);
        
        if(tileEntity.tier == FactoryTier.BASIC)
        {
        	for(int i = 0; i < tileEntity.tier.processes; i++)
        	{
        		int xPos = 59 + (i*38);
        		
	        	displayInt = tileEntity.getScaledProgress(20, i);
	        	drawTexturedModalRect(guiWidth + xPos, guiHeight + 33, 176, 52, 8, displayInt);
        	}
        }
        else if(tileEntity.tier == FactoryTier.ADVANCED)
        {
        	for(int i = 0; i < tileEntity.tier.processes; i++)
        	{
        		int xPos = 39 + (i*26);
        		
	        	displayInt = tileEntity.getScaledProgress(20, i);
	        	drawTexturedModalRect(guiWidth + xPos, guiHeight + 33, 176, 52, 8, displayInt);
        	}
        }
        else if(tileEntity.tier == FactoryTier.ELITE)
        {
        	for(int i = 0; i < tileEntity.tier.processes; i++)
        	{
        		int xPos = 33 + (i*19);
        		
	        	displayInt = tileEntity.getScaledProgress(20, i);
	        	drawTexturedModalRect(guiWidth + xPos, guiHeight + 33, 176, 52, 8, displayInt);
        	}
        }
    }
}
