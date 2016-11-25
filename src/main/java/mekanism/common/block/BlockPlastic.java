package mekanism.common.block;

import java.util.List;

import mekanism.api.EnumColor;
import mekanism.common.Mekanism;
import mekanism.common.block.states.BlockStatePlastic;
import mekanism.common.block.states.BlockStatePlastic.PlasticBlockType;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BlockPlastic extends Block
{
	public PlasticBlockType type;
	
	public BlockPlastic(PlasticBlockType blockType)
	{
		super(Material.WOOD);
		type = blockType;
		setHardness(type == PlasticBlockType.REINFORCED ? 50F : 5F);
		setResistance(type == PlasticBlockType.REINFORCED ? 2000F : 10F);
		setCreativeTab(Mekanism.tabMekanism);
		
		if(type == PlasticBlockType.SLICK)
		{
			slipperiness = 0.98F;
		}
	}

	@Override
	protected BlockStateContainer createBlockState()
	{
		return new BlockStatePlastic(this);
	}

	@Override
	public IBlockState getStateFromMeta(int meta)
	{
		return getDefaultState().withProperty(BlockStatePlastic.colorProperty, EnumDyeColor.byDyeDamage(meta));
	}

	@Override
	public int getMetaFromState(IBlockState state)
	{
		return state.getValue(BlockStatePlastic.colorProperty).getDyeDamage();
	}

	@Override
	public Vec3d modifyAcceleration(World world, BlockPos pos, Entity e, Vec3d motion)
	{
		if(type == PlasticBlockType.ROAD)
		{
			double boost = 1.6;

			double a = Math.atan2(motion.xCoord, motion.zCoord);
			return new Vec3d(motion.xCoord + Math.sin(a) * boost * slipperiness, motion.yCoord, motion.zCoord + Math.cos(a) * boost * slipperiness);
		}
		
		return motion;
	}

	@Override
	public int damageDropped(IBlockState state)
	{
		return getMetaFromState(state);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void getSubBlocks(Item item, CreativeTabs creativetabs, List<ItemStack> list)
	{
		for(int i = 0; i < EnumColor.DYES.length; i++)
		{
			list.add(new ItemStack(item, 1, i));
		}
	}

	@Override
	public int getLightValue(IBlockState state, IBlockAccess world, BlockPos pos)
	{
		if(type == PlasticBlockType.GLOW)
		{
			return 10;
		}
		
		return 0;
	}
}
