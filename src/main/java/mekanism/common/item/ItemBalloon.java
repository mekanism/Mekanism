package mekanism.common.item;

import java.util.List;

import mekanism.api.Coord4D;
import mekanism.api.EnumColor;
import mekanism.api.Pos3D;
import mekanism.common.base.IMetaItem;
import mekanism.common.entity.EntityBalloon;
import mekanism.common.util.LangUtils;
import net.minecraft.block.BlockDispenser;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.dispenser.BehaviorDefaultDispenseItem;
import net.minecraft.dispenser.IBlockSource;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;

public class ItemBalloon extends ItemMekanism implements IMetaItem
{
	public ItemBalloon()
	{
		super();
		setHasSubtypes(true);
		BlockDispenser.dispenseBehaviorRegistry.putObject(this, new DispenserBehavior());
	}
	
	@Override
	public String getTexture(int meta)
	{
		return EnumColor.DYES[meta].getOreDictName() + "Balloon";
	}
	
	@Override
	public int getVariants()
	{
		return EnumColor.DYES.length;
	}

	public EnumColor getColor(ItemStack stack)
	{
		return EnumColor.DYES[stack.getItemDamage()];
	}

	@Override
	public void getSubItems(Item item, CreativeTabs tabs, List<ItemStack> list)
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
			Pos3D pos = new Pos3D(-0.4, 0, 0.3).rotateYaw(entityplayer.renderYawOffset).translate(new Pos3D(entityplayer));

			world.spawnEntityInWorld(new EntityBalloon(world, pos.xCoord-0.5, pos.yCoord-0.25, pos.zCoord-0.5, getColor(itemstack)));
		}

		itemstack.stackSize--;

		return itemstack;
	}

	@Override
	public String getItemStackDisplayName(ItemStack stack)
	{
		EnumColor color = getColor(stack);
        String dyeName = color.getDyedName();

        if(StatCollector.canTranslate(getUnlocalizedName(stack) + "." + color.dyeName))
        {
            return LangUtils.localize(getUnlocalizedName(stack) + "." + color.dyeName);
        }

		if(getColor(stack) == EnumColor.BLACK)
		{
			dyeName = EnumColor.DARK_GREY + color.getDyeName();
		}

		return dyeName + " " + LangUtils.localize("tooltip.balloon");
	}

	@Override
	public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ)
	{
		if(player.isSneaking())
		{
			AxisAlignedBB bound = new AxisAlignedBB(pos, pos.add(1, 3, 1));

			List<EntityBalloon> balloonsNear = player.worldObj.getEntitiesWithinAABB(EntityBalloon.class, bound);

			if(balloonsNear.size() > 0)
			{
				return true;
			}

			if(world.getBlockState(pos).getBlock().isReplaceable(world, pos))
			{
				pos = pos.down();
			}
			
			if(!world.isSideSolid(pos, EnumFacing.UP))
			{
				return true;
			}

			if(canReplace(world, pos.up()) && canReplace(world, pos.up(2)))
			{
				world.setBlockToAir(pos.up());
				world.setBlockToAir(pos.up(2));

				if(!world.isRemote)
				{
					world.spawnEntityInWorld(new EntityBalloon(world, new Coord4D(pos, world), getColor(stack)));
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
				AxisAlignedBB bound = new AxisAlignedBB(entity.posX - 0.2, entity.posY - 0.5, entity.posZ - 0.2, entity.posX + 0.2, entity.posY + entity.height + 4, entity.posZ + 0.2);

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

	private boolean canReplace(World world, BlockPos pos)
	{
		return world.isAirBlock(pos) || world.getBlockState(pos).getBlock().isReplaceable(world, pos);
	}
	
	public class DispenserBehavior extends BehaviorDefaultDispenseItem
	{
		@Override
		public ItemStack dispenseStack(IBlockSource source, ItemStack stack)
		{
			Coord4D coord = new Coord4D(source.getX(), source.getY(), source.getZ(), source.getWorld().provider.getDimensionId());
			EnumFacing side = EnumFacing.getFront(BlockDispenser.getFacing(source.getBlockMetadata()).ordinal());

			List<EntityLivingBase> entities = source.getWorld().getEntitiesWithinAABB(EntityLivingBase.class, coord.offset(side).getBoundingBox());
			boolean latched = false;
			
			for(EntityLivingBase entity : entities)
			{
				AxisAlignedBB bound = new AxisAlignedBB(entity.posX - 0.2, entity.posY - 0.5, entity.posZ - 0.2, entity.posX + 0.2, entity.posY + entity.height + 4, entity.posZ + 0.2);

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
					source.getWorld().spawnEntityInWorld(new EntityBalloon(source.getWorld(), new Coord4D(new BlockPos(pos), source.getWorld()), getColor(stack)));
				}
			}
			
			stack.stackSize--;
			return stack;
		}
	}
}
