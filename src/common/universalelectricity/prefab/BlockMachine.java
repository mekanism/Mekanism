package universalelectricity.prefab;

import java.util.Random;

import net.minecraft.src.BlockContainer;
import net.minecraft.src.CreativeTabs;
import net.minecraft.src.EntityItem;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.IInventory;
import net.minecraft.src.ItemStack;
import net.minecraft.src.Material;
import net.minecraft.src.NBTTagCompound;
import net.minecraft.src.TileEntity;
import net.minecraft.src.World;
import universalelectricity.implement.IItemElectric;
import buildcraft.api.tools.IToolWrench;

/**
 * A block you may extend from to create your machine blocks! You do not have to
 * extend from this block if you do not want to. It's optional but it comes with
 * some useful functions that will make coding easier for you.
 */
public abstract class BlockMachine extends BlockContainer
{
	public BlockMachine(String name, int id, Material material)
	{
		super(id, material);
		this.setBlockName(name);
		this.setHardness(0.5F);
	}

	public BlockMachine(String name, int id, Material material, CreativeTabs creativeTab)
	{
		this(name, id, material);
		this.setCreativeTab(creativeTab);
	}

	@Override
	public int damageDropped(int metadata)
	{
		return metadata;
	}

	/**
	 * Returns the quantity of items to drop on block destruction.
	 */
	@Override
	public int quantityDropped(Random par1Random)
	{
		return 1;
	}

	/**
	 * Returns the ID of the items to drop on destruction.
	 */
	@Override
	public int idDropped(int par1, Random par2Random, int par3)
	{
		return this.blockID;
	}

	/**
	 * DO NOT OVERRIDE THIS FUNCTION! Called when the block is right clicked by
	 * the player. This modified version detects electric items and wrench
	 * actions on your machine block. Do not override this function. Use
	 * machineActivated instead! (It does the same thing)
	 */
	@Override
	public boolean onBlockActivated(World par1World, int x, int y, int z, EntityPlayer par5EntityPlayer, int par6, float par7, float par8, float par9)
	{
		int metadata = par1World.getBlockMetadata(x, y, z);

		/**
		 * Check if the player is holding a wrench or an electric item. If so,
		 * do not open the GUI.
		 */
		if (par5EntityPlayer.inventory.getCurrentItem() != null)
		{
			if (par5EntityPlayer.inventory.getCurrentItem().getItem() instanceof IToolWrench)
			{
				par1World.notifyBlocksOfNeighborChange(x, y, z, this.blockID);
				((IToolWrench) par5EntityPlayer.inventory.getCurrentItem().getItem()).wrenchUsed(par5EntityPlayer, x, y, z);

				if (par5EntityPlayer.isSneaking())
				{
					return this.onSneakUseWrench(par1World, x, y, z, par5EntityPlayer);
				}
				else
				{
					return this.onUseWrench(par1World, x, y, z, par5EntityPlayer);
				}
			}
			else if (par5EntityPlayer.inventory.getCurrentItem().getItem() instanceof IItemElectric)
			{
				if (this.onUseElectricItem(par1World, x, y, z, par5EntityPlayer)) { return true; }
			}
		}

		if (par5EntityPlayer.isSneaking())
		{
			return this.onSneakMachineActivated(par1World, x, y, z, par5EntityPlayer);
		}
		else
		{
			return this.onMachineActivated(par1World, x, y, z, par5EntityPlayer);
		}
	}

	/**
	 * Called when the machine is right clicked by the player
	 * 
	 * @return True if something happens
	 */
	public boolean onMachineActivated(World par1World, int x, int y, int z, EntityPlayer par5EntityPlayer)
	{
		return false;
	}

	/**
	 * Called when the machine is right clicked by the player while sneaking
	 * (shift clicking)
	 * 
	 * @return True if something happens
	 */
	public boolean onSneakMachineActivated(World par1World, int x, int y, int z, EntityPlayer par5EntityPlayer)
	{
		return false;
	}

	/**
	 * Called when a player uses an electric item on the machine
	 * 
	 * @return True if some happens
	 */
	public boolean onUseElectricItem(World par1World, int x, int y, int z, EntityPlayer par5EntityPlayer)
	{
		return false;
	}

	/**
	 * Called when a player uses a wrench on the machine
	 * 
	 * @return True if some happens
	 */
	public boolean onUseWrench(World par1World, int x, int y, int z, EntityPlayer par5EntityPlayer)
	{
		return false;
	}

	/**
	 * Called when a player uses a wrench on the machine while sneaking
	 * 
	 * @return True if some happens
	 */
	public boolean onSneakUseWrench(World par1World, int x, int y, int z, EntityPlayer par5EntityPlayer)
	{
		return this.onUseWrench(par1World, x, y, z, par5EntityPlayer);
	}

	/**
	 * Returns the TileEntity used by this block. You should use the metadata
	 * sensitive version of this to get the maximum optimization!
	 */
	@Override
	public TileEntity createNewTileEntity(World var1)
	{
		return null;
	}

	/**
	 * Override this if you don't need it. This will eject all items out of this
	 * machine if it has an inventory
	 */
	@Override
	public void breakBlock(World par1World, int x, int y, int z, int par5, int par6)
	{
		TileEntity tileEntity = par1World.getBlockTileEntity(x, y, z);

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
							EntityItem var12 = new EntityItem(par1World, (x + var8), (y + var9), (z + var10), new ItemStack(var7.itemID, var11, var7.getItemDamage()));

							if (var7.hasTagCompound())
							{
								var12.item.setTagCompound((NBTTagCompound) var7.getTagCompound().copy());
							}

							float var13 = 0.05F;
							var12.motionX = ((float) random.nextGaussian() * var13);
							var12.motionY = ((float) random.nextGaussian() * var13 + 0.2F);
							var12.motionZ = ((float) random.nextGaussian() * var13);
							par1World.spawnEntityInWorld(var12);
						}
					}
				}
			}
		}

		super.breakBlock(par1World, x, y, z, par5, par6);
	}
}
