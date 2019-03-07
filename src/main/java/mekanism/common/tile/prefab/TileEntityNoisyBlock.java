package mekanism.common.tile.prefab;

import mekanism.client.sound.SoundHandler;
import mekanism.common.Mekanism;
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
	private int playSoundCooldown = 0;

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


	protected float getInitialVolume() {
		return 1.0f;
	}


	// Protected way for subclasses to swap out a sound
	@SideOnly(Side.CLIENT)
	protected void setSoundEvent(SoundEvent event) {
		this.soundEvent = event;

		// Stop the active sound if it's playing, since underlying sound might be changing
		SoundHandler.stopTileSound(getPos());
	}

	@SideOnly(Side.CLIENT)
	private void updateSound() {
		if (getActive() && !isInvalid()) {
			// If sounds are being muted, we can attempt to start them on every tick, only to have them
			// denied by the event bus, so use a cooldown period that ensures we're only trying once every
			// second or so to start a sound.
			if (--playSoundCooldown > 0) {
				return;
			}

			if (activeSound == null || !Minecraft.getMinecraft().getSoundHandler().isSoundPlaying(activeSound)) {
				activeSound = SoundHandler.startTileSound(soundEvent.getSoundName(), getInitialVolume(), getPos());
				playSoundCooldown = 20;
			}
		} else {
			if (activeSound != null) {
				SoundHandler.stopTileSound(getPos());
				activeSound = null;
				playSoundCooldown = 0;
			}
		}
	}

	@Override
	public void invalidate() {
		super.invalidate();

		if (world.isRemote) {
			updateSound();
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
