package mekanism.client.gui;

import java.util.ArrayList;
import java.util.List;

import mekanism.api.EnumColor;
import mekanism.api.ListUtils;
import mekanism.api.gas.Gas;
import mekanism.api.gas.GasStack;
import mekanism.api.gas.OreGas;
import mekanism.client.gui.GuiEnergyInfo.IInfoHandler;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.inventory.container.ContainerChemicalCrystalizer;
import mekanism.common.tile.TileEntityChemicalCrystalizer;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.oredict.OreDictionary;

import org.apache.commons.lang3.text.WordUtils;
import org.lwjgl.opengl.GL11;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiChemicalCrystalizer extends GuiMekanism
{
    public TileEntityChemicalCrystalizer tileEntity;
    
    public Gas prevGas;
    
	public ItemStack renderStack;
	
	public int stackSwitch = 0;
	
	public int stackIndex = 0;
	
	public List<ItemStack> iterStacks;

    public GuiChemicalCrystalizer(InventoryPlayer inventory, TileEntityChemicalCrystalizer tentity)
    {
        super(tentity, new ContainerChemicalCrystalizer(inventory, tentity));
        tileEntity = tentity;
        
        guiElements.add(new GuiRedstoneControl(this, tileEntity, MekanismUtils.getResource(ResourceType.GUI, "GuiChemicalCrystalizer.png")));
        guiElements.add(new GuiEnergyInfo(new IInfoHandler() {
        	@Override
        	public List<String> getInfo()
        	{
        		String multiplier = MekanismUtils.getEnergyDisplay(tileEntity.ENERGY_USAGE);
        		return ListUtils.asList("Using: " + multiplier + "/t", "Needed: " + MekanismUtils.getEnergyDisplay(tileEntity.getMaxEnergy()-tileEntity.getEnergy()));
        	}
        }, this, tileEntity, MekanismUtils.getResource(ResourceType.GUI, "GuiChemicalCrystalizer.png")));
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY)
    {    	
		int xAxis = (mouseX - (width - xSize) / 2);
		int yAxis = (mouseY - (height - ySize) / 2);
		
        fontRenderer.drawString(tileEntity.getInvName(), 37, 4, 0x404040);
        
        if(tileEntity.inputTank.getGas() != null)
        {
        	fontRenderer.drawString(tileEntity.inputTank.getGas().getGas().getLocalizedName(), 29, 12, 0x00CD00);
        	fontRenderer.drawString("(" + ((OreGas)tileEntity.inputTank.getGas().getGas()).getOreName() + ")", 29, 21, 0x00CD00);
        }
        
    	if(renderStack != null)
		{
			try {
				GL11.glPushMatrix();
				GL11.glEnable(GL11.GL_LIGHTING);
				itemRenderer.renderItemAndEffectIntoGUI(fontRenderer, mc.getTextureManager(), renderStack, 12, 19);
				GL11.glDisable(GL11.GL_LIGHTING);
				GL11.glPopMatrix();
			} catch(Exception e) {}
		}
		
		if(xAxis >= 116 && xAxis <= 168 && yAxis >= 76 && yAxis <= 80)
		{
			drawCreativeTabHoveringText(MekanismUtils.getEnergyDisplay(tileEntity.getEnergy()), xAxis, yAxis);
		}
		
		if(xAxis >= 6 && xAxis <= 22 && yAxis >= 5 && yAxis <= 63)
		{
			drawCreativeTabHoveringText(tileEntity.inputTank.getGas() != null ? tileEntity.inputTank.getGas().getGas().getLocalizedName() + ": " + tileEntity.inputTank.getStored() : MekanismUtils.localize("gui.empty"), xAxis, yAxis);
		}
		
    	super.drawGuiContainerForegroundLayer(mouseX, mouseY);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTick, int mouseX, int mouseY)
    {
    	super.drawGuiContainerBackgroundLayer(partialTick, mouseX, mouseY);
    	
    	mc.renderEngine.bindTexture(MekanismUtils.getResource(ResourceType.GUI, "GuiChemicalCrystalizer.png"));
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        int guiWidth = (width - xSize) / 2;
        int guiHeight = (height - ySize) / 2;
        drawTexturedModalRect(guiWidth, guiHeight, 0, 0, xSize, ySize);
        
        int xAxis = mouseX - guiWidth;
		int yAxis = mouseY - guiHeight;
		
        int displayInt;
        
        displayInt = tileEntity.getScaledEnergyLevel(52);
        drawTexturedModalRect(guiWidth + 116, guiHeight + 76, 176, 0, displayInt, 4);

        displayInt = tileEntity.getScaledProgress(48);
        drawTexturedModalRect(guiWidth + 64, guiHeight + 40, 176, 63, displayInt, 8);
        
        if(tileEntity.getScaledInputGasLevel(58) > 0)
        {
        	displayGauge(6, 5, tileEntity.getScaledInputGasLevel(58), null, tileEntity.inputTank.getGas());
        }
    }
    
    private Gas getInputGas()
    {
    	return tileEntity.inputTank.getGas() != null ? tileEntity.inputTank.getGas().getGas() : null;
    }
    
    private void resetStacks()
    {
    	iterStacks.clear();
		renderStack = null;
	   	stackSwitch = 0;
    	stackIndex = -1;
    }
    
    @Override
    public void updateScreen()
    {
    	super.updateScreen();
    	
    	if(prevGas != getInputGas())
    	{
    		prevGas = getInputGas();
    		
    		if(prevGas == null || !(prevGas instanceof OreGas))
    		{
    			resetStacks();
    		}
    		
    		OreGas gas = (OreGas)prevGas;
    		
    		if(gas != null)
    		{
    			String oreDictName = "ore" + WordUtils.capitalize(gas.getName());
    			updateStackList(oreDictName);
    		}
    	}
		
		if(stackSwitch > 0)
		{
			stackSwitch--;
		}
		
		if(stackSwitch == 0 && iterStacks != null && iterStacks.size() > 0)
		{
			stackSwitch = 20;
			
			if(stackIndex == -1 || stackIndex == iterStacks.size()-1)
			{
				stackIndex = 0;
			}
			else if(stackIndex < iterStacks.size()-1)
			{
				stackIndex++;
			}
			
			renderStack = iterStacks.get(stackIndex);
		}
		else if(iterStacks != null && iterStacks.size() == 0)
		{
			renderStack = null;
		}
    }
    
	public void displayGauge(int xPos, int yPos, int scale, FluidStack fluid, GasStack gas)
	{
	    if(fluid == null && gas == null)
	    {
	        return;
	    }
	    
	    int guiWidth = (width - xSize) / 2;
        int guiHeight = (height - ySize) / 2;
	    
		int start = 0;

		while(true)
		{
			int renderRemaining = 0;

			if(scale > 16) 
			{
				renderRemaining = 16;
				scale -= 16;
			} 
			else {
				renderRemaining = scale;
				scale = 0;
			}

			mc.renderEngine.bindTexture(MekanismRenderer.getBlocksTexture());
			
			if(fluid != null)
			{
				drawTexturedModelRectFromIcon(guiWidth + xPos, guiHeight + yPos + 58 - renderRemaining - start, fluid.getFluid().getIcon(), 16, 16 - (16 - renderRemaining));
			}
			else if(gas != null)
			{
				drawTexturedModelRectFromIcon(guiWidth + xPos, guiHeight + yPos + 58 - renderRemaining - start, gas.getGas().getIcon(), 16, 16 - (16 - renderRemaining));
			}
			
			start+=16;

			if(renderRemaining == 0 || scale == 0)
			{
				break;
			}
		}

		mc.renderEngine.bindTexture(MekanismUtils.getResource(ResourceType.GUI, "GuiChemicalCrystalizer.png"));
		drawTexturedModalRect(guiWidth + xPos, guiHeight + yPos, 176, 4, 16, 59);
	}
	
    private void updateStackList(String oreName)
    {
       	if(iterStacks == null)
    	{
    		iterStacks = new ArrayList<ItemStack>();
    	}
    	else {
    		iterStacks.clear();
    	}
    	
    	List<String> keys = new ArrayList<String>();
    	
    	for(String s : OreDictionary.getOreNames())
    	{
    		if(oreName.equals(s) || oreName.equals("*"))
    		{
    			keys.add(s);
    		}
    		else if(oreName.endsWith("*") && !oreName.startsWith("*"))
    		{
    			if(s.startsWith(oreName.substring(0, oreName.length()-1)))
    			{
    				keys.add(s);
    			}
    		}
    		else if(oreName.startsWith("*") && !oreName.endsWith("*"))
    		{
    			if(s.endsWith(oreName.substring(1)))
    			{
    				keys.add(s);
    			}
    		}
    		else if(oreName.startsWith("*") && oreName.endsWith("*"))
    		{
    			if(s.contains(oreName.substring(1, oreName.length()-1)))
    			{
    				keys.add(s);
    			}
    		}
    	}
    	
    	for(String key : keys)
    	{
    		for(ItemStack stack : OreDictionary.getOres(key))
    		{
    			ItemStack toAdd = stack.copy();
    			
    			if(!iterStacks.contains(stack) && toAdd.getItem() instanceof ItemBlock)
    			{
    				iterStacks.add(stack.copy());
    			}
    		}
    	}
    	
    	stackSwitch = 0;
    	stackIndex = -1;
    }
}
