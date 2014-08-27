package mekanism.common.tile;

import mekanism.api.Pos3D;
import mekanism.client.sound.IHasSound;
import mekanism.client.sound.ISoundSource;
import mekanism.client.sound.SoundTile;
import mekanism.common.Mekanism;
import mekanism.common.base.IActiveState;

import net.minecraft.client.audio.ISound;
import net.minecraft.client.audio.ISound.AttenuationType;
import net.minecraft.util.ResourceLocation;
import cpw.mods.fml.client.FMLClientHandler;

public abstract class TileEntityNoisyElectricBlock extends TileEntityElectricBlock implements IHasSound, ISoundSource, IActiveState
{
	/** The ResourceLocation of the machine's sound */
	public ResourceLocation soundURL;

	/** The bundled URL of this machine's sound effect */
	public SoundTile sound;

	/**
	 * The base of all blocks that deal with electricity and make noise.
	 *
	 * @param name      - full name of this block
	 * @param maxEnergy - how much energy this block can store
	 */
	public TileEntityNoisyElectricBlock(String soundPath, String name, double maxEnergy)
	{
		super(name, maxEnergy);

		soundURL = new ResourceLocation("mekanism", "tile." + soundPath);
	}

	@Override
	public ISound getSound()
	{
		return sound;
	}

	@Override
	public boolean shouldPlaySound()
	{
		return getActive() && !isInvalid();
	}

	@Override
	public ResourceLocation getSoundLocation()
	{
		return soundURL;
	}

	@Override
	public float getVolume()
	{
		return 1F;
	}

	@Override
	public float getPitch()
	{
		return 1F;
	}

	@Override
	public Pos3D getSoundPosition()
	{
		return new Pos3D(xCoord+0.5, yCoord+0.5, zCoord+0.5);
	}

	@Override
	public boolean shouldRepeat()
	{
		return true;
	}

	@Override
	public int getRepeatDelay()
	{
		return 0;
	}

	@Override
	public AttenuationType getAttenuation()
	{
		return AttenuationType.LINEAR;
	}

	@Override
	public void validate()
	{
		super.validate();
		sound = new SoundTile(this, this);
	}

	@Override
	public void onUpdate()
	{
		if(worldObj.isRemote && shouldPlaySound() && sound.isDonePlaying())
		{
			Mekanism.logger.info("Playing " + this.fullName + " noise");
			sound.reset();
			FMLClientHandler.instance().getClient().getSoundHandler().playSound(sound);
		}
	}
}
