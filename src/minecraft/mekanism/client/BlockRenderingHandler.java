package mekanism.client;

import mekanism.common.BlockMachine.MachineType;
import mekanism.common.Mekanism;
import net.minecraft.block.Block;
import net.minecraft.client.model.ModelChest;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.world.IBlockAccess;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class BlockRenderingHandler implements ISimpleBlockRenderingHandler
{
	public ModelTheoreticalElementizer theoreticalElementizer = new ModelTheoreticalElementizer();
	public ModelElectricPump electricPump = new ModelElectricPump();
	public ModelMetallurgicInfuser metallurgicInfuser = new ModelMetallurgicInfuser();
	public ModelChest electricChest = new ModelChest();
	
	@Override
	public void renderInventoryBlock(Block block, int metadata, int modelID, RenderBlocks renderer)
	{
	    GL11.glPushMatrix();
	    GL11.glRotatef(90F, 0.0F, 1.0F, 0.0F);
	    
	    if(block.blockID == Mekanism.machineBlockID)
	    {
    		if(metadata == MachineType.THEORETICAL_ELEMENTIZER.meta)
    		{
    			GL11.glRotatef(180F, 0.0F, 0.0F, 1.0F);
    			GL11.glRotatef(180F, 0.0F, -1.0F, 0.0F);
    	    	GL11.glTranslated(0.0F, -0.8F, 0.0F);
    	    	GL11.glBindTexture(3553, FMLClientHandler.instance().getClient().renderEngine.getTexture("/mods/mekanism/render/TheoreticalElementizer.png"));
    	    	theoreticalElementizer.render(0.0560F);
    		}
    		else if(metadata == MachineType.ELECTRIC_PUMP.meta)
    		{
    			GL11.glRotatef(180F, 0.0F, 0.0F, 1.0F);
    			GL11.glRotatef(90F, 0.0F, -1.0F, 0.0F);
    	    	GL11.glTranslated(0.0F, -0.85F, 0.0F);
    	    	GL11.glBindTexture(3553, FMLClientHandler.instance().getClient().renderEngine.getTexture("/mods/mekanism/render/ElectricPump.png"));
    	    	electricPump.render(0.0560F);
    		}
    		else if(metadata == MachineType.METALLURGIC_INFUSER.meta)
    		{
    			GL11.glRotatef(180F, 0.0F, 0.0F, 1.0F);
    			GL11.glRotatef(270F, 0.0F, -1.0F, 0.0F);
    	    	GL11.glTranslated(0.0F, 0.3F, 0.0F);
    	    	GL11.glBindTexture(3553, FMLClientHandler.instance().getClient().renderEngine.getTexture("/mods/mekanism/render/MetallurgicInfuser.png"));
    	    	metallurgicInfuser.render(0.0625F);
    		}
    		else if(metadata == MachineType.ELECTRIC_CHEST.meta)
    		{
                GL11.glTranslatef(-0.5F, -0.5F, -0.5F);
                GL11.glTranslatef(0, 1.0F, 1.0F);
                GL11.glScalef(1.0F, -1F, -1F);
    	    	GL11.glBindTexture(3553, FMLClientHandler.instance().getClient().renderEngine.getTexture("/mods/mekanism/render/ElectricChest.png"));
    	    	electricChest.renderAll();
    		}
    		else {
    	        renderItem(renderer, metadata, block);
    		}
	    }
	    
	    GL11.glPopMatrix();
	}

	@Override
	public boolean renderWorldBlock(IBlockAccess world, int x, int y, int z, Block block, int modelId, RenderBlocks renderer)
	{
		if(block.blockID == Mekanism.machineBlockID)
		{
			int metadata = world.getBlockMetadata(x, y, z);
			
			if(!MachineType.getFromMetadata(metadata).hasModel)
			{
				renderer.renderStandardBlock(block, x, y, z);
				renderer.setRenderBoundsFromBlock(block);
				return true;
			}
		}
		
		return false;
	}

	@Override
	public boolean shouldRender3DInInventory() 
	{
		return true;
	}

	@Override
	public int getRenderId() 
	{
		return ClientProxy.RENDER_ID;
	}
	
	/**
	 * Cleaned-up snip of RenderBlocks.renderBlockAsItem() -- used for rendering an item as an entity,
	 * in a player's inventory, and in a player's hand.
	 * @param renderer - RenderBlocks renderer to render the item with
	 * @param metadata - block/item metadata
	 * @param block - block to render
	 */
	public void renderItem(RenderBlocks renderer, int metadata, Block block)
	{
		block.setBlockBoundsForItemRender();
		
		renderer.setRenderBoundsFromBlock(block);

        if (renderer.useInventoryTint)
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
        renderer.renderBottomFace(block, 0.0D, 0.0D, 0.0D, block.getIcon(0, metadata));
        tessellator.draw();
        tessellator.startDrawingQuads();
        tessellator.setNormal(0.0F, 1.0F, 0.0F);
        renderer.renderTopFace(block, 0.0D, 0.0D, 0.0D, block.getIcon(1, metadata));
        tessellator.draw();
        tessellator.startDrawingQuads();
        tessellator.setNormal(0.0F, 0.0F, -1.0F);
        renderer.renderEastFace(block, 0.0D, 0.0D, 0.0D, block.getIcon(2, metadata));
        tessellator.draw();
        tessellator.startDrawingQuads();
        tessellator.setNormal(0.0F, 0.0F, 1.0F);
        renderer.renderWestFace(block, 0.0D, 0.0D, 0.0D, block.getIcon(3, metadata));
        tessellator.draw();
        tessellator.startDrawingQuads();
        tessellator.setNormal(-1.0F, 0.0F, 0.0F);
        renderer.renderNorthFace(block, 0.0D, 0.0D, 0.0D, block.getIcon(4, metadata));
        tessellator.draw();
        tessellator.startDrawingQuads();
        tessellator.setNormal(1.0F, 0.0F, 0.0F);
        renderer.renderSouthFace(block, 0.0D, 0.0D, 0.0D, block.getIcon(5, metadata));
        tessellator.draw();
        GL11.glTranslatef(0.5F, 0.5F, 0.5F);
	}
}
