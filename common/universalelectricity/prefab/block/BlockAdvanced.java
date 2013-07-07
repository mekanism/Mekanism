package universalelectricity.prefab.block;

import java.lang.reflect.Method;
import java.util.Random;

import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

/**
 * An advanced block class that is to be extended for wrenching capabilities.
 */
public abstract class BlockAdvanced extends BlockContainer
{
	public BlockAdvanced(int id, Material material)
	{
		super(id, material);
		this.setHardness(0.6f);
	}

	/**
	 * DO NOT OVERRIDE THIS FUNCTION! Called when the block is right clicked by the player. This
	 * modified version detects electric items and wrench actions on your machine block. Do not
	 * override this function. Use onMachineActivated instead! (It does the same thing)
	 * 
	 * @param world The World Object.
	 * @param x , y, z The coordinate of the block.
	 * @param side The side the player clicked on.
	 * @param hitX , hitY, hitZ The position the player clicked on relative to the block.
	 */
	@Override
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer entityPlayer, int side, float hitX, float hitY, float hitZ)
	{
		int metadata = world.getBlockMetadata(x, y, z);

		/**
		 * Check if the player is holding a wrench or an electric item. If so, call the wrench
		 * event.
		 */
		if (this.isUsableWrench(entityPlayer, entityPlayer.inventory.getCurrentItem(), x, y, z))
		{
			this.damageWrench(entityPlayer, entityPlayer.inventory.getCurrentItem(), x, y, z);

			if (entityPlayer.isSneaking())
			{
				if (this.onSneakUseWrench(world, x, y, z, entityPlayer, side, hitX, hitY, hitZ))
				{
					return true;
				}
			}

			if (this.onUseWrench(world, x, y, z, entityPlayer, side, hitX, hitY, hitZ))
			{
				return true;
			}
		}

		if (entityPlayer.isSneaking())
		{
			if (this.onSneakMachineActivated(world, x, y, z, entityPlayer, side, hitX, hitY, hitZ))
			{
				return true;
			}
		}

		return this.onMachineActivated(world, x, y, z, entityPlayer, side, hitX, hitY, hitZ);
	}

	/**
	 * A function that denotes if an itemStack is a wrench that can be used. Override this for more
	 * wrench compatibility. Compatible with Buildcraft and IC2 wrench API via reflection.
	 * 
	 * @return True if it is a wrench.
	 */
	public boolean isUsableWrench(EntityPlayer entityPlayer, ItemStack itemStack, int x, int y, int z)
	{
		if (entityPlayer != null && itemStack != null)
		{
			Class wrenchClass = itemStack.getItem().getClass();

			/**
			 * UE and Buildcraft
			 */
			try
			{
				Method methodCanWrench = wrenchClass.getMethod("canWrench", EntityPlayer.class, Integer.TYPE, Integer.TYPE, Integer.TYPE);
				return (Boolean) methodCanWrench.invoke(itemStack.getItem(), entityPlayer, x, y, z);
			}
			catch (NoClassDefFoundError e)
			{
			}
			catch (Exception e)
			{
			}

			/**
			 * Industrialcraft
			 */
			try
			{
				if (wrenchClass == Class.forName("ic2.core.item.tool.ItemToolWrench") || wrenchClass == Class.forName("ic2.core.item.tool.ItemToolWrenchElectric"))
				{
					return itemStack.getItemDamage() < itemStack.getMaxDamage();
				}
			}
			catch (Exception e)
			{
			}
		}

		return false;
	}

	/**
	 * This function damages a wrench. Works with Buildcraft and Industrialcraft wrenches.
	 * 
	 * @return True if damage was successfull.
	 */
	public boolean damageWrench(EntityPlayer entityPlayer, ItemStack itemStack, int x, int y, int z)
	{
		if (this.isUsableWrench(entityPlayer, itemStack, x, y, z))
		{
			Class wrenchClass = itemStack.getItem().getClass();

			/**
			 * UE and Buildcraft
			 */
			try
			{
				Method methodWrenchUsed = wrenchClass.getMethod("wrenchUsed", EntityPlayer.class, Integer.TYPE, Integer.TYPE, Integer.TYPE);
				methodWrenchUsed.invoke(itemStack.getItem(), entityPlayer, x, y, z);
				return true;
			}
			catch (Exception e)
			{
			}

			/**
			 * Industrialcraft
			 */
			try
			{
				if (wrenchClass == Class.forName("ic2.core.item.tool.ItemToolWrench") || wrenchClass == Class.forName("ic2.core.item.tool.ItemToolWrenchElectric"))
				{
					Method methodWrenchDamage = wrenchClass.getMethod("damage", ItemStack.class, Integer.TYPE, EntityPlayer.class);
					methodWrenchDamage.invoke(itemStack.getItem(), itemStack, 1, entityPlayer);
					return true;
				}
			}
			catch (Exception e)
			{
			}
		}

		return false;
	}

	/**
	 * Called when the machine is right clicked by the player
	 * 
	 * @return True if something happens
	 */
	public boolean onMachineActivated(World world, int x, int y, int z, EntityPlayer entityPlayer, int side, float hitX, float hitY, float hitZ)
	{
		return false;
	}

	/**
	 * Called when the machine is being wrenched by a player while sneaking.
	 * 
	 * @return True if something happens
	 */
	public boolean onSneakMachineActivated(World world, int x, int y, int z, EntityPlayer entityPlayer, int side, float hitX, float hitY, float hitZ)
	{
		return false;
	}

	/**
	 * Called when a player uses a wrench on the machine
	 * 
	 * @return True if some happens
	 */
	public boolean onUseWrench(World world, int x, int y, int z, EntityPlayer entityPlayer, int side, float hitX, float hitY, float hitZ)
	{
		return false;
	}

	/**
	 * Called when a player uses a wrench on the machine while sneaking. Only works with the UE
	 * wrench.
	 * 
	 * @return True if some happens
	 */
	public boolean onSneakUseWrench(World world, int x, int y, int z, EntityPlayer entityPlayer, int side, float hitX, float hitY, float hitZ)
	{
		return this.onUseWrench(world, x, y, z, entityPlayer, side, hitX, hitY, hitZ);
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

	@Override
	public void breakBlock(World world, int x, int y, int z, int par5, int par6)
	{
		this.dropEntireInventory(world, x, y, z, par5, par6);
		super.breakBlock(world, x, y, z, par5, par6);
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
}
