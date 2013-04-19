package mekanism.client;

import mekanism.common.IEnergyCube;
import mekanism.common.IElectricChest;
import mekanism.common.Mekanism;
import mekanism.common.BlockMachine.MachineType;
import mekanism.common.Tier.EnergyCubeTier;
import net.minecraft.block.Block;
import net.minecraft.client.model.ModelChest;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.IItemRenderer;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.client.registry.RenderingRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ItemRenderingHandler implements IItemRenderer
{
	@Override
	public boolean handleRenderType(ItemStack item, ItemRenderType type)
	{
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
        if(type == ItemRenderType.EQUIPPED)
        {
        	GL11.glTranslatef(0.5F, 0.5F, 0.5F);
        }
        
		if(item.getItem() instanceof IEnergyCube)
		{
			EnergyCubeTier tier = ((IEnergyCube)item.getItem()).getEnergyCubeTier(item);
	        
	        renderItem((RenderBlocks)data[0], tier.ordinal());
		}
		else if(item.getItemDamage() == MachineType.ELECTRIC_CHEST.meta)
		{
			IElectricChest chest = (IElectricChest)item.getItem();
			ModelChest electricChest = new ModelChest();
			
			GL11.glRotatef(90F, 0.0F, 1.0F, 0.0F);
            GL11.glTranslatef(-0.5F, -0.5F, -0.5F);
            GL11.glTranslatef(0, 1.0F, 1.0F);
            GL11.glScalef(1.0F, -1F, -1F);
	    	GL11.glBindTexture(3553, FMLClientHandler.instance().getClient().renderEngine.getTexture("/mods/mekanism/render/ElectricChest.png"));
	    	
			float lidangle = chest.getPrevLidAngle(item) + (chest.getLidAngle(item) - chest.getPrevLidAngle(item)) * 1F;//partialTick;
	        lidangle = 1.0F - lidangle;
	        lidangle = 1.0F - lidangle * lidangle * lidangle;
	        electricChest.chestLid.rotateAngleX = -((lidangle * 3.141593F) / 2.0F);
	    	
	    	electricChest.renderAll();
		}
		else {
			RenderingRegistry.instance().renderInventoryBlock((RenderBlocks)data[0], Block.blocksList[Mekanism.machineBlockID], item.getItemDamage(), ClientProxy.RENDER_ID);
		}
	}
	
	/**
	 * Cleaned-up snip of RenderBlocks.renderBlockAsItem() -- used for rendering an item as an entity,
	 * in a player's inventory, and in a player's hand.
	 * @param renderer - RenderBlocks renderer to render the item with
	 * @param metadata - block/item metadata
	 * @param block - block to render
	 */
	public void renderItem(RenderBlocks renderer, int metadata)
	{
		Block block = Block.blocksList[Mekanism.energyCubeID];
		renderer.setRenderBoundsFromBlock(block);
		block.setBlockBoundsForItemRender();

        if(renderer.useInventoryTint)
        {
            int renderColor = block.getRenderColor(metadata);
            float red = (float)(renderColor >> 16 & 255) / 255.0F;
            float green = (float)(renderColor >> 8 & 255) / 255.0F;
            float blue = (float)(renderColor & 255) / 255.0F;
            GL11.glColor4f(red, green, blue, 1.0F);
        }

        GL11.glTranslatef(-0.5F, -0.5F, -0.5F);
        Tessellator tessellator = Tessellator.instance;
        tessellator.startDrawingQuads();
        tessellator.setNormal(0.0F, -1.0F, 0.0F);
        renderer.renderBottomFace(block, 0.0D, 0.0D, 0.0D, renderer.getBlockIconFromSideAndMetadata(block, 0, metadata));
        tessellator.draw();
        tessellator.startDrawingQuads();
        tessellator.setNormal(0.0F, 1.0F, 0.0F);
        renderer.renderTopFace(block, 0.0D, 0.0D, 0.0D, renderer.getBlockIconFromSideAndMetadata(block, 1, metadata));
        tessellator.draw();
        tessellator.startDrawingQuads();
        tessellator.setNormal(0.0F, 0.0F, -1.0F);
        renderer.renderEastFace(block, 0.0D, 0.0D, 0.0D, renderer.getBlockIconFromSideAndMetadata(block, 2, metadata));
        tessellator.draw();
        tessellator.startDrawingQuads();
        tessellator.setNormal(0.0F, 0.0F, 1.0F);
        renderer.renderWestFace(block, 0.0D, 0.0D, 0.0D, renderer.getBlockIconFromSideAndMetadata(block, 5, metadata));
        tessellator.draw();
        tessellator.startDrawingQuads();
        tessellator.setNormal(-1.0F, 0.0F, 0.0F);
        renderer.renderNorthFace(block, 0.0D, 0.0D, 0.0D, renderer.getBlockIconFromSideAndMetadata(block, 4, metadata));
        tessellator.draw();
        tessellator.startDrawingQuads();
        tessellator.setNormal(1.0F, 0.0F, 0.0F);
        renderer.renderSouthFace(block, 0.0D, 0.0D, 0.0D, renderer.getBlockIconFromSideAndMetadata(block, 3, metadata));
        tessellator.draw();
        GL11.glTranslatef(0.5F, 0.5F, 0.5F);
	}
}
