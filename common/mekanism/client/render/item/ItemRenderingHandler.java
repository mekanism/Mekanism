package mekanism.client.render.item;

import mekanism.api.EnumColor;
import mekanism.api.energy.IEnergizedItem;
import mekanism.client.ClientProxy;
import mekanism.client.MekanismClient;
import mekanism.client.model.ModelEnergyCube;
import mekanism.client.model.ModelEnergyCube.ModelEnergyCore;
import mekanism.client.model.ModelGasTank;
import mekanism.client.model.ModelJetpack;
import mekanism.client.model.ModelObsidianTNT;
import mekanism.client.model.ModelRobit;
import mekanism.client.model.ModelTransmitter;
import mekanism.client.render.MekanismRenderer;
import mekanism.client.render.tileentity.RenderBin;
import mekanism.common.IElectricChest;
import mekanism.common.IEnergyCube;
import mekanism.common.Mekanism;
import mekanism.common.Tier.EnergyCubeTier;
import mekanism.common.block.BlockMachine.MachineType;
import mekanism.common.inventory.InventoryBin;
import mekanism.common.item.ItemBlockBasic;
import mekanism.common.item.ItemBlockMachine;
import mekanism.common.item.ItemJetpack;
import mekanism.common.item.ItemRobit;
import mekanism.common.item.ItemWalkieTalkie;
import mekanism.common.tileentity.TileEntityBin;
import mekanism.common.multipart.ItemPartTransmitter;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.model.ModelChest;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.IItemRenderer;
import net.minecraftforge.common.ForgeDirection;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.client.registry.RenderingRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ItemRenderingHandler implements IItemRenderer
{
	public ModelRobit robit = new ModelRobit();
	public ModelChest electricChest = new ModelChest();
	public ModelTransmitter transmitterSmall = new ModelTransmitter(ModelTransmitter.Size.SMALL);
    public ModelTransmitter transmitterLarge = new ModelTransmitter(ModelTransmitter.Size.LARGE);
	public ModelEnergyCube energyCube = new ModelEnergyCube();
	public ModelEnergyCore energyCore = new ModelEnergyCore();
	public ModelGasTank gasTank = new ModelGasTank();
	public ModelObsidianTNT obsidianTNT = new ModelObsidianTNT();
	public ModelJetpack jetpack = new ModelJetpack();
	
	public RenderBin binRenderer = (RenderBin)TileEntityRenderer.instance.specialRendererMap.get(TileEntityBin.class);
    private final RenderItem renderItem = (RenderItem)RenderManager.instance.getEntityClassRenderObject(EntityItem.class);
	
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
		RenderBlocks renderBlocks = (RenderBlocks)data[0];
		
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
	        GL11.glTranslatef(0, (float)Math.sin(Math.toRadians((MekanismClient.ticksPassed + MekanismRenderer.getPartialTick()) * 3)) / 7, 0);
	        GL11.glRotatef((MekanismClient.ticksPassed + MekanismRenderer.getPartialTick()) * 4, 0, 1, 0);
	        GL11.glRotatef(36F + (MekanismClient.ticksPassed + MekanismRenderer.getPartialTick()) * 4, 0, 1, 1);
	        energyCore.render(0.0625F);
	        GL11.glPopMatrix();

	        MekanismRenderer.glowOff();

	        GL11.glShadeModel(GL11.GL_FLAT);
	        GL11.glDisable(GL11.GL_LINE_SMOOTH);
	        GL11.glDisable(GL11.GL_POLYGON_SMOOTH);
	        GL11.glDisable(GL11.GL_BLEND);

	        GL11.glPopMatrix();
		}
		else if(item.getItem() instanceof ItemBlockBasic && item.getItemDamage() == 6)
		{
			RenderingRegistry.instance().renderInventoryBlock((RenderBlocks)data[0], Block.blocksList[Mekanism.basicBlockID], item.getItemDamage(), ClientProxy.BASIC_RENDER_ID);
			
			if(binRenderer == null || binRenderer.getFontRenderer() == null)
			{
				return;
			}
			
			InventoryBin inv = new InventoryBin(item);
			ForgeDirection side = ForgeDirection.getOrientation(2);
			
            String amount = "";
            ItemStack itemStack = inv.getStack();

            if(itemStack != null)
            {
                amount = Integer.toString(inv.getItemCount());
            }

            OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240, 240);

            if(itemStack != null)
            {
                GL11.glPushMatrix();
                
                if(!(itemStack.getItem() instanceof ItemBlock) || Block.blocksList[itemStack.itemID].getRenderType() != 0)
                {
                	GL11.glRotatef(180, 0, 0, 1);
                	GL11.glTranslatef(-1.02F, -0.2F, 0);
                	
                	if(type == ItemRenderType.INVENTORY)
                	{
                		GL11.glTranslatef(-0.45F, -0.4F, 0.0F);
                	}
                }
                
                if(type == ItemRenderType.EQUIPPED || type == ItemRenderType.EQUIPPED_FIRST_PERSON || type == ItemRenderType.ENTITY)
                {
                	GL11.glTranslatef(-0.22F, -0.2F, -0.22F);
                }

                GL11.glTranslated(0.73, 0.08, 0.44);
                GL11.glRotatef(90, 0, 1, 0);

                float scale = 0.03125F;
                float scaler = 0.9F;
                
                GL11.glScalef(scale*scaler, scale*scaler, 0);

                TextureManager renderEngine = Minecraft.getMinecraft().renderEngine;

                GL11.glDisable(GL11.GL_LIGHTING);
                
                renderItem.renderItemAndEffectIntoGUI(binRenderer.getFontRenderer(), renderEngine, itemStack, 0, 0);
                
                GL11.glEnable(GL11.GL_LIGHTING);
                GL11.glPopMatrix();
            }
			
            if(amount != "")
            {
            	float maxScale = 0.02F;
            	
		        GL11.glPushMatrix();
	
		        GL11.glPolygonOffset(-10, -10);
		        GL11.glEnable(GL11.GL_POLYGON_OFFSET_FILL);
	
		        float displayWidth = 1 - (2 / 16);
		        float displayHeight = 1 - (2 / 16);
		        GL11.glTranslatef(0, -0.31F, 0);
		        
		        if(type == ItemRenderType.EQUIPPED || type == ItemRenderType.EQUIPPED_FIRST_PERSON || type == ItemRenderType.ENTITY)
                {
		        	GL11.glTranslated(-0.5, -0.4, -0.5);
                }
	
                GL11.glTranslatef(0, 0.9F, 1);
                GL11.glRotatef(90, 0, 1, 0);
                GL11.glRotatef(90, 1, 0, 0);
	
		        GL11.glTranslatef(displayWidth / 2, 1F, displayHeight / 2);
		        GL11.glRotatef(-90, 1, 0, 0);
	
		        FontRenderer fontRenderer = binRenderer.getFontRenderer();
	
		        int requiredWidth = Math.max(fontRenderer.getStringWidth(amount), 1);
		        int lineHeight = fontRenderer.FONT_HEIGHT + 2;
		        int requiredHeight = lineHeight * 1;
		        float scaler = 0.4F;
		        float scaleX = (displayWidth / requiredWidth);
		        float scale = scaleX * scaler;
	
		        if(maxScale > 0)
		        {
		            scale = Math.min(scale, maxScale);
		        }
	
		        GL11.glScalef(scale, -scale, scale);
		        GL11.glDepthMask(false);
	
		        int offsetX;
		        int offsetY;
		        int realHeight = (int)Math.floor(displayHeight / scale);
		        int realWidth = (int)Math.floor(displayWidth / scale);
	
		        offsetX = (realWidth - requiredWidth) / 2;
		        offsetY = (realHeight - requiredHeight) / 2;
	
		        GL11.glDisable(GL11.GL_LIGHTING);
		        fontRenderer.drawString("\u00a7f" + amount, offsetX - (realWidth / 2), 1 + offsetY - (realHeight / 2), 1);
		        GL11.glEnable(GL11.GL_LIGHTING);
		        GL11.glDepthMask(true);
		        GL11.glDisable(GL11.GL_POLYGON_OFFSET_FILL);
	
		        GL11.glPopMatrix();
            }
		}
		else if(item.itemID == Mekanism.gasTankID)
		{
			Minecraft.getMinecraft().renderEngine.bindTexture(MekanismUtils.getResource(ResourceType.RENDER, "GasTank.png"));
			GL11.glRotatef(180F, 0.0F, 0.0F, 1.0F);
			GL11.glRotatef(270F, 0.0F, -1.0F, 0.0F);
	    	GL11.glTranslatef(0.0F, -1.0F, 0.0F);
			gasTank.render(0.0625F);
		}
		else if(item.itemID == Mekanism.obsidianTNTID)
		{
			Minecraft.getMinecraft().renderEngine.bindTexture(MekanismUtils.getResource(ResourceType.RENDER, "ObsidianTNT.png"));
			GL11.glRotatef(180F, 0.0F, 0.0F, 1.0F);
			GL11.glRotatef(180F, 0.0F, -1.0F, 0.0F);
	    	GL11.glTranslatef(0.0F, -1.0F, 0.0F);
			obsidianTNT.render(0.0625F);
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
		else if(MachineType.get(item) == MachineType.ELECTRIC_CHEST)
		{
			IElectricChest chest = (IElectricChest)item.getItem();
			
			GL11.glRotatef(90F, 0.0F, 1.0F, 0.0F);
            GL11.glTranslatef(-0.5F, -0.5F, -0.5F);
            GL11.glTranslatef(0, 1.0F, 1.0F);
            GL11.glScalef(1.0F, -1F, -1F);
            
            Minecraft.getMinecraft().renderEngine.bindTexture(MekanismUtils.getResource(ResourceType.RENDER, "ElectricChest.png"));
	    	
			float lidangle = chest.getPrevLidAngle(item) + (chest.getLidAngle(item) - chest.getPrevLidAngle(item)) * MekanismRenderer.getPartialTick();
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
		else if(item.getItem() instanceof ItemJetpack)
		{
			GL11.glRotatef(180, 0.0F, 0.0F, 1.0F);
			GL11.glRotatef(90, 0.0F, -1.0F, 0.0F);
			GL11.glTranslatef(0.2F, -0.35F, 0.0F);
			Minecraft.getMinecraft().renderEngine.bindTexture(MekanismUtils.getResource(ResourceType.RENDER, "Jetpack.png"));
			jetpack.render(0.0625F);
		}
		else if(item.getItem() instanceof ItemPartTransmitter)
		{
			GL11.glRotatef(180F, 0.0F, 0.0F, 1.0F);
			GL11.glRotatef(180F, 0.0F, -1.0F, 0.0F);
	    	GL11.glTranslated(0.0F, -1.0F, 0.0F);
	    	GL11.glDisable(GL11.GL_CULL_FACE);
	    	
	    	switch(item.getItem().getDamage(item))
	    	{
	    		case 0:
	    			Minecraft.getMinecraft().renderEngine.bindTexture(MekanismUtils.getResource(ResourceType.RENDER, "UniversalCable.png"));
	    			break;
	    		case 1:
	    			Minecraft.getMinecraft().renderEngine.bindTexture(MekanismUtils.getResource(ResourceType.RENDER, "MechanicalPipe.png"));
	    			break;
	    		case 2:
	    			Minecraft.getMinecraft().renderEngine.bindTexture(MekanismUtils.getResource(ResourceType.RENDER, "PressurizedTube.png"));
	    			break;
	    		case 3:
	    			Minecraft.getMinecraft().renderEngine.bindTexture(MekanismUtils.getResource(ResourceType.RENDER, "LogisticalTransporter.png"));
	    			break;
	    	}
	    	
	    	transmitterSmall.renderSide(ForgeDirection.UP, true);
	    	transmitterSmall.renderSide(ForgeDirection.DOWN, true);
	    	transmitterSmall.renderCenter(new boolean[]{true, true, false, false, false, false});
	    	
	    	GL11.glEnable(GL11.GL_CULL_FACE);

		}
		else {
			if(item.getItem() instanceof ItemBlockMachine)
			{
				RenderingRegistry.instance().renderInventoryBlock((RenderBlocks)data[0], Block.blocksList[item.itemID], item.getItemDamage(), ClientProxy.MACHINE_RENDER_ID);
			}
			else if(item.getItem() instanceof ItemBlockBasic)
			{
				RenderingRegistry.instance().renderInventoryBlock((RenderBlocks)data[0], Block.blocksList[item.itemID], item.getItemDamage(), ClientProxy.BASIC_RENDER_ID);
			}
		}
	}
}
