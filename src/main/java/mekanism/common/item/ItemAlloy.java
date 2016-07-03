package mekanism.common.item;

import mekanism.api.IAlloyInteraction;
import mekanism.api.MekanismConfig.general;
import mekanism.common.MekanismItems;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class ItemAlloy extends ItemMekanism
{
	public ItemAlloy()
	{
		super();
	}
	
	@Override
	public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ)
    {
		TileEntity tile = world.getTileEntity(x, y, z);
		
		if(general.allowTransmitterAlloyUpgrade && tile instanceof IAlloyInteraction)
		{
			if(!world.isRemote)
			{
				int ordinal = stack.getItem() == MekanismItems.EnrichedAlloy? 1 : (stack.getItem() == MekanismItems.ReinforcedAlloy ? 2 : 3);
				((IAlloyInteraction)tile).onAlloyInteraction(player, ordinal);
			}
			
			return true;
		}
		
        return false;
    }
}
