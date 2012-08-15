package net.uberkat.obsidian.common;

import java.util.ArrayList;

import cpw.mods.fml.common.registry.BlockProxy;

import net.minecraft.src.*;

public class BlockObsidianContainer extends BlockContainer implements BlockProxy
{
	public BlockObsidianContainer(int i, Material material)
	{
		super(i, material);
	}

	public TileEntity createNewTileEntity(World world)
	{
		return null;
	}
	
	public String getTextureFile()
	{
		return "/obsidian/terrain.png";
	}
}
