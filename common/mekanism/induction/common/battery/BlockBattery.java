/**
 * 
 */
package mekanism.induction.common.battery;

import mekanism.api.induction.ICapacitor;
import mekanism.induction.client.render.BlockRenderingHandler;
import mekanism.induction.common.MekanismInduction;
import mekanism.induction.common.base.BlockBase;
import mekanism.induction.common.base.ListUtil;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
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
public class BlockBattery extends BlockBase implements ITileEntityProvider
{
	public BlockBattery(int id)
	{
		super("battery", id);
		this.setTextureName(MekanismInduction.PREFIX + "machine");
	}

	@Override
	public void onBlockClicked(World world, int x, int y, int z, EntityPlayer entityPlayer)
	{
		if (!world.isRemote)
		{
			if (!entityPlayer.capabilities.isCreativeMode)
			{
				TileEntityBattery tileEntity = (TileEntityBattery) world.getBlockTileEntity(x, y, z);
				ItemStack itemStack = ListUtil.getTop(tileEntity.structure.inventory);

				if (tileEntity.structure.inventory.remove(itemStack))
				{
					entityPlayer.dropPlayerItem(itemStack);
					tileEntity.updateAllClients();
				}
			}
		}
	}

	@Override
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer entityPlayer, int side, float xClick, float yClick, float zClick)
	{
		TileEntityBattery tileEntity = (TileEntityBattery) world.getBlockTileEntity(x, y, z);

		if (entityPlayer.isSneaking())
		{
			boolean result = tileEntity.toggleSide(ForgeDirection.getOrientation(side));

			if (!world.isRemote)
			{
				entityPlayer.addChatMessage("Toggled side to: " + (result ? "input" : "output"));
			}

			return true;
		}
		else
		{
			if (entityPlayer.getCurrentEquippedItem() != null)
			{
				if (entityPlayer.getCurrentEquippedItem().getItem() instanceof ICapacitor)
				{
					if (side != 0 && side != 1)
					{
						if (!world.isRemote)
						{
							if (tileEntity.structure.addCell(entityPlayer.getCurrentEquippedItem()))
							{
								entityPlayer.inventory.setInventorySlotContents(entityPlayer.inventory.currentItem, null);
								tileEntity.updateAllClients();
							}
						}
					}
				}
			}
		}

		if (!world.isRemote)
		{
			if(entityPlayer.getCurrentEquippedItem() != null && entityPlayer.getCurrentEquippedItem().itemID == MekanismInduction.blockBattery.blockID)
			{
				if(!tileEntity.structure.isMultiblock)
				{
					return false;
				}
			}
			
			entityPlayer.openGui(MekanismInduction.instance, 0, world, x, y, z);
		}

		return true;
	}

	@Override
	public void onNeighborBlockChange(World world, int x, int y, int z, int id)
	{
		if (!world.isRemote)
		{
			if (id == blockID)
			{
				TileEntityBattery battery = (TileEntityBattery) world.getBlockTileEntity(x, y, z);

				battery.update();
			}
		}
	}

	@Override
	public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase entityliving, ItemStack itemstack)
	{
		if (!world.isRemote)
		{
			TileEntityBattery battery = (TileEntityBattery) world.getBlockTileEntity(x, y, z);

			battery.update();
		}
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
	public boolean removeBlockByPlayer(World world, EntityPlayer player, int x, int y, int z)
	{
		if (!world.isRemote && canHarvestBlock(player, world.getBlockMetadata(x, y, z)))
		{
			TileEntityBattery tileEntity = (TileEntityBattery) world.getBlockTileEntity(x, y, z);

			if (!tileEntity.structure.isMultiblock)
			{
				for (ItemStack itemStack : tileEntity.structure.inventory)
				{
					float motion = 0.7F;
					double motionX = (world.rand.nextFloat() * motion) + (1.0F - motion) * 0.5D;
					double motionY = (world.rand.nextFloat() * motion) + (1.0F - motion) * 0.5D;
					double motionZ = (world.rand.nextFloat() * motion) + (1.0F - motion) * 0.5D;

					EntityItem entityItem = new EntityItem(world, x + motionX, y + motionY, z + motionZ, itemStack);

					world.spawnEntityInWorld(entityItem);
				}
			}
		}

		return super.removeBlockByPlayer(world, player, x, y, z);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public int getRenderType()
	{
		return BlockRenderingHandler.INSTANCE.getRenderId();
	}

	@Override
	public TileEntity createNewTileEntity(World world)
	{
		return new TileEntityBattery();
	}
}
