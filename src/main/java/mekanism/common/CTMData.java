package mekanism.common;

import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import mekanism.api.Coord4D;
import mekanism.client.render.block.TextureSubmap;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.util.ForgeDirection;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class CTMData
{
	public CTMTextureData mainTextureData;

	public HashMap<Block, List<Integer>> acceptableBlockMetas = new HashMap<Block, List<Integer>>();

	public CTMTextureData[] sideOverrides = new CTMTextureData[6];

	public CTMTextureData facingOverride;

	public int facing;

	public boolean renderConvexConnections = false;

	public CTMData(String textureName, Block block, List<Integer> connectableMeta)
	{
		mainTextureData = new CTMTextureData(textureName);
		acceptableBlockMetas.put(block, connectableMeta);
	}

	public CTMData addSideOverride(ForgeDirection side, String sideTexture)
	{
		sideOverrides[side.ordinal()] = new CTMTextureData(sideTexture);

		return this;
	}

	public CTMData addFacingOverride(String facingTexture)
	{
		facingOverride = new CTMTextureData(facingTexture);

		return this;
	}

	public boolean hasFacingOverride()
	{
		return facingOverride != null;
	}

	public void setFacing(int newFacing)
	{
		facing = newFacing;
	}

	public CTMData registerIcons(IIconRegister register)
	{
		mainTextureData.registerIcons(register);

		if(facingOverride != null)
		{
			facingOverride.registerIcons(register);
		}

		for(CTMTextureData data : sideOverrides)
		{
			if(data != null)
			{
				data.registerIcons(register);
			}
		}

		return this;
	}

	public CTMTextureData getTextureData(int side)
	{
		if(hasFacingOverride() && side == facing)
		{
			return facingOverride;
		}
		
		if(sideOverrides[side] != null)
		{
			return sideOverrides[side];
		}
		
		return mainTextureData;
	}

	public IIcon getIcon(int side)
	{
		return getTextureData(side).icon;
	}

	public TextureSubmap getSubmap(int side)
	{
		return getTextureData(side).submap;
	}

	public TextureSubmap getSmallSubmap(int side)
	{
		return getTextureData(side).submapSmall;
	}

	public CTMData addOtherBlockConnectivities(Block block, List<Integer> connectableMeta)
	{
		acceptableBlockMetas.put(block, connectableMeta);
		return this;
	}

	public CTMData setRenderConvexConnections()
	{
		renderConvexConnections = true;
		return this;
	}

	@SideOnly(Side.CLIENT)
	public boolean shouldRenderSide(IBlockAccess world, int x, int y, int z, int side)
	{
		Coord4D obj = new Coord4D(x, y, z);
		Block coordBlock = obj.getBlock(world);
		int coordMeta = obj.getMetadata(world);
		boolean valid = false;

		for(Entry<Block, List<Integer>> entry : acceptableBlockMetas.entrySet())
		{
			valid |= entry.getKey().equals(coordBlock) && entry.getValue().contains(coordMeta);
		}
		
		return !valid;
	}
}
