package mekanism.common;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;

public final class MekanismSounds 
{
	public static final SoundEvent CJ_EASTER_EGG = getRegisteredSoundEvent("mekanism:etc.cj");
	public static final SoundEvent POP = getRegisteredSoundEvent("mekanism:etc.Pop");
	public static final SoundEvent DING = getRegisteredSoundEvent("mekanism:etc.Ding");
	public static final SoundEvent HYDRAULIC = getRegisteredSoundEvent("mekanism:etc.Hydraulic");
	public static final SoundEvent CLICK = getRegisteredSoundEvent("mekanism:etc.Click");
	
	private static SoundEvent getRegisteredSoundEvent(String id) 
	{
		SoundEvent soundevent = (SoundEvent)SoundEvent.REGISTRY.getObject(new ResourceLocation(id));

		if(soundevent == null)
		{
			throw new IllegalStateException("Invalid Sound requested: " + id);
		} 
		else {
			return soundevent;
		}
	}
}
