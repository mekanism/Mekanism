package mekanism.induction.common.block;

import mekanism.common.Mekanism;
import mekanism.common.Object3D;
import mekanism.induction.client.render.BlockRenderingHandler;
import mekanism.induction.common.item.ItemCoordLink;
import mekanism.induction.common.tileentity.TileEntityEMContractor;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class BlockEMContractor extends Block implements ITileEntityProvider
{
	public BlockEMContractor(int id)
	{
		super(id, Material.piston);
		setCreativeTab(Mekanism.tabMekanism);
		setTextureName("mekanism:machine");
		setHardness(5F);
		setResistance(10F);
	}

	@SideOnly(Side.CLIENT)
	@Override
	public int getRenderType()
	{
		return BlockRenderingHandler.INSTANCE.getRenderId();
	}

	@Override
	public boolean onBlockActivated(World world, int par2, int par3, int par4, EntityPlayer entityPlayer, int par6, float par7, float par8, float par9)
	{
		TileEntityEMContractor contractor = (TileEntityEMContractor) world.getBlockTileEntity(par2, par3, par4);

		if(entityPlayer.getCurrentEquippedItem() != null)
		{
			if(entityPlayer.getCurrentEquippedItem().itemID == Item.dyePowder.itemID)
			{
				contractor.setDye(entityPlayer.getCurrentEquippedItem().getItemDamage());

				if(!entityPlayer.capabilities.isCreativeMode)
				{
					entityPlayer.inventory.decrStackSize(entityPlayer.inventory.currentItem, 1);
				}
				
				return true;
			}
			else if(entityPlayer.getCurrentEquippedItem().getItem() instanceof ItemCoordLink)
			{
				ItemCoordLink link = ((ItemCoordLink) entityPlayer.getCurrentEquippedItem().getItem());
				Object3D linkVec = link.getLink(entityPlayer.getCurrentEquippedItem());

				if(linkVec != null)
				{
					if(linkVec.getTileEntity(world) instanceof TileEntityEMContractor)
					{
						contractor.setLink((TileEntityEMContractor) linkVec.getTileEntity(world), true);

						if(world.isRemote)
						{
							entityPlayer.addChatMessage("Linked " + getLocalizedName() + " with " + " [" + (int) linkVec.xCoord + ", " + (int) linkVec.yCoord + ", " + (int) linkVec.zCoord + "]");
						}

						link.clearLink(entityPlayer.getCurrentEquippedItem());

						return true;
					}
				}

				return false;
			}
		}

		if(!entityPlayer.isSneaking())
		{
			contractor.incrementFacing();
		}
		else {
			contractor.suck = !contractor.suck;
			contractor.updatePath();
		}

		return true;
	}

	@Override
	public void onNeighborBlockChange(World world, int x, int y, int z, int blockID)
	{
		TileEntityEMContractor tileContractor = (TileEntityEMContractor) world.getBlockTileEntity(x, y, z);

		if(!world.isRemote && !tileContractor.isLatched())
		{
			for(ForgeDirection side : ForgeDirection.VALID_DIRECTIONS)
			{
				TileEntity tileEntity = world.getBlockTileEntity(x + side.offsetX, y + side.offsetY, z + side.offsetZ);

				if(tileEntity instanceof IInventory)
				{
					tileContractor.setFacing(side.getOpposite());
					return;
				}
			}
		}
	}

	@Override
	public TileEntity createNewTileEntity(World world)
	{
		return new TileEntityEMContractor();
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
}
