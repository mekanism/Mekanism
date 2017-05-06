package mekanism.common;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;

public final class MekanismSounds
{
	public static SoundEvent BEEP;
	public static SoundEvent CLICK;
	public static SoundEvent DING;
	public static SoundEvent ERROR;
	public static SoundEvent GAS_MASK;
	public static SoundEvent HYDRAULIC;
	public static SoundEvent POP;
	public static SoundEvent SUCCESS;
	public static SoundEvent CJ_EASTER_EGG;

	public static void register()
	{
		BEEP = registerSound("etc.Beep");
		CLICK = registerSound("etc.Click");
		DING = registerSound("etc.Ding");
		ERROR = registerSound("etc.Error");
		GAS_MASK = registerSound("etc.GasMask");
		HYDRAULIC = registerSound("etc.Hydraulic");
		POP = registerSound("etc.Pop");
		SUCCESS = registerSound("etc.Success");
		CJ_EASTER_EGG = registerSound("etc.cj");
	}

	public static SoundEvent registerSound(String soundName)
	{
		ResourceLocation soundID = new ResourceLocation("mekanism", soundName);
		SoundEvent event = new SoundEvent(soundID).setRegistryName(soundID);
		return GameRegistry.register(event);
	}
}
