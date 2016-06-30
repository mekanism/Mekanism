package ic2.api.item;

import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

public interface IKineticRotor {
	int getDiameter(ItemStack stack);

	ResourceLocation getRotorRenderTexture(ItemStack stack);

	float getEfficiency(ItemStack stack);

	int getMinWindStrength(ItemStack stack);

	int getMaxWindStrength(ItemStack stack);

	boolean isAcceptedType(ItemStack stack, GearboxType type);

	public static enum GearboxType  {
		WATER,
		WIND,
	}
}
