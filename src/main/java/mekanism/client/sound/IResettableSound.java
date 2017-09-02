package mekanism.client.sound;

import net.minecraft.client.audio.ITickableSound;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public interface IResettableSound extends ITickableSound
{
	void reset();
}
