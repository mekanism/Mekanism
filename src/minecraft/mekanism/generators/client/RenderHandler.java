package mekanism.generators.client;


import org.lwjgl.opengl.GL11;

import mekanism.common.Mekanism;
import mekanism.generators.common.MekanismGenerators;
import net.minecraft.src.Block;
import net.minecraft.src.IBlockAccess;
import net.minecraft.src.RenderBlocks;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler;

public class RenderHandler implements ISimpleBlockRenderingHandler
{
	public ModelAdvancedSolarGenerator solarGenerator = new ModelAdvancedSolarGenerator();
	public ModelBioGenerator bioGenerator = new ModelBioGenerator();
	
	@Override
	public void renderInventoryBlock(Block block, int metadata, int modelID, RenderBlocks renderer)
	{
	    GL11.glPushMatrix();
	    GL11.glRotatef(180F, 0.0F, 0.0F, 1.0F);
	    
	    if(block.blockID == MekanismGenerators.advancedSolarGeneratorID)
	    {
	    	GL11.glTranslatef(0.0F, 0.3F, 0.0F);
	        GL11.glBindTexture(3553, FMLClientHandler.instance().getClient().renderEngine.getTexture("/resources/mekanism/render/AdvancedSolarGenerator.png"));
	        solarGenerator.render(0.0F, 0.024F);
	    }
	    else if(block.blockID == MekanismGenerators.bioGeneratorID)
	    {
	    	GL11.glTranslated(0.0F, -1.1F, 0.0F);
	    	GL11.glBindTexture(3553, FMLClientHandler.instance().getClient().renderEngine.getTexture("/resources/mekanism/render/BioGenerator.png"));
	    	bioGenerator.render(0.0625F);
	    }
	    
	    GL11.glPopMatrix();
	}

	@Override
	public boolean renderWorldBlock(IBlockAccess world, int x, int y, int z, Block block, int modelId, RenderBlocks renderer)
	{
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

}
