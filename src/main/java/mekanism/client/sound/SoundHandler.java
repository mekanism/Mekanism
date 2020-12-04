package mekanism.client.sound;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.util.EnumMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import javax.annotation.Nonnull;
import mekanism.api.Upgrade;
import mekanism.client.sound.PlayerSound.SoundType;
import mekanism.common.Mekanism;
import mekanism.common.config.MekanismConfig;
import mekanism.common.lib.radiation.RadiationManager.RadiationScale;
import mekanism.common.registration.impl.SoundEventRegistryObject;
import mekanism.common.tile.interfaces.ITileSound;
import mekanism.common.tile.interfaces.IUpgradeTile;
import mekanism.common.util.WorldUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.ISound;
import net.minecraft.client.audio.SimpleSound;
import net.minecraft.client.audio.Sound;
import net.minecraft.client.audio.SoundEngine;
import net.minecraft.client.audio.TickableSound;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.event.sound.PlaySoundEvent;
import net.minecraftforge.client.event.sound.SoundSetupEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/**
 * SoundHandler is the central point for sounds on Mek client side. There are roughly three classes of sounds to deal with:
 *
 * <ol>
 *  <li>One-shot sounds like GUI interactions; play the sound and done</li>
 *  <li>Long-lived player item sounds (such as jetpacks): long-running sounds that may flip on/off quickly based on user action. We follow the minecart model for
 *      these sounds; starting a sound and then muting when not in use.</li>
 *  <li>Tile entity sounds: long-running, repeating sounds that run while a fixed tile is active. These are sounds that users want to be able to mute effectively.</li>
 * </ol>
 *
 * All sounds, when initially started can be intercepted on the Forge event bus and wrapped by various muting/manipulation mods. For item sounds, we don't want to them to
 * be manipulated, since the flipping on/off is too prone to weird timing issues. For long-running sounds, we need a way to honor these attempted manipulations, without
 * allowing them to become the permanent state of the sound (which is what happens by default). To accomplish this, we have our own wrapper that intercepts new repeating
 * sounds from Mek and ensures that they periodically poll for any muting/manipulation so that it the object can dynamically adjust to conditions.
 *
 * @apiNote Only used by client
 */
public class SoundHandler {

    private SoundHandler() {
    }

    private static final Set<UUID> jetpackSounds = new ObjectOpenHashSet<>();
    private static final Set<UUID> scubaMaskSounds = new ObjectOpenHashSet<>();
    private static final Set<UUID> flamethrowerSounds = new ObjectOpenHashSet<>();
    private static final Set<UUID> gravitationalModulationSounds = new ObjectOpenHashSet<>();
    public static final Map<RadiationScale, GeigerSound> radiationSoundMap = new EnumMap<>(RadiationScale.class);

    private static final Long2ObjectMap<ISound> soundMap = new Long2ObjectOpenHashMap<>();
    private static boolean IN_MUFFLED_CHECK = false;
    private static SoundEngine soundEngine;

    public static void clearPlayerSounds() {
        jetpackSounds.clear();
        scubaMaskSounds.clear();
        flamethrowerSounds.clear();
        gravitationalModulationSounds.clear();
    }

    public static void clearPlayerSounds(UUID uuid) {
        jetpackSounds.remove(uuid);
        scubaMaskSounds.remove(uuid);
        flamethrowerSounds.remove(uuid);
        gravitationalModulationSounds.remove(uuid);
    }

    public static void startSound(@Nonnull IWorld world, @Nonnull UUID uuid, @Nonnull SoundType soundType) {
        switch (soundType) {
            case JETPACK:
                if (!jetpackSounds.contains(uuid)) {
                    PlayerEntity player = world.getPlayerByUuid(uuid);
                    if (player != null) {
                        jetpackSounds.add(uuid);
                        playSound(new JetpackSound(player));
                    }
                }
                break;
            case SCUBA_MASK:
                if (!scubaMaskSounds.contains(uuid)) {
                    PlayerEntity player = world.getPlayerByUuid(uuid);
                    if (player != null) {
                        scubaMaskSounds.add(uuid);
                        playSound(new ScubaMaskSound(player));
                    }
                }
                break;
            case FLAMETHROWER:
                if (!flamethrowerSounds.contains(uuid)) {
                    PlayerEntity player = world.getPlayerByUuid(uuid);
                    if (player != null) {
                        flamethrowerSounds.add(uuid);
                        //TODO: Evaluate at some point if there is a better way to do this
                        // Currently it requests both play, except only one can ever play at once due to the shouldPlaySound method
                        playSound(new FlamethrowerSound.Active(player));
                        playSound(new FlamethrowerSound.Idle(player));
                    }
                }
                break;
            case GRAVITATIONAL_MODULATOR:
                if (!gravitationalModulationSounds.contains(uuid)) {
                    PlayerEntity player = world.getPlayerByUuid(uuid);
                    if (player != null) {
                        gravitationalModulationSounds.add(uuid);
                        playSound(new GravitationalModulationSound(player));
                    }
                }
                break;
        }
    }

    public static void playSound(SoundEventRegistryObject<?> soundEventRO) {
        playSound(soundEventRO.get());
    }

    public static void playSound(SoundEvent sound) {
        playSound(SimpleSound.master(sound, 1, MekanismConfig.client.baseSoundVolume.get()));
    }

    public static void playSound(ISound sound) {
        Minecraft.getInstance().getSoundHandler().play(sound);
    }

    public static ISound startTileSound(SoundEvent soundEvent, SoundCategory category, float volume, BlockPos pos) {
        // First, check to see if there's already a sound playing at the desired location
        ISound s = soundMap.get(pos.toLong());
        if (s == null || !Minecraft.getInstance().getSoundHandler().isPlaying(s)) {
            // No sound playing, start one up - we assume that tile sounds will play until explicitly stopped
            // The TileTickableSound will then periodically poll to see if the volume should be adjusted
            s = new TileTickableSound(soundEvent, category, pos, volume);

            if (!isClientPlayerInRange(s)) {
                //If the player is not in range of the sound the tile would play,
                // instead of starting it, just don't
                return null;
            }

            // Start the sound
            playSound(s);

            // N.B. By the time playSound returns, our expectation is that our wrapping-detector handler has fired
            // and dealt with any muting interceptions and, CRITICALLY, updated the soundMap with the final ISound.
            s = soundMap.get(pos.toLong());
        }
        return s;
    }

    public static void stopTileSound(BlockPos pos) {
        long posKey = pos.toLong();
        ISound s = soundMap.get(posKey);
        if (s != null) {
            // and maybe we can avoid this dedicated soundMap
            Minecraft.getInstance().getSoundHandler().stop(s);
            soundMap.remove(posKey);
        }
    }

    private static boolean isClientPlayerInRange(ISound sound) {
        if (sound.isGlobal() || sound.getAttenuationType() == ISound.AttenuationType.NONE) {
            //If the sound is global or has no attenuation, then return that the player is in range
            return true;
        }
        PlayerEntity player = Minecraft.getInstance().player;
        if (player == null) {
            //Shouldn't happen but just in case
            return false;
        }
        Sound s = sound.getSound();
        if (s == null) {
            //If the sound hasn't been initialized yet for some reason try initializing it
            sound.createAccessor(Minecraft.getInstance().getSoundHandler());
            s = sound.getSound();
        }
        //Attenuation distance, defaults to 16 blocks
        int attenuationDistance = s.getAttenuationDistance();
        //Scale the distance based on the sound's volume
        float scaledDistance = Math.max(sound.getVolume(), 1) * attenuationDistance;
        //Check if the player is within range of hearing the sound
        return player.getPositionVec().squareDistanceTo(sound.getX(), sound.getY(), sound.getZ()) < scaledDistance * scaledDistance;
    }

    @SubscribeEvent
    public static void onSoundEngineSetup(SoundSetupEvent event) {
        //Grab the sound engine, so that we are able to play sounds. We use this event rather than requiring the use of an AT
        if (soundEngine == null) {
            //Note: We include a null check as the constructor for SoundEngine is public and calls this event
            // And we do not want to end up grabbing a modders variant of this
            soundEngine = event.getManager();
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onTilePlaySound(PlaySoundEvent event) {
        // Ignore any sound event which is null or is happening in a muffled check
        ISound resultSound = event.getResultSound();
        if (resultSound == null || IN_MUFFLED_CHECK) {
            return;
        }

        // Ignore any sound event outside this mod namespace
        ResourceLocation soundLoc = event.getSound().getSoundLocation();
        //If it is mekanism or one of the sub modules let continue
        if (!soundLoc.getNamespace().startsWith(Mekanism.MODID)) {
            return;
        }

        // If this is a Mek player sound, unwrap any muffling that other mods may have attempted. I haven't
        // sorted out a good way to deal with long-lived, non-repeating, dynamic volume sounds -- something
        // to investigate in the future.
        if (event.getSound() instanceof PlayerSound) {
            event.setResultSound(event.getSound());
            return;
        }

        //Ignore any non-tile Mek sounds
        if (event.getName().startsWith("tile.")) {
            //At this point, we've got a known block Mekanism sound.
            // Update our soundMap so that we can actually have a shot at stopping this sound; note that we also
            // need to "unoffset" the sound position so that we build the correct key for the sound map
            // Aside: I really, really, wish Forge returned the final result sound as part of playSound :/
            BlockPos pos = new BlockPos(resultSound.getX() - 0.5, resultSound.getY() - 0.5, resultSound.getZ() - 0.5);
            soundMap.put(pos.toLong(), resultSound);
        }
    }

    private static class TileTickableSound extends TickableSound {

        private final float originalVolume;

        // Choose an interval between 60-80 ticks (3-4 seconds) to check for muffling changes. We do this
        // to ensure that not every tile sound tries to run on the same tick and thus create
        // uneven spikes of CPU usage
        private final int checkInterval = 20 + ThreadLocalRandom.current().nextInt(20);

        TileTickableSound(SoundEvent soundEvent, SoundCategory category, BlockPos pos, float volume) {
            super(soundEvent, category);
            //Keep track of our original volume
            this.originalVolume = volume * MekanismConfig.client.baseSoundVolume.get();
            this.x = pos.getX() + 0.5F;
            this.y = pos.getY() + 0.5F;
            this.z = pos.getZ() + 0.5F;
            //Hold off on setting volume until after we set the position
            this.volume = this.originalVolume * getTileVolumeFactor();
            this.repeat = true;
            this.repeatDelay = 0;
        }

        @Override
        public void tick() {
            // Every configured interval, see if we need to adjust muffling
            if (Minecraft.getInstance().world.getGameTime() % checkInterval == 0) {
                if (!isClientPlayerInRange(this)) {
                    //If the player is not in range of hearing this sound any more; go ahead and shutdown
                    finishPlaying();
                    return;
                }
                // Run the event bus with the original sound. Note that we must making sure to set the GLOBAL/STATIC
                // flag that ensures we don't wrap already muffled sounds. This is...NOT ideal and makes some
                // significant (hopefully well-informed) assumptions about locking/ordering of all these calls.
                IN_MUFFLED_CHECK = true;
                //Make sure we set our volume back to what it actually would be for purposes of letting other mods know
                // what volume to use
                volume = originalVolume;
                ISound s = ForgeHooksClient.playSound(soundEngine, this);
                IN_MUFFLED_CHECK = false;

                if (s == this) {
                    // No filtering done, use the original sound's volume
                    volume = originalVolume * getTileVolumeFactor();
                } else if (s == null) {
                    // Full on mute; go ahead and shutdown
                    finishPlaying();
                } else {
                    // Altered sound returned; adjust volume
                    volume = s.getVolume() * getTileVolumeFactor();
                }
            }
        }

        private float getTileVolumeFactor() {
            // Pull the TE from the sound position and see if supports muffling upgrades. If it does, calculate what
            // percentage of the original volume should be muted
            TileEntity tile = WorldUtils.getTileEntity(Minecraft.getInstance().world, new BlockPos(getX(), getY(), getZ()));
            float retVolume = 1.0F;

            if (tile instanceof IUpgradeTile) {
                IUpgradeTile upgradeTile = (IUpgradeTile) tile;
                if (upgradeTile.supportsUpgrades() && upgradeTile.getComponent().supports(Upgrade.MUFFLING)) {
                    int mufflerCount = upgradeTile.getComponent().getUpgrades(Upgrade.MUFFLING);
                    retVolume = 1.0F - (mufflerCount / (float) Upgrade.MUFFLING.getMax());
                }
            }

            if (tile instanceof ITileSound) {
                retVolume *= ((ITileSound) tile).getVolume();
            }

            return retVolume;
        }

        @Override
        public float getVolume() {
            if (this.sound == null) {
                this.createAccessor(Minecraft.getInstance().getSoundHandler());
            }
            return super.getVolume();
        }

        @Override
        public boolean canBeSilent() {
            return true;
        }
    }
}