package mekanism.common.item;

import java.util.List;

import mekanism.api.Coord4D;
import mekanism.api.EnumColor;
import mekanism.api.Pos3D;
import mekanism.common.entity.EntityBalloon;
import mekanism.common.util.MekanismUtils;

import net.minecraft.block.BlockDispenser;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.dispenser.BehaviorDefaultDispenseItem;
import net.minecraft.dispenser.IBlockSource;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ItemBalloon extends ItemMekanism
{
	public ItemBalloon()
	{
		super();
		setHasSubtypes(true);
		BlockDispenser.dispenseBehaviorRegistry.putObject(this, new DispenserBehavior());
	}

	public EnumColor getColor(ItemStack stack)
	{
		return EnumColor.DYES[stack.getItemDamage()];
	}

	@Override
	public void getSubItems(Item item, CreativeTabs tabs, List list)
	{
		for(int i = 0; i < EnumColor.DYES.length; i++)
		{
			EnumColor color = EnumColor.DYES[i];

			if(color != null)
			{
				ItemStack stack = new ItemStack(this);
				stack.setItemDamage(i);
				list.add(stack);
			}
		}
	}

	@Override
	public ItemStack onItemRightClick(ItemStack itemstack, World world, EntityPlayer entityplayer)
	{
		if(!world.isRemote)
		{
			Pos3D pos = new Pos3D();
			pos.zPos += 0.3;
			pos.xPos -= 0.4;
			pos.rotateYaw(entityplayer.renderYawOffset);
			pos.translate(new Pos3D(entityplayer));

			world.spawnEntityInWorld(new EntityBalloon(world, pos.xPos-0.5, pos.yPos-0.25, pos.zPos-0.5, getColor(itemstack)));
		}

		itemstack.stackSize--;

		return itemstack;
	}

	@Override
	public String getItemStackDisplayName(ItemStack stack)
	{
		String color = getColor(stack).getDyedName();

		if(getColor(stack) == EnumColor.BLACK)
		{
			color = EnumColor.DARK_GREY + getColor(stack).getDyeName();
		}

		return color + " " + MekanismUtils.localize("tooltip.balloon");
	}

	@Override
	public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ)
	{
		if(player.isSneaking())
		{
			AxisAlignedBB bound = AxisAlignedBB.getBoundingBox(x, y, z, x+1, y+3, z+1);

			List<EntityBalloon> balloonsNear = player.worldObj.getEntitiesWithinAABB(EntityBalloon.class, bound);

			if(balloonsNear.size() > 0)
			{
				return true;
			}

			Coord4D obj = new Coord4D(x, y, z, world.provider.dimensionId);

			if(obj.getBlock(world).isReplaceable(world, x, y, z))
			{
				obj.yCoord--;
			}
			
			if(!world.isSideSolid(x, y, z, ForgeDirection.UP))
			{
				return true;
			}

			if(canReplace(world, obj.xCoord, obj.yCoord+1, obj.zCoord) && canReplace(world, obj.xCoord, obj.yCoord+2, obj.zCoord))
			{
				world.setBlockToAir(obj.xCoord, obj.yCoord+1, obj.zCoord);
				world.setBlockToAir(obj.xCoord, obj.yCoord+2, obj.zCoord);

				if(!world.isRemote)
				{
					world.spawnEntityInWorld(new EntityBalloon(world, obj, getColor(stack)));
					stack.stackSize--;
				}
			}

			return true;
		}

		return false;
	}

	@Override
	public boolean itemInteractionForEntity(ItemStack stack, EntityPlayer player, EntityLivingBase entity)
	{
		if(player.isSneaking())
		{
			if(!player.worldObj.isRemote)
			{
				AxisAlignedBB bound = AxisAlignedBB.getBoundingBox(entity.posX - 0.2, entity.posY - 0.5, entity.posZ - 0.2, entity.posX + 0.2, entity.posY + entity.ySize + 4, entity.posZ + 0.2);

				List<EntityBalloon> balloonsNear = player.worldObj.getEntitiesWithinAABB(EntityBalloon.class, bound);

				for(EntityBalloon balloon : balloonsNear)
				{
					if(balloon.latchedEntity == entity)
					{
						return true;
					}
				}

				player.worldObj.spawnEntityInWorld(new EntityBalloon(entity, getColor(stack)));
				stack.stackSize--;
			}

			return true;
		}

		return false;
	}

	private boolean canReplace(World world, int x, int y, int z)
	{
		return world.isAirBlock(x, y, z) || world.getBlock(x, y, z).isReplaceable(world, x, y, z);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(IIconRegister register) {}
	
	public class DispenserBehavior extends BehaviorDefaultDispenseItem
	{
		@Override
		public ItemStack dispenseStack(IBlockSource source, ItemStack stack)
		{
			Coord4D coord = new Coord4D(source.getXInt(), source.getYInt(), source.getZInt(), source.getWorld().provider.dimensionId);
			ForgeDirection side = ForgeDirection.getOrientation(BlockDispenser.func_149937_b(source.getBlockMetadata()).ordinal());

			List<EntityLivingBase> entities = source.getWorld().getEntitiesWithinAABB(EntityLivingBase.class, coord.getFromSide(side).getBoundingBox());
			boolean latched = false;
			
			for(EntityLivingBase entity : entities)
			{
				AxisAlignedBB bound = AxisAlignedBB.getBoundingBox(entity.posX - 0.2, entity.posY - 0.5, entity.posZ - 0.2, entity.posX + 0.2, entity.posY + entity.ySize + 4, entity.posZ + 0.2);

				List<EntityBalloon> balloonsNear = source.getWorld().getEntitiesWithinAABB(EntityBalloon.class, bound);
				boolean hasBalloon = false;
				
				for(EntityBalloon balloon : balloonsNear)
				{
					if(balloon.latchedEntity == entity)
					{
						hasBalloon = true;
					}
				}
				
				if(!hasBalloon)
				{
					source.getWorld().spawnEntityInWorld(new EntityBalloon(entity, getColor(stack)));
					latched = true;
				}
			}
			
			if(!latched)
			{
				Pos3D pos = new Pos3D(coord);
				
				switch(side)
				{
					case DOWN:
						pos.translate(0, -2.5, 0);
						break;
					case UP:
						pos.translate(0, 0, 0);
						break;
					case NORTH:
						pos.translate(0, -1, -0.5);
						break;
					case SOUTH:
						pos.translate(0, -1, 0.5);
						break;
					case WEST:
						pos.translate(-0.5, -1, 0);
						break;
					case EAST:
						pos.translate(0.5, -1, 0);
						break;
					default:
						break;
				}
				
				if(!source.getWorld().isRemote)
				{
					source.getWorld().spawnEntityInWorld(new EntityBalloon(source.getWorld(), pos.xPos, pos.yPos, pos.zPos, getColor(stack)));
				}
			}
			
			stack.stackSize--;
			return stack;
		}
	}
}
