package mekanism.client.sound;

import net.minecraft.client.audio.ISound.AttenuationType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public interface ISoundSource {

    @SideOnly(Side.CLIENT)
    ResourceLocation getSoundLocation();

    @SideOnly(Side.CLIENT)
    float getVolume();

    @SideOnly(Side.CLIENT)
    float getFrequency();

    @SideOnly(Side.CLIENT)
    Vec3d getSoundPosition();

    @SideOnly(Side.CLIENT)
    boolean shouldRepeat();

    @SideOnly(Side.CLIENT)
    int getRepeatDelay();

    @SideOnly(Side.CLIENT)
    AttenuationType getAttenuation();
}
