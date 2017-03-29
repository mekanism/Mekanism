package mekanism.common.multipart;

import java.util.Random;

import mekanism.api.Coord4D;
import mekanism.common.MekanismBlocks;
import mekanism.common.block.states.BlockStateFacing;
import mekanism.common.multipart.BlockStateTransmitter.TransmitterType.Size;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BlockGlowPanel extends Block implements ITileEntityProvider
{
	private static Random rand = new Random();
	public static AxisAlignedBB[] bounds = new AxisAlignedBB[6];

	static
	{
		AxisAlignedBB cuboid = new AxisAlignedBB(0.25, 0, 0.25, 0.75, 0.125, 0.75);
		Vec3d fromOrigin = new Vec3d(-0.5, -0.5, -0.5);

		for(EnumFacing side : EnumFacing.VALUES)
		{
			bounds[side.ordinal()] = MultipartMekanism.rotate(cuboid.offset(fromOrigin.xCoord, fromOrigin.yCoord, fromOrigin.zCoord), side).offset(-fromOrigin.xCoord, -fromOrigin.zCoord, -fromOrigin.zCoord);
		}
	}
	
	public BlockGlowPanel() 
	{
        super(Material.PISTON);
    }
	
	@Override
	public int getMetaFromState(IBlockState state)
    {
		return 0;
    }
	
	@Override
	public BlockStateContainer createBlockState()
	{
		return new GlowPanelBlockState(this);
	}
	
	@Override
	public IBlockState getActualState(IBlockState state, IBlockAccess world, BlockPos pos)
	{
		TileEntityGlowPanel tileEntity = (TileEntityGlowPanel)world.getTileEntity(pos);
		return state.withProperty(BlockStateFacing.facingProperty, tileEntity.side);
	}

	@SideOnly(Side.CLIENT)
    @Override
    public IBlockState getExtendedState(IBlockState state, IBlockAccess world, BlockPos pos) 
	{
		TileEntityGlowPanel tileEntity = (TileEntityGlowPanel)world.getTileEntity(pos);
		state = state.withProperty(BlockStateFacing.facingProperty, tileEntity.side);
		
		if(state instanceof IExtendedBlockState)
		{
			return ((IExtendedBlockState)state).withProperty(ColorProperty.INSTANCE, new ColorProperty(tileEntity.colour));
		}
		
		return state;
	}
	
	@Override
	public void neighborChanged(IBlockState state, World world, BlockPos pos, Block block, BlockPos neighbor)
	{
		TileEntityGlowPanel tileEntity = (TileEntityGlowPanel)world.getTileEntity(pos);
		
		if(!world.isRemote && !canStay(tileEntity))
		{			
			float motion = 0.7F;
			double motionX = (rand.nextFloat() * motion) + (1.0F - motion) * 0.5D;
			double motionY = (rand.nextFloat() * motion) + (1.0F - motion) * 0.5D;
			double motionZ = (rand.nextFloat() * motion) + (1.0F - motion) * 0.5D;

			ItemStack stack = new ItemStack(MekanismBlocks.GlowPanel, 1, tileEntity.colour.getMetaValue());
			EntityItem entityItem = new EntityItem(world, pos.getX() + motionX, pos.getY() + motionY, pos.getZ() + motionZ, stack);

			world.spawnEntity(entityItem);
			world.setBlockToAir(pos);
		}
	}
	
	@Override
	public void onNeighborChange(IBlockAccess world, BlockPos pos, BlockPos neighbor)
	{
		TileEntityGlowPanel tileEntity = (TileEntityGlowPanel)world.getTileEntity(pos);
		
		if(!tileEntity.getWorld().isRemote && !canStay(tileEntity))
		{			
			float motion = 0.7F;
			double motionX = (rand.nextFloat() * motion) + (1.0F - motion) * 0.5D;
			double motionY = (rand.nextFloat() * motion) + (1.0F - motion) * 0.5D;
			double motionZ = (rand.nextFloat() * motion) + (1.0F - motion) * 0.5D;

			ItemStack stack = new ItemStack(MekanismBlocks.GlowPanel, 1, tileEntity.colour.getMetaValue());
			EntityItem entityItem = new EntityItem(tileEntity.getWorld(), pos.getX() + motionX, pos.getY() + motionY, pos.getZ() + motionZ, stack);

			tileEntity.getWorld().spawnEntity(entityItem);
			tileEntity.getWorld().setBlockToAir(pos);
		}
	}
	
	@Override
	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos)
	{
		TileEntityGlowPanel tile = (TileEntityGlowPanel)world.getTileEntity(pos);
		return bounds[tile.side.ordinal()];
	}
	
	@Override
	public float getBlockHardness(IBlockState state, World world, BlockPos pos)
	{
		return 3.5F;
	}
	
	@Override
	public boolean canPlaceBlockOnSide(World world, BlockPos pos, EnumFacing side)
    {
		return world.isSideSolid(pos.offset(side), side.getOpposite());
    }
	
	public static boolean canStay(TileEntityGlowPanel tileEntity)
	{
		Coord4D adj = new Coord4D(tileEntity.getPos().offset(tileEntity.side), tileEntity.getWorld());
		return tileEntity.getWorld().isSideSolid(adj.getPos(), tileEntity.side.getOpposite());
	}
	
	@Override
	public int getLightValue(IBlockState state, IBlockAccess world, BlockPos pos)
    {
		return 15;
    }
	
	@Override
	public int damageDropped(IBlockState state)
    {
		return ((IExtendedBlockState)state).getValue(ColorProperty.INSTANCE).color.getMetaValue();
    }
	
	@Override
    public ItemStack getPickBlock(IBlockState state, RayTraceResult target, World world, BlockPos pos, EntityPlayer player)
	{
		TileEntityGlowPanel tileEntity = (TileEntityGlowPanel)world.getTileEntity(pos);
		return new ItemStack(MekanismBlocks.GlowPanel, 1, tileEntity.colour.getMetaValue());
	}

	@Override
	public TileEntity createNewTileEntity(World worldIn, int meta)
	{
		return new TileEntityGlowPanel();
	}
	
	@Override
	public boolean canRenderInLayer(IBlockState state, BlockRenderLayer layer)
	{
		return true;
	}
	
	@Override
    public EnumBlockRenderType getRenderType(IBlockState state) 
	{
        return EnumBlockRenderType.MODEL;
    }

    @Override
    public boolean isBlockNormalCube(IBlockState state) 
    {
        return false;
    }

    @Override
    public boolean isOpaqueCube(IBlockState state) 
    {
        return false;
    }

    @Override
    public boolean isFullCube(IBlockState state)
    {
        return false;
    }

    @Override
    public boolean isFullBlock(IBlockState state)
    {
        return false;
    }
}
