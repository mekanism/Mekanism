package mekanism.common;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.registries.IForgeRegistry;

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

	public static void register(IForgeRegistry<SoundEvent> registry)
	{
		BEEP = registerSound(registry, "etc.Beep");
		CLICK = registerSound(registry, "etc.Click");
		DING = registerSound(registry, "etc.Ding");
		ERROR = registerSound(registry, "etc.Error");
		GAS_MASK = registerSound(registry, "etc.GasMask");
		HYDRAULIC = registerSound(registry, "etc.Hydraulic");
		POP = registerSound(registry, "etc.Pop");
		SUCCESS = registerSound(registry, "etc.Success");
		CJ_EASTER_EGG = registerSound(registry, "etc.cj");
	}

	public static SoundEvent registerSound(IForgeRegistry<SoundEvent> registry, String soundName)
	{
		ResourceLocation soundID = new ResourceLocation("mekanism", soundName);
		SoundEvent event = new SoundEvent(soundID).setRegistryName(soundID);
		registry.register(event);
		return event;
	}
}
