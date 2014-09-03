package mekanism.common.multipart;

import net.minecraft.block.Block;

import codechicken.microblock.BlockMicroMaterial;

public class PlasticMicroMaterial extends BlockMicroMaterial
{
	public PlasticMicroMaterial(Block block, int meta)
	{
		super(block, meta);
	}

	@Override
	public int getColour(int pass)
	{
		return block().getRenderColor(meta()) << 8 | 0xFF;
	}
}
