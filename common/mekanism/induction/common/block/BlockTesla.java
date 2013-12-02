/**
 * 
 */
package mekanism.induction.common.block;

import mekanism.api.EnumColor;
import mekanism.api.Object3D;
import mekanism.common.Mekanism;
import mekanism.common.item.ItemConfigurator;
import mekanism.common.util.MekanismUtils;
import mekanism.induction.client.render.BlockRenderingHandler;
import mekanism.induction.common.tileentity.TileEntityTesla;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import universalelectricity.core.vector.Vector3;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * @author Calclavia
 * 
 */
public class BlockTesla extends Block implements ITileEntityProvider
{
	public BlockTesla(int id)
	{
		super(id, Material.piston);
		setCreativeTab(Mekanism.tabMekanism);
		setTextureName("mekanism:machine");
		setHardness(5F);
		setResistance(10F);
	}

	@Override
	public void onBlockAdded(World world, int x, int y, int z)
	{
		super.onBlockAdded(world, x, y, z);
		TileEntity tileEntity = world.getBlockTileEntity(x, y, z);
		((TileEntityTesla) tileEntity).updatePositionStatus();
	}

	@Override
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int par6, float par7, float par8, float par9)
	{
		TileEntity t = world.getBlockTileEntity(x, y, z);
		TileEntityTesla tileEntity = ((TileEntityTesla) t).getControllingTelsa();

		if(player.getCurrentEquippedItem() != null)
		{
			if(player.getCurrentEquippedItem().itemID == Item.dyePowder.itemID)
			{
				tileEntity.setDye(player.getCurrentEquippedItem().getItemDamage());

				if(!player.capabilities.isCreativeMode)
				{
					player.inventory.decrStackSize(player.inventory.currentItem, 1);
				}

				return true;
			}
			else if(player.getCurrentEquippedItem().itemID == Item.redstone.itemID)
			{
				boolean status = tileEntity.toggleEntityAttack();

				if(!player.capabilities.isCreativeMode)
				{
					player.inventory.decrStackSize(player.inventory.currentItem, 1);
				}

				if(!world.isRemote)
				{
					player.addChatMessage("Toggled entity attack to: " + status);
				}

				return true;
			}
			else if(player.getCurrentEquippedItem().getItem() instanceof ItemConfigurator)
			{
				if(tileEntity.linked == null)
				{
					ItemConfigurator item = ((ItemConfigurator)player.getCurrentEquippedItem().getItem());
					
					if(item.getState(player.getCurrentEquippedItem()) == 3)
					{
						Object3D linkObj = item.getLink(player.getCurrentEquippedItem());
	
						if(linkObj != null)
						{
							if(!world.isRemote)
							{
								int dimID = item.getLink(player.getCurrentEquippedItem()).dimensionId;
								World otherWorld = MinecraftServer.getServer().worldServerForDimension(dimID);
	
								if(linkObj.getTileEntity(otherWorld) instanceof TileEntityTesla)
								{
									tileEntity.setLink(new Vector3(((TileEntityTesla)linkObj.getTileEntity(otherWorld)).getTopTelsa()), dimID, true);
									
									player.addChatMessage(EnumColor.DARK_BLUE + "[Mekanism]" + EnumColor.GREY + " " + MekanismUtils.localize("text.tesla.success") + "!");
	
									item.clearLink(player.getCurrentEquippedItem());
									world.playSoundEffect(x + 0.5, y + 0.5, z + 0.5, "ambient.weather.thunder", 5, 1);
	
									return true;
								}
							}
						}
					}
				}
				else {
					tileEntity.setLink(null, world.provider.dimensionId, true);

					if(!world.isRemote)
					{
						player.addChatMessage("Unlinked Tesla.");
					}

					return true;
				}
			}
		}
		else {
			boolean receiveMode = tileEntity.toggleReceive();

			if(world.isRemote)
			{
				player.addChatMessage("Tesla receive mode is now " + receiveMode);
			}
			
			return true;
		}

		return false;
	}

	@Override
	public void onNeighborBlockChange(World world, int x, int y, int z, int id)
	{
		TileEntity tileEntity = world.getBlockTileEntity(x, y, z);
		((TileEntityTesla) tileEntity).updatePositionStatus();
	}

	@Override
	public TileEntity createNewTileEntity(World world)
	{
		return new TileEntityTesla();
	}

	@Override
	@SideOnly(Side.CLIENT)
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
}
