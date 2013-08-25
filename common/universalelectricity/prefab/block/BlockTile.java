package universalelectricity.prefab.block;

import java.util.Random;

import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

/**
 * An advanced block class that is to be extended for wrenching capabilities.
 */
public abstract class BlockTile extends BlockAdvanced implements ITileEntityProvider
{
	public BlockTile(int id, Material material)
	{
		super(id, material);
		this.isBlockContainer = true;
	}

	/**
	 * Called whenever the block is added into the world. Args: world, x, y, z
	 */
	@Override
	public void onBlockAdded(World par1World, int par2, int par3, int par4)
	{
		super.onBlockAdded(par1World, par2, par3, par4);
	}

	/**
	 * ejects contained items into the world, and notifies neighbours of an update, as appropriate
	 */
	@Override
	public void breakBlock(World world, int x, int y, int z, int par5, int par6)
	{
		this.dropEntireInventory(world, x, y, z, par5, par6);
		super.breakBlock(world, x, y, z, par5, par6);
		world.removeBlockTileEntity(x, y, z);
	}

	/**
	 * Called when the block receives a BlockEvent - see World.addBlockEvent. By default, passes it
	 * on to the tile entity at this location. Args: world, x, y, z, blockID, EventID, event
	 * parameter
	 */
	@Override
	public boolean onBlockEventReceived(World par1World, int par2, int par3, int par4, int par5, int par6)
	{
		super.onBlockEventReceived(par1World, par2, par3, par4, par5, par6);
		TileEntity tileentity = par1World.getBlockTileEntity(par2, par3, par4);
		return tileentity != null ? tileentity.receiveClientEvent(par5, par6) : false;
	}

	/**
	 * Override this if you don't need it. This will eject all items out of this machine if it has
	 * an inventory.
	 */
	public void dropEntireInventory(World world, int x, int y, int z, int par5, int par6)
	{
		TileEntity tileEntity = world.getBlockTileEntity(x, y, z);

		if (tileEntity != null)
		{
			if (tileEntity instanceof IInventory)
			{
				IInventory inventory = (IInventory) tileEntity;

				for (int var6 = 0; var6 < inventory.getSizeInventory(); ++var6)
				{
					ItemStack var7 = inventory.getStackInSlot(var6);

					if (var7 != null)
					{
						Random random = new Random();
						float var8 = random.nextFloat() * 0.8F + 0.1F;
						float var9 = random.nextFloat() * 0.8F + 0.1F;
						float var10 = random.nextFloat() * 0.8F + 0.1F;

						while (var7.stackSize > 0)
						{
							int var11 = random.nextInt(21) + 10;

							if (var11 > var7.stackSize)
							{
								var11 = var7.stackSize;
							}

							var7.stackSize -= var11;
							EntityItem var12 = new EntityItem(world, (x + var8), (y + var9), (z + var10), new ItemStack(var7.itemID, var11, var7.getItemDamage()));

							if (var7.hasTagCompound())
							{
								var12.getEntityItem().setTagCompound((NBTTagCompound) var7.getTagCompound().copy());
							}

							float var13 = 0.05F;
							var12.motionX = ((float) random.nextGaussian() * var13);
							var12.motionY = ((float) random.nextGaussian() * var13 + 0.2F);
							var12.motionZ = ((float) random.nextGaussian() * var13);
							world.spawnEntityInWorld(var12);
						}
					}
				}
			}
		}
	}

	/**
	 * Returns the TileEntity used by this block. You should use the metadata sensitive version of
	 * this to get the maximum optimization!
	 */
	@Override
	public TileEntity createNewTileEntity(World var1)
	{
		return null;
	}

}
