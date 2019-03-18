package mekanism.common.tile.prefab;

import mekanism.client.HolidayManager;
import mekanism.client.sound.ISoundSource;
import mekanism.common.Upgrade;
import mekanism.common.base.IActiveState;
import mekanism.common.base.IHasSound;
import mekanism.common.base.IUpgradeTile;
import mekanism.common.base.SoundWrapper;
import mekanism.common.config.MekanismConfig;
import net.minecraft.client.audio.ISound.AttenuationType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public abstract class TileEntityNoisyBlock extends TileEntityElectricBlock implements IHasSound, ISoundSource, IActiveState
{
	/** The ResourceLocation of the machine's sound */
	public ResourceLocation soundURL;

	/** The bundled URL of this machine's sound effect */
	@SideOnly(Side.CLIENT)
	public SoundWrapper sound;
	
	/** The path of this machine's sound */
	public String soundPath;

	/**
	 * The base of all blocks that deal with electricity and make noise.
	 *
	 * @param sound     - the sound path of this block
	 * @param name      - full name of this block
	 * @param maxEnergy - how much energy this block can store
	 */
	public TileEntityNoisyBlock(String sound, String name, double maxEnergy)
	{
		super(name, maxEnergy);
		
		soundPath = sound;
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
		if(this instanceof IUpgradeTile && ((IUpgradeTile)this).getComponent().supports(Upgrade.MUFFLING))
		{
			return Math.max(0.001F, 1F - (float)((IUpgradeTile)this).getComponent().getUpgrades(Upgrade.MUFFLING)/(float)Upgrade.MUFFLING.getMax());
		}
		
		return 1F;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public float getFrequency()
	{
		return 1F;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public Vec3d getSoundPosition()
	{
		return new Vec3d(getPos()).addVector(0.5, 0.5, 0.5);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean shouldRepeat()
	{
		return false;
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

		if(world.isRemote)
		{
			try {
				soundURL = HolidayManager.filterSound(new ResourceLocation("mekanism", "tile." + soundPath));
				initSounds();
			} catch(Throwable t) {}
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
		
		if(world.isRemote)
		{
			updateSound();
		}
	}

	@SideOnly(Side.CLIENT)
	public void updateSound()
	{
		if(shouldPlaySound() && getSound().canRestart() && MekanismConfig.current().client.enableMachineSounds.val())
		{
			getSound().reset();
			getSound().play();
		}
	}
}
