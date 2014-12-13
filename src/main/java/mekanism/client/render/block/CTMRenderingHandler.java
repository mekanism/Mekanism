package mekanism.client.render.block;

import mekanism.api.MekanismConfig;
import mekanism.client.ClientProxy;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.CTMData;
import mekanism.common.base.IBlockCTM;
import mekanism.common.tile.TileEntityBasicBlock;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.util.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.fml.client.registry.ISimpleBlockRenderingHandler;

/**
 * CTM ISBRH adapted from Chisel
 * Code licensed under GPLv2
 * @author AUTOMATIC_MAIDEN, asie, pokefenn, unpairedbracket
 */
public class CTMRenderingHandler implements ISimpleBlockRenderingHandler
{
	RenderBlocksCTM rendererCTM = new RenderBlocksCTM();

	@Override
	public void renderInventoryBlock(Block block, int metadata, int modelID, RenderBlocks renderer)
	{
		MekanismRenderer.renderItem(renderer, metadata, block);
	}

	@Override
	public boolean renderWorldBlock(IBlockAccess world, int x, int y, int z, Block block, int modelId, RenderBlocks rendererOld)
	{
		int meta = world.getBlockMetadata(x, y, z);

		CTMData blockCTM = ((IBlockCTM)block).getCTMData(world, x, y, z, meta);

		if(MekanismConfig.client.renderCTM && blockCTM != null)
		{
			if(blockCTM.hasFacingOverride() && world.getTileEntity(new BlockPos(x, y, z)) instanceof TileEntityBasicBlock)
			{
				TileEntityBasicBlock tile = (TileEntityBasicBlock)world.getTileEntity(new BlockPos(x, y, z));
				blockCTM.setFacing(tile.facing);
			}
			rendererCTM.blockAccess = world;
			rendererCTM.renderMaxX = 1.0;
			rendererCTM.renderMaxY = 1.0;
			rendererCTM.renderMaxZ = 1.0;

			rendererCTM.dataCTM = blockCTM;

			rendererCTM.rendererOld = rendererOld;

			return rendererCTM.renderStandardBlock(block, x, y, z);
		}
		return rendererOld.renderStandardBlock(block, x, y, z);
	}

	@Override
	public boolean shouldRender3DInInventory(int renderId)
	{
		return true;
	}

	@Override
	public int getRenderId()
	{
		return ClientProxy.CTM_RENDER_ID;
	}

}
