package buildcraft.api.transport;

import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;

public interface IStripesPipe extends IPipe {
	void sendItem(ItemStack itemStack, EnumFacing direction);
	void dropItem(ItemStack itemStack, EnumFacing direction);
}
