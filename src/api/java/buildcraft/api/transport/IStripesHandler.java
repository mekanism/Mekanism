package buildcraft.api.transport;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraft.util.EnumFacing;

public interface IStripesHandler {
	public static enum StripesHandlerType {
		ITEM_USE,
		BLOCK_BREAK
	}
	
	StripesHandlerType getType();
	
	boolean shouldHandle(ItemStack stack);
	
	boolean handle(World world, int x, int y, int z, EnumFacing direction,
			ItemStack stack, EntityPlayer player, IStripesPipe pipe);
}
