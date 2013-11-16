/**
 * 
 */
package mekanism.induction.common.item;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import universalelectricity.core.vector.Vector3;

/**
 * @author Calclavia
 * 
 */
public class ItemLinker extends ItemCoordLink
{
	public ItemLinker(int id)
	{
		super("linker", id);
	}

	@Override
	public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int par7, float par8, float par9, float par10)
	{
		if (!world.isRemote)
		{
			int dimID = world.provider.dimensionId;
			player.addChatMessage("Set link to block [" + x + ", " + y + ", " + z + "], dimension '" + dimID + "'");
			this.setLink(stack, new Vector3(x, y, z), dimID);
		}

		return true;
	}
}
