package mekanism.common.tile.prefab;

import mekanism.common.Upgrade;
import mekanism.common.base.IActiveState;
import mekanism.common.base.IUpgradeTile;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.ISound;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public abstract class TileEntityNoisyBlock extends TileEntityElectricBlock implements IActiveState
{
	private SoundEvent soundEvent;

	@SideOnly(Side.CLIENT)
	private ISound activeSound;

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

		// TODO: Have subclasses pass in a static SoundEvent so we avoid per-instance # of SoundEvents for same sound
		soundEvent = new SoundEvent(new ResourceLocation("mekanism", "tile." + sound));
	}


	protected float getVolume()
	{
		if(this instanceof IUpgradeTile && ((IUpgradeTile)this).getComponent().supports(Upgrade.MUFFLING))
		{
			return Math.max(0.001F, 1F - (float)((IUpgradeTile)this).getComponent().getUpgrades(Upgrade.MUFFLING)/(float)Upgrade.MUFFLING.getMax());
		}
		return 1F;
	}


	// Protected way for subclasses to swap out a sound
	@SideOnly(Side.CLIENT)
	protected void setSoundEvent(SoundEvent event) {
		this.soundEvent = event;

		// Stop the active sound if it's playing, since underlying sound might be changing
		Minecraft mc = Minecraft.getMinecraft();
		if (activeSound != null && mc.getSoundHandler().isSoundPlaying(activeSound)) {
			mc.getSoundHandler().stopSound(activeSound);
		}
	}

	@SideOnly(Side.CLIENT)
	private void updateSound() {
		Minecraft mc = Minecraft.getMinecraft();
		if (getActive() && !isInvalid()) {
			// Machine is active; if we don't have a sound already playing, schedule another
			if (activeSound == null || !mc.getSoundHandler().isSoundPlaying(activeSound)) {
				activeSound = new PositionedSoundRecord(soundEvent, SoundCategory.BLOCKS, getVolume(), 1.0f, getPos());
				mc.getSoundHandler().playSound(activeSound);
			}
		} else {
			// Not active; stop any active playing sounds
			if (activeSound != null && mc.getSoundHandler().isSoundPlaying(activeSound)) {
				mc.getSoundHandler().stopSound(activeSound);
				activeSound = null;
			}
		}
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
}
