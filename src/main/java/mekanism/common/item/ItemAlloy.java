package mekanism.common.item;

import mekanism.api.IAlloyInteraction;
import mekanism.common.MekanismItems;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.util.MekanismUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ItemAlloy extends ItemMekanism
{
	public ItemAlloy()
	{
		super();
	}
	
	@Override
	public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ)
    {
		TileEntity tile = world.getTileEntity(pos);
		
		if(MekanismUtils.hasCapability(tile, Capabilities.ALLOY_INTERACTION_CAPABILITY, side))
		{
			if(!world.isRemote)
			{
				IAlloyInteraction interaction = MekanismUtils.getCapability(tile, Capabilities.ALLOY_INTERACTION_CAPABILITY, side);
				int ordinal = stack.getItem() == MekanismItems.EnrichedAlloy? 1 : (stack.getItem() == MekanismItems.ReinforcedAlloy ? 2 : 3);
				interaction.onAlloyInteraction(player, ordinal);
			}
			
			return true;
		}
		
        return false;
    }
}
