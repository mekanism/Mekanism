package mekanism.client.render.ctm;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.IStringSerializable;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class CTMData 
{
	public List<String> acceptableBlockStates = new ArrayList<String>();
	
	public boolean renderConvexConnections = false;
	
	public CTMData(IStringSerializable... states)
	{
		for(IStringSerializable state : states)
		{
			acceptableBlockStates.add(state.getName());
		}
	}
	
	public CTMData setRenderConvexConnections()
	{
		renderConvexConnections = true;
		return this;
	}
	
	@SideOnly(Side.CLIENT)
	public boolean shouldRenderSide(IBlockAccess world, BlockPos pos, EnumFacing side)
	{
		IBlockState state = world.getBlockState(pos);
		
		if(state.getBlock() instanceof ICTMBlock)
		{
			IStringSerializable serializable = (IStringSerializable)state.getValue(((ICTMBlock)state.getBlock()).getTypeProperty());
			return !acceptableBlockStates.contains(serializable.getName());
		}
		
		return true;
	}
}
