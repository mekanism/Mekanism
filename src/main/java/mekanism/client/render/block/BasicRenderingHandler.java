package mekanism.client.render.block;

import mekanism.client.ClientProxy;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.MekanismBlocks;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.world.IBlockAccess;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class BasicRenderingHandler implements ISimpleBlockRenderingHandler
{
	@Override
	public void renderInventoryBlock(Block block, int metadata, int modelID, RenderBlocks renderer)
	{
		GL11.glPushMatrix();
		GL11.glRotatef(90F, 0.0F, 1.0F, 0.0F);

		if(block == MekanismBlocks.BasicBlock || block == MekanismBlocks.BasicBlock2)
		{
			if(block == MekanismBlocks.BasicBlock && metadata == 10)
			{
				MekanismRenderer.blendOn();
			}
			
			GL11.glRotatef(180, 0.0F, 1.0F, 0.0F);
			MekanismRenderer.renderItem(renderer, metadata, block);
			
			if(block == MekanismBlocks.BasicBlock && metadata == 10)
			{
				MekanismRenderer.blendOff();
			}
		}

		GL11.glPopMatrix();
	}

	@Override
	public boolean renderWorldBlock(IBlockAccess world, int x, int y, int z, Block block, int modelId, RenderBlocks renderer)
	{
		if(block == MekanismBlocks.BasicBlock || block == MekanismBlocks.BasicBlock2)
		{
			int metadata = world.getBlockMetadata(x, y, z);

			renderer.renderStandardBlock(block, x, y, z);
			renderer.setRenderBoundsFromBlock(block);

			return true;
		}

		return false;
	}

	@Override
	public int getRenderId()
	{
		return ClientProxy.BASIC_RENDER_ID;
	}

	@Override
	public boolean shouldRender3DInInventory(int modelId)
	{
		return true;
	}
}
