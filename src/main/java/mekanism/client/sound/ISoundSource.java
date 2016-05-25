package mekanism.client.sound;

import net.minecraft.client.audio.ISound.AttenuationType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public interface ISoundSource
{
	@SideOnly(Side.CLIENT)
	public ResourceLocation getSoundLocation();

	@SideOnly(Side.CLIENT)
	public float getVolume();

	@SideOnly(Side.CLIENT)
	public float getFrequency();

	@SideOnly(Side.CLIENT)
	public Vec3d getSoundPosition();

	@SideOnly(Side.CLIENT)
	public boolean shouldRepeat();

	@SideOnly(Side.CLIENT)
	public int getRepeatDelay();

	@SideOnly(Side.CLIENT)
	public AttenuationType getAttenuation();
}
