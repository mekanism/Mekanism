package mekanism.common.multipart;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import codechicken.lib.vec.BlockCoord;
import codechicken.lib.vec.Rotation;
import codechicken.lib.vec.Vector3;
import codechicken.multipart.TMultiPart;
import codechicken.multipart.TileMultipart;

/**
 * Taken from FMP to prevent compile issues
 * @author aidancbrady
 *
 */
public abstract class MItemMultiPart extends Item
{
	public MItemMultiPart(int id)
	{
		super(id);
	}
	
	private double getHitDepth(Vector3 vhit, int side)
	{
        return vhit.copy().scalarProject(Rotation.axes[side]) + (side%2^1);
	}
        
    @Override
    public boolean onItemUse(ItemStack item, EntityPlayer player, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ)
    {
    	BlockCoord pos = new BlockCoord(x, y, z);
        Vector3 vhit = new Vector3(hitX, hitY, hitZ);
        double d = getHitDepth(vhit, side);
        
        if(d < 1 && place(item, player, world, x, y, z, side, hitX, hitY, hitZ, pos, vhit))
        {
            return true;
        }
        
        pos.offset(side);
        
        return place(item, player, world, x, y, z, side, hitX, hitY, hitZ, pos, vhit);
    }
    
    private boolean place(ItemStack item, EntityPlayer player, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ, BlockCoord pos, Vector3 vhit)
    {
        TMultiPart part = newPart(item, player, world, pos, side, vhit);
        
        if(part == null || !TileMultipart.canPlacePart(world, pos, part))
        {
            return false;
        }
        
        if(!world.isRemote)
        {
            TileMultipart.addPart(world, pos, part);
        }
        
        if(!player.capabilities.isCreativeMode)
        {
            item.stackSize -= 1;
        }
        
        return true;
    }
    
    /**
     * Create a new part based on the placement information parameters.
     */
    public abstract TMultiPart newPart(ItemStack item, EntityPlayer player, World world, BlockCoord pos, int side, Vector3 vhit);
}
