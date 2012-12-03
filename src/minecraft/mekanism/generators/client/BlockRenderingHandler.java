package mekanism.generators.client;

import org.lwjgl.opengl.GL11;

import mekanism.common.Mekanism;
import mekanism.generators.common.MekanismGenerators;
import mekanism.generators.common.BlockGenerator.GeneratorType;
import net.minecraft.src.Block;
import net.minecraft.src.IBlockAccess;
import net.minecraft.src.ItemStack;
import net.minecraft.src.RenderBlocks;
import net.minecraft.src.Tessellator;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.IItemRenderer;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler;

public class BlockRenderingHandler implements ISimpleBlockRenderingHandler
{
	public ModelAdvancedSolarGenerator solarGenerator = new ModelAdvancedSolarGenerator();
	public ModelBioGenerator bioGenerator = new ModelBioGenerator();
	
	@Override
	public void renderInventoryBlock(Block block, int metadata, int modelID, RenderBlocks renderer)
	{
	    GL11.glPushMatrix();
	    GL11.glRotatef(90F, 0.0F, 1.0F, 0.0F);
	    
	    if(block.blockID == MekanismGenerators.generatorID)
	    {
    		if(metadata == 4)
    		{
    			GL11.glRotatef(180F, 0.0F, 0.0F, 1.0F);
    			GL11.glRotatef(90F, 0.0F, -1.0F, 0.0F);
    	    	GL11.glTranslated(0.0F, -1.0F, 0.0F);
    	    	GL11.glBindTexture(3553, FMLClientHandler.instance().getClient().renderEngine.getTexture("/resources/mekanism/render/BioGenerator.png"));
    	    	bioGenerator.render(0.0625F);
    		}
    		else if(metadata == 5)
    		{
    			GL11.glRotatef(180F, 0.0F, 0.0F, 1.0F);
    			GL11.glRotatef(90F, 0.0F, 1.0F, 0.0F);
    	    	GL11.glTranslatef(0.0F, 0.3F, 0.0F);
    	        GL11.glBindTexture(3553, FMLClientHandler.instance().getClient().renderEngine.getTexture("/resources/mekanism/render/AdvancedSolarGenerator.png"));
    	        solarGenerator.render(0.0F, 0.022F);
    		}
    		else {
    	        ForgeHooksClient.bindTexture(block.getTextureFile(), 0);
    	        renderItem(renderer, metadata, block);
    		}
	    }
	    
	    GL11.glPopMatrix();
	}

	@Override
	public boolean renderWorldBlock(IBlockAccess world, int x, int y, int z, Block block, int modelId, RenderBlocks renderer)
	{
		if(block.blockID == MekanismGenerators.generatorID)
		{
			int metadata = world.getBlockMetadata(x, y, z);
			
			if(metadata != 4 && metadata != 5)
			{
				renderer.renderStandardBlock(block, x, y, z);
				renderer.func_83018_a(block);
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
		return GeneratorsClientProxy.RENDER_ID;
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
		
		if(metadata == GeneratorType.SOLAR_GENERATOR.meta)
		{
			block.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 0.4F, 1.0F);
		}
		else {
			block.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
		}
		
		renderer.func_83018_a(block);

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
        renderer.renderBottomFace(block, 0.0D, 0.0D, 0.0D, block.getBlockTextureFromSideAndMetadata(0, metadata));
        tessellator.draw();
        tessellator.startDrawingQuads();
        tessellator.setNormal(0.0F, 1.0F, 0.0F);
        renderer.renderTopFace(block, 0.0D, 0.0D, 0.0D, block.getBlockTextureFromSideAndMetadata(1, metadata));
        tessellator.draw();
        tessellator.startDrawingQuads();
        tessellator.setNormal(0.0F, 0.0F, -1.0F);
        renderer.renderEastFace(block, 0.0D, 0.0D, 0.0D, block.getBlockTextureFromSideAndMetadata(2, metadata));
        tessellator.draw();
        tessellator.startDrawingQuads();
        tessellator.setNormal(0.0F, 0.0F, 1.0F);
        renderer.renderWestFace(block, 0.0D, 0.0D, 0.0D, block.getBlockTextureFromSideAndMetadata(3, metadata));
        tessellator.draw();
        tessellator.startDrawingQuads();
        tessellator.setNormal(-1.0F, 0.0F, 0.0F);
        renderer.renderNorthFace(block, 0.0D, 0.0D, 0.0D, block.getBlockTextureFromSideAndMetadata(4, metadata));
        tessellator.draw();
        tessellator.startDrawingQuads();
        tessellator.setNormal(1.0F, 0.0F, 0.0F);
        renderer.renderSouthFace(block, 0.0D, 0.0D, 0.0D, block.getBlockTextureFromSideAndMetadata(5, metadata));
        tessellator.draw();
        GL11.glTranslatef(0.5F, 0.5F, 0.5F);
	}
}
