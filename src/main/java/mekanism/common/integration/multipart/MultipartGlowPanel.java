package mekanism.common.integration.multipart;

import mcmultipart.api.multipart.IMultipart;
import mcmultipart.api.slot.EnumFaceSlot;
import mcmultipart.api.slot.IPartSlot;
import mekanism.common.MekanismBlocks;
import mekanism.common.block.states.BlockStateFacing;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class MultipartGlowPanel implements IMultipart
{
	@Override
	public IPartSlot getSlotForPlacement(World world, BlockPos pos, IBlockState state, EnumFacing facing, float hitX, float hitY,float hitZ, EntityLivingBase placer) 
	{
		return EnumFaceSlot.values()[facing.ordinal()];
	}

	@Override
	public IPartSlot getSlotFromWorld(IBlockAccess world, BlockPos pos, IBlockState state) 
	{
		return EnumFaceSlot.values()[state.getValue(BlockStateFacing.facingProperty).ordinal()];
	}
	
	@Override
	public Block getBlock() {
		return MekanismBlocks.GlowPanel;
	}
}
