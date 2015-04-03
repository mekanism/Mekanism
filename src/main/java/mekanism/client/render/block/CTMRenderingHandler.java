package mekanism.client.render.block;

import mekanism.api.MekanismConfig;
import mekanism.client.ClientProxy;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.CTMData;
import mekanism.common.base.IBlockCTM;
import mekanism.common.block.BlockMachine.MachineType;
import mekanism.common.tile.TileEntityBasicBlock;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockAccess;
import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler;

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
			if(blockCTM.hasFacingOverride() && world.getTileEntity(x, y, z) instanceof TileEntityBasicBlock)
			{
				TileEntityBasicBlock tile = (TileEntityBasicBlock)world.getTileEntity(x, y, z);
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
		
		if(MachineType.get(block, meta) != null)
		{
			if(!MachineType.get(block, meta).hasModel)
			{
				TileEntity tile = world.getTileEntity(x, y, z);
				
				if(tile instanceof TileEntityBasicBlock)
				{
					if(((TileEntityBasicBlock)tile).facing >= 2)
					{
						rendererOld.uvRotateTop = MekanismRenderer.directionMap[((TileEntityBasicBlock)tile).facing-2];
						rendererOld.uvRotateBottom = MekanismRenderer.directionMap[((TileEntityBasicBlock)tile).facing-2];
					}
				}
				
				rendererOld.renderStandardBlock(block, x, y, z);
				rendererOld.setRenderBoundsFromBlock(block);
				
				return true;
			}
			
			return false;
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
