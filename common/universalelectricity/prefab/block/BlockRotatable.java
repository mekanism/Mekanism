package universalelectricity.prefab.block;

import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;

/**
 * A block that can rotate based on placed position and wrenching.
 * 
 * @author Calclavia
 */
public abstract class BlockRotatable extends BlockTile implements IRotatableBlock
{
	protected byte rotationMask = 60;

	public BlockRotatable(int id, Material material)
	{
		super(id, material);
	}

	@Override
	public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase entityLiving, ItemStack itemStack)
	{
		int angle = MathHelper.floor_double((entityLiving.rotationYaw * 4.0F / 360.0F) + 0.5D) & 3;
		int change = 3;

		switch (angle)
		{
			case 0:
				change = 2;
				break;

			case 1:
				change = 5;
				break;

			case 2:
				change = 3;
				break;

			case 3:
				change = 4;
				break;
		}

		world.setBlockMetadataWithNotify(x, y, z, change, 3);
	}

	@Override
	public boolean onUseWrench(World world, int x, int y, int z, EntityPlayer par5EntityPlayer, int side, float hitX, float hitY, float hitZ)
	{
		return this.rotateBlock(world, x, y, z, ForgeDirection.getOrientation(side));
	}

	@Override
	public boolean rotateBlock(World worldObj, int x, int y, int z, ForgeDirection axis)
	{
		int currentRotMeta = worldObj.getBlockMetadata(x, y, z);
		ForgeDirection orientation = ForgeDirection.getOrientation(currentRotMeta);
		ForgeDirection rotated = orientation.getRotation(axis);
		int rmeta = rotated.ordinal();
		int rmetaBit = 1 << rmeta;
		System.out.println(rmetaBit + ": " + (rmetaBit & this.rotationMask));
		if ((rmetaBit & this.rotationMask) == rmetaBit)
		{
			worldObj.setBlockMetadataWithNotify(x, y, z, rmeta, 3);
			return true;
		}

		return false;
	}

	@Override
	public ForgeDirection getDirection(World world, int x, int y, int z)
	{
		return ForgeDirection.getOrientation(world.getBlockMetadata(x, y, z));
	}

	@Override
	public void setDirection(World world, int x, int y, int z, ForgeDirection direction)
	{
		world.setBlockMetadataWithNotify(x, y, z, direction.ordinal(), 3);
	}
}