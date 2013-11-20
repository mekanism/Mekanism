package mekanism.common.util;

import java.util.ArrayList;
import java.util.List;

import mekanism.api.Object3D;
import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.event.ForgeEventFactory;

public final class MinerUtils 
{
	public static List<ItemStack> getStacksFromBlock(World world, Object3D obj)
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

        return block.getBlockDropped(world, obj.xCoord, obj.yCoord, obj.zCoord, meta, 0);
	}
}
