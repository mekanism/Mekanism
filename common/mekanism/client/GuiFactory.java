package mekanism.client;

import mekanism.api.Object3D;
import mekanism.common.ContainerFactory;
import mekanism.common.IFactory.RecipeType;
import mekanism.common.PacketHandler;
import mekanism.common.PacketHandler.Transmission;
import mekanism.common.Tier.FactoryTier;
import mekanism.common.TileEntityFactory;
import mekanism.common.network.PacketRemoveUpgrade;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.InventoryPlayer;

import org.lwjgl.opengl.GL11;

import universalelectricity.core.electricity.ElectricityDisplay;
import universalelectricity.core.electricity.ElectricityDisplay.ElectricUnit;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiFactory extends GuiContainer
{
    public TileEntityFactory tileEntity;

    public GuiFactory(InventoryPlayer inventory, TileEntityFactory tentity)
    {
        super(new ContainerFactory(inventory, tentity));
        xSize+=26;
        tileEntity = tentity;
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY)
    {
		int xAxis = (mouseX - (width - xSize) / 2);
		int yAxis = (mouseY - (height - ySize) / 2);
		
        fontRenderer.drawString(tileEntity.fullName, 48, 4, 0x404040);
        fontRenderer.drawString("Inventory", 8, (ySize - 93) + 2, 0x404040);
        fontRenderer.drawString(RecipeType.values()[tileEntity.recipeType].getName(), 124, (ySize - 93) + 2, 0x404040);
        fontRenderer.drawString("S:" + (tileEntity.speedMultiplier+1) + "x", 179, 47, 0x404040);
        fontRenderer.drawString("E:" + (tileEntity.energyMultiplier+1) + "x", 179, 57, 0x404040);
        
		if(xAxis >= 165 && xAxis <= 169 && yAxis >= 17 && yAxis <= 69)
		{
			drawCreativeTabHoveringText(ElectricityDisplay.getDisplayShort(tileEntity.electricityStored, ElectricUnit.JOULES), xAxis, yAxis);
		}
		
		if(xAxis >= 179 && xAxis <= 198 && yAxis >= 47 && yAxis <= 54)
		{
			drawCreativeTabHoveringText("Remove speed upgrade", xAxis, yAxis);
		}
		
		if(xAxis >= 179 && xAxis <= 198 && yAxis >= 57 && yAxis <= 64)
		{
			drawCreativeTabHoveringText("Remove energy upgrade", xAxis, yAxis);
		}
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float par1, int par2, int par3)
    {
    	mc.renderEngine.func_110577_a(tileEntity.tier.guiLocation);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        int guiWidth = (width - xSize) / 2;
        int guiHeight = (height - ySize) / 2;
        drawTexturedModalRect(guiWidth, guiHeight, 0, 0, xSize, ySize);
        
        int displayInt;
        
        displayInt = tileEntity.getScaledEnergyLevel(52);
        drawTexturedModalRect(guiWidth + 165, guiHeight + 17 + 52 - displayInt, 176 + 26, 52 - displayInt, 4, displayInt);
        
        displayInt = tileEntity.getScaledUpgradeProgress(14);
        drawTexturedModalRect(guiWidth + 180, guiHeight + 30, 176 + 26, 72, 10, displayInt);
        
        displayInt = tileEntity.getScaledRecipeProgress(15);
        drawTexturedModalRect(guiWidth + 181, guiHeight + 94, 176 + 26, 86, 10, displayInt);
        
        if(tileEntity.tier == FactoryTier.BASIC)
        {
        	for(int i = 0; i < tileEntity.tier.processes; i++)
        	{
        		int xAxis = 59 + (i*38);
        		
	        	displayInt = tileEntity.getScaledProgress(20, i);
	        	drawTexturedModalRect(guiWidth + xAxis, guiHeight + 33, 176 + 26, 52, 8, displayInt);
        	}
        }
        else if(tileEntity.tier == FactoryTier.ADVANCED)
        {
        	for(int i = 0; i < tileEntity.tier.processes; i++)
        	{
        		int xAxis = 39 + (i*26);
        		
	        	displayInt = tileEntity.getScaledProgress(20, i);
	        	drawTexturedModalRect(guiWidth + xAxis, guiHeight + 33, 176 + 26, 52, 8, displayInt);
        	}
        }
        else if(tileEntity.tier == FactoryTier.ELITE)
        {
        	for(int i = 0; i < tileEntity.tier.processes; i++)
        	{
        		int xAxis = 33 + (i*19);
        		
	        	displayInt = tileEntity.getScaledProgress(20, i);
	        	drawTexturedModalRect(guiWidth + xAxis, guiHeight + 33, 176 + 26, 52, 8, displayInt);
        	}
        }
    }
    
	@Override
	protected void mouseClicked(int mouseX, int mouseY, int button)
	{
		super.mouseClicked(mouseX, mouseY, button);
		
		if(button == 0)
		{
			int xAxis = (mouseX - (width - xSize) / 2);
			int yAxis = (mouseY - (height - ySize) / 2);
			
			if(xAxis >= 179 && xAxis <= 198 && yAxis >= 47 && yAxis <= 54)
			{
				mc.sndManager.playSoundFX("random.click", 1.0F, 1.0F);
				PacketHandler.sendPacket(Transmission.SERVER, new PacketRemoveUpgrade().setParams(Object3D.get(tileEntity), (byte)0));
			}
			
			if(xAxis >= 179 && xAxis <= 198 && yAxis >= 57 && yAxis <= 64)
			{
				mc.sndManager.playSoundFX("random.click", 1.0F, 1.0F);
				PacketHandler.sendPacket(Transmission.SERVER, new PacketRemoveUpgrade().setParams(Object3D.get(tileEntity), (byte)1));
			}
		}
	}
}
