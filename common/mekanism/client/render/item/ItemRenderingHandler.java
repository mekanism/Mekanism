package mekanism.client.render.item;

import mekanism.client.ClientProxy;
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
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Icon;
import net.minecraftforge.client.IItemRenderer;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import cpw.mods.fml.client.registry.RenderingRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ItemRenderingHandler implements IItemRenderer
{
	public ModelRobit robit = new ModelRobit();
	public ModelChest electricChest = new ModelChest();
	
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
	        
			GL11.glRotatef(90, 0.0F, 1.0F, 0.0F);
	        MekanismRenderer.renderItem((RenderBlocks)data[0], tier.ordinal(), Mekanism.EnergyCube);
		}
		else if(item.getItem() instanceof ItemWalkieTalkie)
		{
			Icon icon = item.getItem().getIconIndex(item);
			TextureManager texturemanager = Minecraft.getMinecraft().getTextureManager();

            if (icon == null)
            {
                GL11.glPopMatrix();
                return;
            }

            texturemanager.bindTexture(texturemanager.getResourceLocation(item.getItemSpriteNumber()));
            Tessellator tessellator = Tessellator.instance;
            float f = icon.getMinU();
            float f1 = icon.getMaxU();
            float f2 = icon.getMinV();
            float f3 = icon.getMaxV();
            float f4 = 0.0F;
            float f5 = 0.3F;
            GL11.glEnable(GL12.GL_RESCALE_NORMAL);
            GL11.glTranslatef(-f4, -f5, 0.0F);
            float f6 = 1.5F;
            GL11.glScalef(f6, f6, f6);
            GL11.glRotatef(50.0F, 0.0F, 1.0F, 0.0F);
            GL11.glRotatef(335.0F, 0.0F, 0.0F, 1.0F);
            GL11.glTranslatef(-0.9375F, -0.0625F, 0.0F);
            RenderManager.instance.itemRenderer.renderItemIn2D(tessellator, f1, f2, f, f3, icon.getIconWidth(), icon.getIconHeight(), 0.0625F);

            GL11.glDisable(GL12.GL_RESCALE_NORMAL);
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
