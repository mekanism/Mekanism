package mekanism.client.sound;

import java.lang.ref.WeakReference;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.common.config.MekanismConfig;
import mekanism.common.registration.impl.SoundEventRegistryObject;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.ISoundEventListener;
import net.minecraft.client.audio.SoundEventAccessor;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.audio.TickableSound;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;

public abstract class PlayerSound extends TickableSound {

    @Nonnull
    private final WeakReference<PlayerEntity> playerReference;
    private final int subtitleFrequency;
    private float lastX;
    private float lastY;
    private float lastZ;

    private float fadeUpStep = 0.1f;
    private float fadeDownStep = 0.1f;
    private int consecutiveTicks;

    public PlayerSound(@Nonnull PlayerEntity player, @Nonnull SoundEventRegistryObject<?> sound) {
        this(player, sound.get(), 60);
        //Set it to repeat the subtitle every 3 seconds the sound is constantly playing
    }

    public PlayerSound(@Nonnull PlayerEntity player, @Nonnull SoundEvent sound, int subtitleFrequency) {
        super(sound, SoundCategory.PLAYERS);
        this.playerReference = new WeakReference<>(player);
        this.subtitleFrequency = subtitleFrequency;
        this.lastX = (float) player.getPosX();
        this.lastY = (float) player.getPosY();
        this.lastZ = (float) player.getPosZ();
        this.repeat = true;
        this.repeatDelay = 0;

        // N.B. the volume must be > 0 on first time it's processed by sound system or else it will not
        // get registered for tick events.
        this.volume = 0.1F;
    }

    @Nullable
    private PlayerEntity getPlayer() {
        return playerReference.get();
    }

    protected void setFade(float fadeUpStep, float fadeDownStep) {
        this.fadeUpStep = fadeUpStep;
        this.fadeDownStep = fadeDownStep;
    }

    @Override
    public double getX() {
        //Gracefully handle the player becoming null if this object is kept around after update marks us as donePlaying
        PlayerEntity player = getPlayer();
        if (player != null) {
            this.lastX = (float) player.getPosX();
        }
        return this.lastX;
    }

    @Override
    public double getY() {
        //Gracefully handle the player becoming null if this object is kept around after update marks us as donePlaying
        PlayerEntity player = getPlayer();
        if (player != null) {
            this.lastY = (float) player.getPosY();
        }
        return this.lastY;
    }

    @Override
    public double getZ() {
        //Gracefully handle the player becoming null if this object is kept around after update marks us as donePlaying
        PlayerEntity player = getPlayer();
        if (player != null) {
            this.lastZ = (float) player.getPosZ();
        }
        return this.lastZ;
    }

    @Override
    public void tick() {
        PlayerEntity player = getPlayer();
        if (player == null || !player.isAlive()) {
            finishPlaying();
            volume = 0.0F;
            consecutiveTicks = 0;
            return;
        }

        if (shouldPlaySound(player)) {
            if (volume < 1.0F) {
                // If we weren't max volume, start fading up
                volume = Math.min(1.0F, volume + fadeUpStep);
            }
            if (consecutiveTicks % subtitleFrequency == 0) {
                SoundHandler soundHandler = Minecraft.getInstance().getSoundHandler();
                for (ISoundEventListener soundEventListener : soundHandler.sndManager.listeners) {
                    SoundEventAccessor soundEventAccessor = createAccessor(soundHandler);
                    if (soundEventAccessor != null) {
                        soundEventListener.onPlaySound(this, soundEventAccessor);
                    }
                }
                consecutiveTicks = 1;
            } else {
                consecutiveTicks++;
            }
        } else if (volume > 0.0F) {
            consecutiveTicks = 0;
            // Not yet fully muted, fade down
            volume = Math.max(0.0F, volume - fadeDownStep);
        }
    }

    public abstract boolean shouldPlaySound(@Nonnull PlayerEntity player);

    @Override
    public float getVolume() {
        return super.getVolume() * MekanismConfig.client.baseSoundVolume.get();
    }

    @Override
    public boolean canBeSilent() {
        return true;
    }

    @Override
    public boolean shouldPlaySound() {
        PlayerEntity player = getPlayer();
        if (player == null) {
            return super.shouldPlaySound();
        }
        return !player.isSilent();
    }

    public enum SoundType {
        FLAMETHROWER,
        JETPACK,
        SCUBA_MASK,
        GRAVITATIONAL_MODULATOR
    }
}