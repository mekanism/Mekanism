package mekanism.common.item;

import java.util.List;

import mekanism.common.Tier.BaseTier;
import mekanism.common.Tier.FactoryTier;
import mekanism.common.base.IFactory.RecipeType;
import mekanism.common.base.IMetaItem;
import mekanism.common.tile.TileEntityAdvancedElectricMachine;
import mekanism.common.tile.TileEntityBasicBlock;
import mekanism.common.tile.TileEntityElectricMachine;
import mekanism.common.tile.TileEntityFactory;
import mekanism.common.tile.TileEntityMetallurgicInfuser;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ItemFactoryInstaller extends ItemMekanism implements IMetaItem
{
	public ItemFactoryInstaller()
	{
		super();
		setMaxStackSize(1);
		setHasSubtypes(true);
	}
	
	@Override
	public EnumActionResult onItemUseFirst(ItemStack stack, EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ, EnumHand hand)
	{
		if(world.isRemote) 
		{
			return EnumActionResult.PASS;
		}
		
		TileEntity tile = world.getTileEntity(pos);
		FactoryTier tier = FactoryTier.values()[stack.getItemDamage()];
		
		if(tile instanceof TileEntityBasicBlock && ((TileEntityBasicBlock)tile).playersUsing.size() > 0)
		{
			return EnumActionResult.FAIL;
		}
		
		if(tile instanceof TileEntityFactory && tier != FactoryTier.BASIC)
		{
			TileEntityFactory factory = (TileEntityFactory)tile;
			
			if(factory.tier.ordinal()+1 == tier.ordinal())
			{
				if(!world.isRemote)
				{
					factory.upgrade();
				}
				
				if(!player.capabilities.isCreativeMode)
				{
					stack.stackSize = 0;
				}
				
				return EnumActionResult.SUCCESS;
			}
		}
		else if(tile != null && tier == FactoryTier.BASIC)
		{
			RecipeType type = null;
			
			for(RecipeType iterType : RecipeType.values())
			{
				ItemStack machineStack = iterType.getStack();
				IBlockState state = world.getBlockState(pos);
				
				if(Block.getBlockFromItem(machineStack.getItem()) == state.getBlock() && machineStack.getItemDamage() == state.getBlock().getMetaFromState(state))
				{
					type = iterType;
					break;
				}
			}
			
			if(type != null)
			{
				if(tile instanceof TileEntityElectricMachine)
				{
					((TileEntityElectricMachine)tile).upgrade(type);
					
					if(!player.capabilities.isCreativeMode)
					{
						stack.stackSize = 0;
					}
					
					return EnumActionResult.SUCCESS;
				}
				else if(tile instanceof TileEntityAdvancedElectricMachine)
				{
					((TileEntityAdvancedElectricMachine)tile).upgrade(type);
					
					if(!player.capabilities.isCreativeMode)
					{
						stack.stackSize = 0;
					}
					
					return EnumActionResult.SUCCESS;
				}
				else if(tile instanceof TileEntityMetallurgicInfuser)
				{
					((TileEntityMetallurgicInfuser)tile).upgrade(type);
					
					if(!player.capabilities.isCreativeMode)
					{
						stack.stackSize = 0;
					}
					
					return EnumActionResult.SUCCESS;
				}
			}
		}
		
		return EnumActionResult.PASS;
	}
	
	private int getOutputSlot(FactoryTier tier, int operation)
	{
		return 5+tier.processes+operation;
	}
	
	@Override
	public String getTexture(int meta)
	{
		return FactoryTier.values()[meta].getBaseTier().getSimpleName() + "FactoryInstaller";
	}
	
	@Override
	public int getVariants()
	{
		return FactoryTier.values().length;
	}

	@Override
	public void getSubItems(Item item, CreativeTabs tabs, List<ItemStack> itemList)
	{
		for(FactoryTier tier : FactoryTier.values())
		{
			itemList.add(new ItemStack(item, 1, tier.ordinal()));
		}
	}

	@Override
	public String getUnlocalizedName(ItemStack item)
	{
		return "item." + BaseTier.values()[item.getItemDamage()].getSimpleName().toLowerCase() + "FactoryInstaller";
	}
}
