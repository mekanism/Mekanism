package mekanism.common.util;

import java.util.ArrayList;
import java.util.List;

import mekanism.api.Object3D;
import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public final class MinerUtils 
{
	public static List<ItemStack> getDrops(World world, Object3D obj, boolean silk)
	{
		Block block = Block.blocksList[obj.getBlockId(world)];

        if(block == null)
        {
        	return new ArrayList<ItemStack>();
        }
        
        if(block.isAirBlock(world, obj.xCoord, obj.yCoord, obj.zCoord))
        {
        	return new ArrayList<ItemStack>();
        }

        int meta = obj.getMetadata(world);

        if(!silk)
        {
        	return block.getBlockDropped(world, obj.xCoord, obj.yCoord, obj.zCoord, meta, 0);
        }
        else {
        	List<ItemStack> ret = new ArrayList<ItemStack>();
        	ret.add(new ItemStack(block.blockID, 1, meta));
        	
        	if(block.getBlockDropped(world, obj.xCoord, obj.yCoord, obj.zCoord, meta, 0) != null && block.getBlockDropped(world, obj.xCoord, obj.yCoord, obj.zCoord, meta, 0).size() > 0)
        	{
        		return ret;
        	}
        }
        
        return new ArrayList<ItemStack>();
	}
}
