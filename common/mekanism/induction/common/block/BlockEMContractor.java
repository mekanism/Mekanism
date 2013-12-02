package mekanism.induction.common.block;

import mekanism.api.EnumColor;
import mekanism.api.Object3D;
import mekanism.common.Mekanism;
import mekanism.common.item.ItemConfigurator;
import mekanism.common.util.MekanismUtils;
import mekanism.induction.client.render.BlockRenderingHandler;
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
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float posX, float posY, float posZ)
	{
		TileEntityEMContractor contractor = (TileEntityEMContractor)world.getBlockTileEntity(x, y, z);

		if(player.getCurrentEquippedItem() != null)
		{
			if(player.getCurrentEquippedItem().itemID == Item.dyePowder.itemID)
			{
				contractor.setDye(player.getCurrentEquippedItem().getItemDamage());

				if(!player.capabilities.isCreativeMode)
				{
					player.inventory.decrStackSize(player.inventory.currentItem, 1);
				}
				
				return true;
			}
			else if(player.getCurrentEquippedItem().getItem() instanceof ItemConfigurator)
			{
				ItemConfigurator item = ((ItemConfigurator)player.getCurrentEquippedItem().getItem());
				
				if(item.getState(player.getCurrentEquippedItem()) == 3)
				{
					Object3D linkVec = item.getLink(player.getCurrentEquippedItem());
	
					if(linkVec != null)
					{
						if(linkVec.getTileEntity(world) instanceof TileEntityEMContractor)
						{
							contractor.setLink((TileEntityEMContractor)linkVec.getTileEntity(world), true);
	
							if(world.isRemote)
							{
								player.addChatMessage(EnumColor.DARK_BLUE + "[Mekanism]" + EnumColor.GREY + " " + MekanismUtils.localize("text.contractor.success") + "!");
							}
	
							item.clearLink(player.getCurrentEquippedItem());
	
							return true;
						}
					}
				}

				return false;
			}
		}

		if(!player.isSneaking())
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
		TileEntityEMContractor tileContractor = (TileEntityEMContractor)world.getBlockTileEntity(x, y, z);

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
