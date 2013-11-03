package mekanism.client.render.item;

import mekanism.api.EnumColor;
import mekanism.api.energy.IEnergizedItem;
import mekanism.client.ClientProxy;
import mekanism.client.MekanismClient;
import mekanism.client.model.ModelEnergyCube;
import mekanism.client.model.ModelEnergyCube.ModelEnergyCore;
import mekanism.client.model.ModelRobit;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.IElectricChest;
import mekanism.common.IEnergyCube;
import mekanism.common.Mekanism;
import mekanism.common.Tier.EnergyCubeTier;
import mekanism.common.block.BlockMachine.MachineType;
import mekanism.common.item.ItemBlockMachine;
import mekanism.common.item.ItemRobit;
import mekanism.common.item.ItemWalkieTalkie;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelChest;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.IItemRenderer;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.client.registry.RenderingRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ItemRenderingHandler implements IItemRenderer
{
	public ModelRobit robit = new ModelRobit();
	public ModelChest electricChest = new ModelChest();
	public ModelEnergyCube energyCube = new ModelEnergyCube();
	public ModelEnergyCore energyCore = new ModelEnergyCore();
	
	@Override
	public boolean handleRenderType(ItemStack item, ItemRenderType type)
	{
		if(item.itemID == Mekanism.WalkieTalkie.itemID)
		{
			return type != ItemRenderType.INVENTORY;
		}
		
		return true;
	}

	@Override
	public boolean shouldUseRenderHelper(ItemRenderType type, ItemStack item, ItemRendererHelper helper)
	{
		return true;
	}

	@Override
	public void renderItem(ItemRenderType type, ItemStack item, Object... data) 
	{
        if(type == ItemRenderType.EQUIPPED || type == ItemRenderType.EQUIPPED_FIRST_PERSON)
        {
        	GL11.glTranslatef(0.5F, 0.5F, 0.5F);
        }
        
		if(item.getItem() instanceof IEnergyCube)
		{
			EnergyCubeTier tier = ((IEnergyCube)item.getItem()).getEnergyCubeTier(item);
			IEnergizedItem energized = (IEnergizedItem)item.getItem();
			Minecraft.getMinecraft().renderEngine.bindTexture(MekanismUtils.getResource(ResourceType.RENDER, "EnergyCube" + tier.name + ".png"));
			
			GL11.glRotatef(180F, 0.0F, 0.0F, 1.0F);
			GL11.glRotatef(270F, 0.0F, -1.0F, 0.0F);
	    	GL11.glTranslatef(0.0F, -1.0F, 0.0F);
	    	
			energyCube.render(0.0625F);
			
	        GL11.glPushMatrix();
	        GL11.glTranslated(0.0, 1.0, 0.0);
	        Minecraft.getMinecraft().renderEngine.bindTexture(MekanismUtils.getResource(ResourceType.RENDER, "EnergyCore.png"));

	        GL11.glShadeModel(GL11.GL_SMOOTH);
	        GL11.glEnable(GL11.GL_BLEND);
	        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

	        MekanismRenderer.glowOn();
	        
	        EnumColor c = tier.color;

	        GL11.glPushMatrix();
	        GL11.glScalef(0.4F, 0.4F, 0.4F);
	        GL11.glColor4f(c.getColor(0), c.getColor(1), c.getColor(2), (float)(energized.getEnergy(item)/energized.getMaxEnergy(item)));
	        GL11.glTranslatef(0, (float)Math.sin(Math.toRadians((MekanismClient.ticksPassed + MekanismRenderer.getPartialTicks()) * 3)) / 7, 0);
	        GL11.glRotatef((MekanismClient.ticksPassed + MekanismRenderer.getPartialTicks()) * 4, 0, 1, 0);
	        GL11.glRotatef(36F + (MekanismClient.ticksPassed + MekanismRenderer.getPartialTicks()) * 4, 0, 1, 1);
	        energyCore.render(0.0625F);
	        GL11.glPopMatrix();

	        MekanismRenderer.glowOff();

	        GL11.glShadeModel(GL11.GL_FLAT);
	        GL11.glDisable(GL11.GL_LINE_SMOOTH);
	        GL11.glDisable(GL11.GL_POLYGON_SMOOTH);
	        GL11.glDisable(GL11.GL_BLEND);

	        GL11.glPopMatrix();
		}
		else if(item.getItem() instanceof ItemWalkieTalkie)
		{
			if(((ItemWalkieTalkie)item.getItem()).getOn(item))
			{
				MekanismRenderer.glowOn();
			}
			
			MekanismRenderer.renderItem(item);
			
			if(((ItemWalkieTalkie)item.getItem()).getOn(item))
			{
				MekanismRenderer.glowOff();
			}
		}
		else if(item.getItem() instanceof ItemBlockMachine && item.getItemDamage() == MachineType.ELECTRIC_CHEST.meta)
		{
			IElectricChest chest = (IElectricChest)item.getItem();
			
			GL11.glRotatef(90F, 0.0F, 1.0F, 0.0F);
            GL11.glTranslatef(-0.5F, -0.5F, -0.5F);
            GL11.glTranslatef(0, 1.0F, 1.0F);
            GL11.glScalef(1.0F, -1F, -1F);
            
            Minecraft.getMinecraft().renderEngine.bindTexture(MekanismUtils.getResource(ResourceType.RENDER, "ElectricChest.png"));
	    	
			float lidangle = chest.getPrevLidAngle(item) + (chest.getLidAngle(item) - chest.getPrevLidAngle(item)) * MekanismRenderer.getPartialTicks();
	        lidangle = 1.0F - lidangle;
	        lidangle = 1.0F - lidangle * lidangle * lidangle;
	        electricChest.chestLid.rotateAngleX = -((lidangle * 3.141593F) / 2.0F);
	    	
	    	electricChest.renderAll();
		}
		else if(item.getItem() instanceof ItemRobit)
		{
			GL11.glRotatef(180, 0.0F, 0.0F, 1.0F);
			GL11.glRotatef(90, 0.0F, -1.0F, 0.0F);
			GL11.glTranslatef(0.0F, -1.5F, 0.0F);
			Minecraft.getMinecraft().renderEngine.bindTexture(MekanismUtils.getResource(ResourceType.RENDER, "Robit.png"));
			robit.render(0.08F);
		}
		else {
			RenderingRegistry.instance().renderInventoryBlock((RenderBlocks)data[0], Block.blocksList[Mekanism.machineBlockID], item.getItemDamage(), ClientProxy.MACHINE_RENDER_ID);
		}
	}
}
