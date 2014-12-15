package mekanism.client.render.block;

import mekanism.client.ClientProxy;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.Mekanism;
import mekanism.common.MekanismBlocks;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.world.IBlockAccess;
import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class PlasticRenderingHandler implements ISimpleBlockRenderingHandler
{
	@Override
	public void renderInventoryBlock(Block block, int metadata, int modelID, RenderBlocks renderer)
	{
		MekanismRenderer.renderItem(renderer, metadata, block);
	}

	@Override
	public boolean renderWorldBlock(IBlockAccess world, int x, int y, int z, Block block, int modelId, RenderBlocks renderer)
	{
		boolean flag = false;
		if(block == MekanismBlocks.GlowPlasticBlock)
		{
			Tessellator tessellator = Tessellator.instance;
			tessellator.setBrightness(240);
			int meta = world.getBlockMetadata(x, y, z);
			int l = block.getRenderColor(meta);
			int r = l >> 16 & 0xFF;
			int g = l >> 8 & 0xFF;
			int b = l & 0xFF;
			tessellator.setColorOpaque(r, g, b);

			if((renderer.renderAllFaces) || (block.shouldSideBeRendered(renderer.blockAccess, x, y - 1, z, 0)))
			{
				renderer.renderFaceYNeg(block, x, y, z, renderer.getBlockIcon(block, renderer.blockAccess, x, y, z, 0));
				flag = true;
			}

			if((renderer.renderAllFaces) || (block.shouldSideBeRendered(renderer.blockAccess, x, y + 1, z, 1)))
			{
				renderer.renderFaceYPos(block, x, y, z, renderer.getBlockIcon(block, renderer.blockAccess, x, y, z, 1));
				flag = true;
			}

			if((renderer.renderAllFaces) || (block.shouldSideBeRendered(renderer.blockAccess, x, y, z - 1, 2)))
			{
				renderer.renderFaceZNeg(block, x, y, z, renderer.getBlockIcon(block, renderer.blockAccess, x, y, z, 2));
				flag = true;
			}

			if((renderer.renderAllFaces) || (block.shouldSideBeRendered(renderer.blockAccess, x, y, z + 1, 3)))
			{
				renderer.renderFaceZPos(block, x, y, z, renderer.getBlockIcon(block, renderer.blockAccess, x, y, z, 3));
				flag = true;
			}

			if((renderer.renderAllFaces) || (block.shouldSideBeRendered(renderer.blockAccess, x - 1, y, z, 4)))
			{
				renderer.renderFaceXNeg(block, x, y, z, renderer.getBlockIcon(block, renderer.blockAccess, x, y, z, 4));
				flag = true;
			}

			if((renderer.renderAllFaces) || (block.shouldSideBeRendered(renderer.blockAccess, x + 1, y, z, 5)))
			{
				renderer.renderFaceXPos(block, x, y, z, renderer.getBlockIcon(block, renderer.blockAccess, x, y, z, 5));
				flag = true;
			}

			return flag;
		}
		flag = renderer.renderStandardBlock(block, x, y, z);
		renderer.setRenderBoundsFromBlock(block);
		return flag;

	}

	@Override
	public int getRenderId()
	{
		return ClientProxy.PLASTIC_RENDER_ID;
	}

	@Override
	public boolean shouldRender3DInInventory(int modelId)
	{
		return true;
	}
}
