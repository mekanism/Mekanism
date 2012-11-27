package mekanism.generators.client;


import org.lwjgl.opengl.GL11;

import mekanism.common.Mekanism;
import mekanism.generators.common.MekanismGenerators;
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
	    GL11.glRotatef(180F, 0.0F, 0.0F, 1.0F);
	    
	    if(block.blockID == MekanismGenerators.generatorID)
	    {
	    	switch(metadata)
	    	{
	    		case 4:
	    	    	GL11.glTranslated(0.0F, -1.1F, 0.0F);
	    	    	GL11.glBindTexture(3553, FMLClientHandler.instance().getClient().renderEngine.getTexture("/resources/mekanism/render/BioGenerator.png"));
	    	    	bioGenerator.render(0.0625F);
	    	    	break;
	    		case 5:
	    	    	GL11.glTranslatef(0.0F, 0.3F, 0.0F);
	    	        GL11.glBindTexture(3553, FMLClientHandler.instance().getClient().renderEngine.getTexture("/resources/mekanism/render/AdvancedSolarGenerator.png"));
	    	        solarGenerator.render(0.0F, 0.024F);
	    	        break;
	    		default:
	    	        ForgeHooksClient.bindTexture(block.getTextureFile(), 0);
	    	        renderItem(renderer, metadata, block);
	    	        break;
	    	}
	    }
	    
	    GL11.glPopMatrix();
	}

	@Override
	public boolean renderWorldBlock(IBlockAccess world, int x, int y, int z, Block block, int modelId, RenderBlocks renderer)
	{
		if(block.blockID == MekanismGenerators.generatorID)
		{
			renderer.renderStandardBlock(block, x, y, z);
			renderer.func_83018_a(block);
			return true;
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
	
	public void renderItem(RenderBlocks renderer, int metadata, Block block)
	{
        block.setBlockBoundsForItemRender();

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
        renderer.renderBottomFace(block, 0.0D, 0.0D, 0.0D, block.getBlockTextureFromSideAndMetadata(1, metadata));
        tessellator.draw();
        tessellator.startDrawingQuads();
        tessellator.setNormal(0.0F, 1.0F, 0.0F);
        renderer.renderTopFace(block, 0.0D, 0.0D, 0.0D, block.getBlockTextureFromSideAndMetadata(0, metadata));
        tessellator.draw();
        tessellator.startDrawingQuads();
        tessellator.setNormal(0.0F, 0.0F, -1.0F);
        renderer.renderEastFace(block, 0.0D, 0.0D, 0.0D, block.getBlockTextureFromSideAndMetadata(2, metadata));
        tessellator.draw();
        tessellator.startDrawingQuads();
        tessellator.setNormal(0.0F, 0.0F, 1.0F);
        renderer.renderWestFace(block, 0.0D, 0.0D, 0.0D, block.getBlockTextureFromSideAndMetadata(4, metadata));
        tessellator.draw();
        tessellator.startDrawingQuads();
        tessellator.setNormal(-1.0F, 0.0F, 0.0F);
        renderer.renderNorthFace(block, 0.0D, 0.0D, 0.0D, block.getBlockTextureFromSideAndMetadata(3, metadata));
        tessellator.draw();
        tessellator.startDrawingQuads();
        tessellator.setNormal(1.0F, 0.0F, 0.0F);
        renderer.renderSouthFace(block, 0.0D, 0.0D, 0.0D, block.getBlockTextureFromSideAndMetadata(5, metadata));
        tessellator.draw();
        GL11.glTranslatef(0.5F, 0.5F, 0.5F);
	}
}
