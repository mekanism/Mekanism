package mekanism.common.tile;

import mekanism.api.Pos3D;
import mekanism.client.sound.IHasSound;
import mekanism.client.sound.IResettableSound;
import mekanism.client.sound.ISoundSource;
import mekanism.client.sound.SoundHandler;
import mekanism.client.sound.TileSound;
import mekanism.common.Upgrade;
import mekanism.common.base.IActiveState;
import mekanism.common.base.IUpgradeTile;

import net.minecraft.client.audio.ISound;
import net.minecraft.client.audio.ISound.AttenuationType;
import net.minecraft.util.ResourceLocation;

public abstract class TileEntityNoisyElectricBlock extends TileEntityElectricBlock implements IHasSound, ISoundSource, IActiveState
{
	/** The ResourceLocation of the machine's sound */
	public ResourceLocation soundURL;

	/** The bundled URL of this machine's sound effect */
	public IResettableSound sound;

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
		if(this instanceof IUpgradeTile)
		{
			float speedUpgrades = ((IUpgradeTile)this).getComponent().getUpgrades(Upgrade.SPEED);
			return 1F + 20 * speedUpgrades / (float)Upgrade.SPEED.getMax();
		}
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
		sound = new TileSound(this, this);
	}

	@Override
	public void onUpdate()
	{
		if(worldObj.isRemote && shouldPlaySound() && SoundHandler.canRestartSound(sound))
		{
			sound.reset();
			SoundHandler.playSound(sound);
		}
	}
}
