package mekanism.common.tile;

import mekanism.api.MekanismConfig.client;
import mekanism.api.Pos3D;
import mekanism.client.sound.ISoundSource;
import mekanism.client.sound.SoundHandler;
import mekanism.client.sound.TileSound;
import mekanism.common.base.IActiveState;
import mekanism.common.base.IHasSound;
import mekanism.common.base.SoundWrapper;
import net.minecraft.client.audio.ISound.AttenuationType;
import net.minecraft.util.ResourceLocation;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public abstract class TileEntityNoisyElectricBlock extends TileEntityElectricBlock implements IHasSound, ISoundSource, IActiveState
{
	/** The ResourceLocation of the machine's sound */
	public ResourceLocation soundURL;

	/** The bundled URL of this machine's sound effect */
	@SideOnly(Side.CLIENT)
	public SoundWrapper sound;

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
	@SideOnly(Side.CLIENT)
	public SoundWrapper getSound()
	{
		return sound;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean shouldPlaySound()
	{
		return getActive() && !isInvalid();
	}

	@Override
	@SideOnly(Side.CLIENT)
	public ResourceLocation getSoundLocation()
	{
		return soundURL;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public float getVolume()
	{
		return 1F;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public float getPitch()
	{
		return 1F;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public Pos3D getSoundPosition()
	{
		return new Pos3D(xCoord+0.5, yCoord+0.5, zCoord+0.5);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean shouldRepeat()
	{
		return true;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public int getRepeatDelay()
	{
		return 0;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public AttenuationType getAttenuation()
	{
		return AttenuationType.LINEAR;
	}

	@Override
	public void validate()
	{
		super.validate();

		if(worldObj.isRemote)
		{
			initSounds();
		}
	}

	@SideOnly(Side.CLIENT)
	public void initSounds()
	{
		sound = new SoundWrapper(this, this);
	}

	@Override
	public void onUpdate()
	{
		super.onUpdate();
		
		if(worldObj.isRemote)
		{
			updateSound();
		}
	}

	@SideOnly(Side.CLIENT)
	public void updateSound()
	{
		if(shouldPlaySound() && getSound().canRestart() && client.enableMachineSounds)
		{
			getSound().reset();
			getSound().play();
		}
	}
}
