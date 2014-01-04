package codechicken.multipart.minecraft;

import codechicken.lib.vec.BlockCoord;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Vec3Pool;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.common.ForgeDirection;

public class PartMetaAccess implements IBlockAccess
{
    public IPartMeta part;
    private BlockCoord pos;
    
    public PartMetaAccess(IPartMeta p)
    {
        part = p;
        pos = p.getPos();
    }
    
    @Override
    public int getBlockId(int i, int j, int k)
    {
        if(i == pos.x && j == pos.y && k == pos.z)
            return part.getBlockId();
        return part.getWorld().getBlockId(i, j, k);
    }

    @Override
    public TileEntity getBlockTileEntity(int i, int j, int k)
    {
        if(i == pos.x && j == pos.y && k == pos.z)
            throw new IllegalArgumentException("Unsupported Operation");
        return part.getWorld().getBlockTileEntity(i, j, k);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public int getLightBrightnessForSkyBlocks(int i, int j, int k, int l)
    {
        return part.getWorld().getLightBrightnessForSkyBlocks(i, j, k, l);
    }

    @Override
    public int getBlockMetadata(int i, int j, int k)
    {
        if(i == pos.x && j == pos.y && k == pos.z)
            return part.getMetadata()&0xF;
        return part.getWorld().getBlockMetadata(i, j, k);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public float getBrightness(int i, int j, int k, int l)
    {
        return part.getWorld().getBrightness(i, j, k, l);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public float getLightBrightness(int i, int j, int k)
    {
        return part.getWorld().getLightBrightness(i, j, k);
    }

    @Override
    public Material getBlockMaterial(int i, int j, int k)
    {
        return Block.blocksList[getBlockId(i, j, k)].blockMaterial;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean isBlockOpaqueCube(int i, int j, int k)
    {
        return part.getWorld().isBlockOpaqueCube(i, j, k);
    }

    @Override
    public boolean isBlockNormalCube(int i, int j, int k)
    {
        return part.getWorld().isBlockNormalCube(i, j, k);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean isAirBlock(int i, int j, int k)
    {
        throw new IllegalArgumentException("Unsupported Operation");
    }

    @Override
    @SideOnly(Side.CLIENT)
    public BiomeGenBase getBiomeGenForCoords(int i, int j)
    {
        return part.getWorld().getBiomeGenForCoords(i, j);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public int getHeight()
    {
        return part.getWorld().getHeight();
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean extendedLevelsInChunkCache()
    {
        return part.getWorld().extendedLevelsInChunkCache();
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean doesBlockHaveSolidTopSurface(int i, int j, int k)
    {
        throw new IllegalArgumentException("Unsupported Operation");
    }

    @Override
    public Vec3Pool getWorldVec3Pool()
    {
        return part.getWorld().getWorldVec3Pool();
    }

    @Override
    public int isBlockProvidingPowerTo(int i, int j, int k, int l)
    {
        throw new IllegalArgumentException("Unsupported Operation");
    }
    
    @Override
    public boolean isBlockSolidOnSide(int x, int y, int z, ForgeDirection side, boolean _default)
    {
        return part.getWorld().isBlockSolidOnSide(x, y, z, side, _default);
    }
}
