package mekanism.common.multipart;

import mcmultipart.api.multipart.IMultipartTile;
import net.minecraft.tileentity.TileEntity;

public class MultipartTile implements IMultipartTile
{
	private TileEntity owner;
	
	public MultipartTile(TileEntity tile)
	{
		owner = tile;
	}
	
	@Override
	public TileEntity getTileEntity()
	{
		return owner;
	}
}
