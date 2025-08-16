package mekanism.client.sound;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.EnumMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;
import mekanism.api.Upgrade;
import mekanism.client.sound.PlayerSound.SoundType;
import mekanism.common.Mekanism;
import mekanism.common.config.MekanismConfig;
import mekanism.common.lib.radiation.RadiationScale;
import mekanism.common.registration.impl.SoundEventRegistryObject;
import mekanism.common.tile.interfaces.ITileSound;
import mekanism.common.tile.interfaces.IUpgradeTile;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.WorldUtils;
import net.minecraft.SharedConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.Sound;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.SoundEngine;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.ClientHooks;
import net.neoforged.neoforge.client.event.sound.PlaySoundEvent;
import net.neoforged.neoforge.client.event.sound.SoundEngineLoadEvent;
import org.jetbrains.annotations.NotNull;

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
 * sounds from Mek and ensures that they periodically poll for any muting/manipulation so that the object can dynamically adjust to any conditions.
 *
 * @apiNote Only used by client
 */
@EventBusSubscriber(modid = Mekanism.MODID, value = Dist.CLIENT)
public class SoundHandler {

    private SoundHandler() {
    }

    private static final Map<UUID, PlayerSound> jetpackSounds = new Object2ObjectOpenHashMap<>();
    private static final Map<UUID, PlayerSound> scubaMaskSounds = new Object2ObjectOpenHashMap<>();
    private static final Map<UUID, PlayerSound[]> flamethrowerSounds = new Object2ObjectOpenHashMap<>();
    private static final Map<UUID, PlayerSound> gravitationalModulationSounds = new Object2ObjectOpenHashMap<>();
    public static final Map<RadiationScale, GeigerSound> radiationSoundMap = new EnumMap<>(RadiationScale.class);

    private static final Long2ObjectMap<SoundInstance> soundMap = new Long2ObjectOpenHashMap<>();
    private static boolean IN_MUFFLED_CHECK = false;
    private static SoundEngine soundEngine;
    private static boolean hadPlayerSounds;

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

    public static void startSound(@NotNull LevelAccessor world, @NotNull UUID uuid, @NotNull SoundType soundType) {
        switch (soundType) {
            case JETPACK -> startSound(world, uuid, jetpackSounds, JetpackSound::new);
            case SCUBA_MASK -> startSound(world, uuid, scubaMaskSounds, ScubaMaskSound::new);
            case GRAVITATIONAL_MODULATOR -> startSound(world, uuid, gravitationalModulationSounds, GravitationalModulationSound::new);
        }
    }

    public static void startFlamethrowerSound(@NotNull Player player) {
        //TODO: Evaluate at some point if there is a better way to do this
        // Currently it requests both play, except only one can ever play at once due to the shouldPlaySound method
        startSounds(player, flamethrowerSounds, FlamethrowerSoundActive::new, FlamethrowerSoundIdle::new);
    }

    private static void startSound(LevelAccessor world, UUID uuid, Map<UUID, PlayerSound> knownSounds, Function<Player, PlayerSound> soundCreator) {
        if (knownSounds.containsKey(uuid)) {
            if (playerSoundsEnabled()) {
                //Check if it needs to be restarted
                restartSounds(knownSounds.get(uuid));
            }
        } else {
            Player player = world.getPlayerByUUID(uuid);
            if (player != null) {
                PlayerSound sound = soundCreator.apply(player);
                playSound(sound);
                knownSounds.put(uuid, sound);
            }
        }
    }

    @SafeVarargs
    private static void startSounds(Player player, Map<UUID, PlayerSound[]> knownSounds, Function<Player, PlayerSound>... soundCreators) {
        UUID uuid = player.getUUID();
        if (knownSounds.containsKey(uuid)) {
            if (playerSoundsEnabled()) {
                //Check if it needs to be restarted
                restartSounds(knownSounds.get(uuid));
            }
        } else {
            PlayerSound[] sounds = new PlayerSound[soundCreators.length];
            for (int i = 0; i < soundCreators.length; i++) {
                playSound(sounds[i] = soundCreators[i].apply(player));
            }
            knownSounds.put(uuid, sounds);
        }
    }

    public static void restartSounds() {
        boolean hasPlayerSounds = playerSoundsEnabled();
        if (hasPlayerSounds != hadPlayerSounds) {
            hadPlayerSounds = hasPlayerSounds;
            if (hasPlayerSounds) {
                //If player sounds were muted and are no longer muted, then we want to try and restart all our sounds
                jetpackSounds.values().forEach(SoundHandler::restartSounds);
                scubaMaskSounds.values().forEach(SoundHandler::restartSounds);
                flamethrowerSounds.values().forEach(SoundHandler::restartSounds);
                gravitationalModulationSounds.values().forEach(SoundHandler::restartSounds);
                radiationSoundMap.values().forEach(SoundHandler::restartSounds);
            }
        }
    }

    private static void restartSounds(PlayerSound... sounds) {
        for (PlayerSound sound : sounds) {
            if (!sound.isStopped() && soundEngine != null && !soundEngine.instanceToChannel.containsKey(sound)) {
                //Note: We need to directly check the instanceToChannel, because isActive will give wrong results as it doesn't
                // get cleared out of the soundDeleteTime map. We also don't restart sounds if they marked themselves as stopped
                // as the cases we have that is if the player is no longer present or the player died, in which case the sound will
                // be removed and restarted as needed
                playSound(sound);
            }
        }
    }

    private static boolean playerSoundsEnabled() {
        return getVolume(SoundSource.MASTER) > 0 && getVolume(SoundSource.PLAYERS) > 0;
    }

    private static float getVolume(SoundSource category) {
        return Minecraft.getInstance().options.getSoundSourceVolume(category);
    }

    public static void playSound(SoundEventRegistryObject<?> soundEventRO) {
        playSound(soundEventRO.get());
    }

    public static void playSound(SoundEvent sound) {
        playSound(SimpleSoundInstance.forUI(sound, 1, MekanismConfig.client.baseSoundVolume.get()));
    }

    public static void playSound(SoundInstance sound) {
        Minecraft.getInstance().getSoundManager().play(sound);
    }

    public static SoundInstance startTileSound(SoundEvent soundEvent, SoundSource category, float volume, RandomSource random, BlockPos pos) {
        return startTileSound(soundEvent, category, volume, random, pos, true);
    }

    public static SoundInstance startTileSound(SoundEvent soundEvent, SoundSource category, float volume, RandomSource random, BlockPos pos, boolean looping) {
        // First, check to see if there's already a sound playing at the desired location
        SoundInstance s = soundMap.get(pos.asLong());
        if (s == null || !Minecraft.getInstance().getSoundManager().isActive(s)) {
            // No sound playing, start one up - we assume that tile sounds will play until explicitly stopped
            // The TileTickableSound will then periodically poll to see if the volume should be adjusted
            s = new TileTickableSound(soundEvent, category, random, pos, volume, looping);

            if (!isClientPlayerInRange(s)) {
                //If the player is not in range of the sound the tile would play,
                // instead of starting it, just don't
                return null;
            }

            // Start the sound
            playSound(s);

            // N.B. By the time playSound returns, our expectation is that our wrapping-detector handler has fired
            // and dealt with any muting interceptions and, CRITICALLY, updated the soundMap with the final ISound.
            s = soundMap.get(pos.asLong());
        }
        return s;
    }

    public static void stopTileSound(BlockPos pos) {
        long posKey = pos.asLong();
        SoundInstance s = soundMap.get(posKey);
        if (s != null) {
            // and maybe we can avoid this dedicated soundMap
            Minecraft.getInstance().getSoundManager().stop(s);
            soundMap.remove(posKey);
        }
    }

    private static boolean isClientPlayerInRange(SoundInstance sound) {
        if (sound.isRelative() || sound.getAttenuation() == SoundInstance.Attenuation.NONE) {
            //If the sound is global or has no attenuation, then return that the player is in range
            return true;
        }
        Player player = Minecraft.getInstance().player;
        if (player == null) {
            //Shouldn't happen but just in case
            return false;
        }
        Sound s = sound.getSound();
        if (s == null) {
            //If the sound hasn't been initialized yet for some reason try initializing it
            sound.resolve(Minecraft.getInstance().getSoundManager());
            s = sound.getSound();
        }
        //Attenuation distance, defaults to 16 blocks
        int attenuationDistance = s.getAttenuationDistance();
        //Scale the distance based on the sound's volume
        float scaledDistance = Math.max(sound.getVolume(), 1) * attenuationDistance;
        //Check if the player is within range of hearing the sound
        return player.position().distanceToSqr(sound.getX(), sound.getY(), sound.getZ()) < scaledDistance * scaledDistance;
    }

    @SubscribeEvent
    public static void onSoundEngineSetup(SoundEngineLoadEvent event) {
        //Grab the sound engine, so that we are able to play sounds. We use this event rather than requiring the use of an AT
        if (soundEngine == null) {
            //Note: We include a null check as the constructor for SoundEngine is public and calls this event
            // And we do not want to end up grabbing a modders variant of this
            soundEngine = event.getEngine();
        }
    }

    public static void onTilePlaySound(PlaySoundEvent event) {
        // Ignore any sound event which is null or is happening in a muffled check
        SoundInstance resultSound = event.getSound();
        if (resultSound == null || IN_MUFFLED_CHECK) {
            return;
        }

        // Ignore any sound event outside this mod namespace
        ResourceLocation soundLoc = event.getOriginalSound().getLocation();
        //If it is mekanism or one of the submodules let continue
        if (!soundLoc.getNamespace().startsWith(Mekanism.MODID)) {
            return;
        }

        // If this is a Mek player sound, unwrap any muffling that other mods may have attempted. I haven't
        // sorted out a good way to deal with long-lived, non-repeating, dynamic volume sounds -- something
        // to investigate in the future.
        if (event.getOriginalSound() instanceof PlayerSound sound) {
            event.setSound(sound);
            return;
        }

        //Ignore any non-tile Mek sounds
        if (event.getName().startsWith("tile.")) {
            //At this point, we've got a known block Mekanism sound.
            // Update our soundMap so that we can actually have a shot at stopping this sound; note that we also
            // need to "unoffset" the sound position so that we build the correct key for the sound map
            // Aside: I really, really, wish Forge returned the final result sound as part of playSound :/
            BlockPos pos = BlockPos.containing(resultSound.getX() - 0.5, resultSound.getY() - 0.5, resultSound.getZ() - 0.5);
            soundMap.put(pos.asLong(), resultSound);
        }
    }

    private static class TileTickableSound extends AbstractTickableSoundInstance {

        private final float originalVolume;

        // Choose an interval between 20-40 ticks (1-2 seconds) to check for muffling changes. We do this
        // to ensure that not every tile sound tries to run on the same tick and thus create
        // uneven spikes of CPU usage
        private final int checkInterval = SharedConstants.TICKS_PER_SECOND + ThreadLocalRandom.current().nextInt(SharedConstants.TICKS_PER_SECOND);

        TileTickableSound(SoundEvent soundEvent, SoundSource category, RandomSource random, BlockPos pos, float volume, boolean looping) {
            super(soundEvent, category, random);
            //Keep track of our original volume
            this.originalVolume = volume * MekanismConfig.client.baseSoundVolume.get();
            this.x = pos.getX() + 0.5F;
            this.y = pos.getY() + 0.5F;
            this.z = pos.getZ() + 0.5F;
            //Hold off on setting volume until after we set the position
            this.volume = this.originalVolume * getTileVolumeFactor();
            this.looping = looping;
            this.delay = 0;
        }

        @Override
        public void tick() {
            // Every configured interval, see if we need to adjust muffling
            Level level = Minecraft.getInstance().level;
            if (level == null) {
                this.stop();
                return;
            }
            if (!MekanismUtils.isTickingNormally(level)) {
                //Mute it similar to how the minecart sound handling is
                volume = 0;
            } else if (level.getGameTime() % checkInterval == 0) {
                if (!isClientPlayerInRange(this)) {
                    //If the player is not in range of hearing this sound anymore; go ahead and shutdown
                    stop();
                    return;
                }
                // Run the event bus with the original sound. Note that we must make sure to set the GLOBAL/STATIC
                // flag that ensures we don't wrap already muffled sounds. This is...NOT ideal and makes some
                // significant (hopefully well-informed) assumptions about locking/ordering of all these calls.
                IN_MUFFLED_CHECK = true;
                //Make sure we set our volume back to what it actually would be for purposes of letting other mods know
                // what volume to use
                volume = originalVolume;
                SoundInstance s = ClientHooks.playSound(soundEngine, this);
                IN_MUFFLED_CHECK = false;

                if (s == this) {
                    // No filtering done, use the original sound's volume
                    volume = originalVolume * getTileVolumeFactor();
                } else if (s == null) {
                    // Full on mute; go ahead and shutdown
                    stop();
                } else {
                    // Altered sound returned; adjust volume
                    volume = s.getVolume() * getTileVolumeFactor();
                }
            }
        }

        private float getTileVolumeFactor() {
            // Pull the TE from the sound position and see if supports muffling upgrades. If it does, calculate what
            // percentage of the original volume should be muted
            BlockEntity tile = WorldUtils.getTileEntity(Minecraft.getInstance().level, BlockPos.containing(getX(), getY(), getZ()));
            float retVolume = 1.0F;

            if (tile instanceof IUpgradeTile upgradeTile && upgradeTile.supportsUpgrade(Upgrade.MUFFLING)) {
                int mufflerCount = Math.min(upgradeTile.getComponent().getUpgrades(Upgrade.MUFFLING), Upgrade.MUFFLING.getMax());
                retVolume = 1.0F - (mufflerCount / (float) Upgrade.MUFFLING.getMax());
            }

            if (tile instanceof ITileSound tileSound) {
                retVolume *= tileSound.getVolume();
            }

            return retVolume;
        }

        @Override
        public float getVolume() {
            if (this.sound == null) {
                this.resolve(Minecraft.getInstance().getSoundManager());
            }
            return super.getVolume();
        }

        @Override
        public boolean canStartSilent() {
            return true;
        }
    }
}
