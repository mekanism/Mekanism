package mekanism.client.sound;

import net.minecraft.client.audio.ITickableSound;
import net.minecraft.client.audio.PositionedSound;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class TestSound extends PositionedSound implements ITickableSound
{
	public boolean finished = true;

	public TestSound(ISoundSource source)
	{
		super(source.getSoundLocation());
		this.volume = getVolume();
		this.field_147663_c = getPitch();
		this.xPosF = (float)source.getSoundPosition().xPos;
		this.yPosF = (float)source.getSoundPosition().yPos;
		this.zPosF = (float)source.getSoundPosition().zPos;
		this.repeat = source.shouldRepeat();
		this.field_147665_h = source.getRepeatDelay();
		this.field_147666_i = source.getAttenuation();
	}

	@Override
	public boolean isDonePlaying()
	{
		return finished;
	}

	@Override
	public void update()
	{
	}
}