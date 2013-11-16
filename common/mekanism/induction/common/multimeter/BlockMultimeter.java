/**
 * 
 */
package mekanism.induction.common.multimeter;

import mekanism.induction.client.render.BlockRenderingHandler;
import mekanism.induction.common.MekanismInduction;
import mekanism.induction.common.base.BlockBase;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * A block that detects power.
 * 
 * @author Calclavia
 * 
 */
public class BlockMultimeter extends BlockBase implements ITileEntityProvider
{
	public BlockMultimeter(int id)
	{
		super("multimeter", id);
		this.setTextureName(MekanismInduction.PREFIX + "machine");
	}

	@Override
	public AxisAlignedBB getCollisionBoundingBoxFromPool(World par1World, int par2, int par3, int par4)
	{
		return null;
	}

	@Override
	public MovingObjectPosition collisionRayTrace(World par1World, int par2, int par3, int par4, Vec3 par5Vec3, Vec3 par6Vec3)
	{
		int metadata = par1World.getBlockMetadata(par2, par3, par4) & 7;
		float thickness = 0.15f;

		if (metadata == 0)
		{
			this.setBlockBounds(0, 0, 0, 1, thickness, 1);
		}
		else if (metadata == 1)
		{
			this.setBlockBounds(0, 1 - thickness, 0, 1, 1, 1);
		}
		else if (metadata == 2)
		{
			this.setBlockBounds(0, 0, 0, 1, 1, thickness);
		}
		else if (metadata == 3)
		{
			this.setBlockBounds(0, 0, 1 - thickness, 1, 1, 1);
		}
		else if (metadata == 4)
		{
			this.setBlockBounds(0, 0, 0, thickness, 1, 1);
		}
		else if (metadata == 5)
		{
			this.setBlockBounds(1 - thickness, 0, 0, 1, 1, 1);
		}

		return super.collisionRayTrace(par1World, par2, par3, par4, par5Vec3, par6Vec3);
	}

	/**
	 * Called when a block is placed using its ItemBlock. Args: World, X, Y, Z, side, hitX, hitY,
	 * hi@OverridetZ, block metadata
	 */
	@Override
	public int onBlockPlaced(World par1World, int par2, int par3, int par4, int side, float hitX, float hitY, float hitZ, int metadata)
	{
		return ForgeDirection.getOrientation(side).getOpposite().ordinal();
	}

	@Override
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer entityPlayer, int side, float par7, float par8, float par9)
	{
		if (entityPlayer.isSneaking())
		{
			world.setBlockMetadataWithNotify(x, y, z, ForgeDirection.ROTATION_MATRIX[world.getBlockMetadata(x, y, z)][side], 3);
		}
		else
		{
			entityPlayer.openGui(MekanismInduction.instance, 0, world, x, y, z);
		}

		return true;
	}

	@Override
	public int isProvidingStrongPower(IBlockAccess blockAccess, int x, int y, int z, int par5)
	{
		return this.isProvidingWeakPower(blockAccess, x, y, z, par5);
	}

	@Override
	public int isProvidingWeakPower(IBlockAccess blockAccess, int x, int y, int z, int par5)
	{
		TileEntity tile = blockAccess.getBlockTileEntity(x, y, z);

		if (tile instanceof TileEntityMultimeter)
		{
			return ((TileEntityMultimeter) tile).redstoneOn ? 14 : 0;
		}
		return 0;
	}

	@Override
	public boolean canProvidePower()
	{
		return true;
	}

	@SideOnly(Side.CLIENT)
	@Override
	public int getRenderType()
	{
		return BlockRenderingHandler.INSTANCE.getRenderId();
	}

	@Override
	public boolean renderAsNormalBlock()
	{
		return false;
	}

	@Override
	public boolean isOpaqueCube()
	{
		return false;
	}

	@Override
	public TileEntity createNewTileEntity(World world)
	{
		return new TileEntityMultimeter();
	}
}
