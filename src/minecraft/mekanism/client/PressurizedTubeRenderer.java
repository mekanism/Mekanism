package mekanism.client;

import org.lwjgl.opengl.GL11;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.world.IBlockAccess;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler;

public class PressurizedTubeRenderer implements ISimpleBlockRenderingHandler
{
	public ModelPressurizedTube pressurizedTube = new ModelPressurizedTube();
	
	@Override
	public void renderInventoryBlock(Block block, int metadata, int modelID, RenderBlocks renderer) 
	{
		GL11.glRotatef(180F, 0.0F, 0.0F, 1.0F);
		GL11.glRotatef(180F, 0.0F, -1.0F, 0.0F);
    	GL11.glTranslated(0.0F, -1.0F, 0.0F);
    	GL11.glBindTexture(3553, FMLClientHandler.instance().getClient().renderEngine.getTexture("/resources/mekanism/render/PressurizedTube.png"));
    	pressurizedTube.Center.render(0.0625F);
    	pressurizedTube.Up.render(0.0625F);
    	pressurizedTube.Down.render(0.0625F);
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
		return ClientProxy.TUBE_RENDER_ID;
	}
}
