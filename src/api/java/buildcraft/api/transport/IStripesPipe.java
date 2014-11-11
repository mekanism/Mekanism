package buildcraft.api.transport;

import net.minecraft.item.ItemStack;
import net.minecraftforge.common.util.ForgeDirection;

public interface IStripesPipe extends IPipe {
	void sendItem(ItemStack itemStack, ForgeDirection direction);
	void dropItem(ItemStack itemStack, ForgeDirection direction);
}
