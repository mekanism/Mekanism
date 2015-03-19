package ic2.api.item;

import net.minecraft.util.ResourceLocation;

public interface IKineticWindRotor {
	public int getDiameter();

	public ResourceLocation getRotorRenderTexture();

	public float getefficiency();

	int getminWindStrength();

	int getmaxWindStrength();
}
