package mekanism.client.sound;

import java.lang.ref.WeakReference;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.common.config.MekanismConfig;
import net.minecraft.client.audio.ITickableSound;
import net.minecraft.client.audio.PositionedSound;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public abstract class PlayerSound extends PositionedSound implements ITickableSound {

    @Nonnull
    private WeakReference<EntityPlayer> playerReference;
    private float lastX;
    private float lastY;
    private float lastZ;

    private float fadeUpStep = 0.1f;
    private float fadeDownStep = 0.1f;

    private boolean donePlaying = false;

    public PlayerSound(@Nonnull EntityPlayer player, @Nonnull ResourceLocation sound) {
        super(sound, SoundCategory.PLAYERS);
        this.playerReference = new WeakReference<>(player);
        this.lastX = (float) player.posX;
        this.lastY = (float) player.posY;
        this.lastZ = (float) player.posZ;
        this.repeat = true;
        this.repeatDelay = 0;

        // N.B. the volume must be > 0 on first time it's processed by sound system or else it will not
        // get registered for tick events.
        this.volume = 0.1F;
    }

    @Nullable
    private EntityPlayer getPlayer() {
        return playerReference.get();
    }

    @Override
    public float getXPosF() {
        //Gracefully handle the player becoming null if this object is kept around after update marks us as donePlaying
        EntityPlayer player = getPlayer();
        if (player != null) {
            this.lastX = (float) player.posX;
        }
        return this.lastX;
    }

    @Override
    public float getYPosF() {
        //Gracefully handle the player becoming null if this object is kept around after update marks us as donePlaying
        EntityPlayer player = getPlayer();
        if (player != null) {
            this.lastY = (float) player.posY;
        }
        return this.lastY;
    }

    @Override
    public float getZPosF() {
        //Gracefully handle the player becoming null if this object is kept around after update marks us as donePlaying
        EntityPlayer player = getPlayer();
        if (player != null) {
            this.lastZ = (float) player.posZ;
        }
        return this.lastZ;
    }

    @Override
    public void update() {
        EntityPlayer player = getPlayer();
        if (player == null || player.isDead) {
            this.donePlaying = true;
            this.volume = 0.0F;
            return;
        }

        if (shouldPlaySound(player)) {
            if (volume < 1.0F) {
                // If we weren't max volume, start fading up
                volume = Math.min(1.0F, volume + fadeUpStep);
            }
        } else if (volume > 0.0F) {
            // Not yet fully muted, fade down
            volume = Math.max(0.0F, volume - fadeDownStep);
        }
    }

    @Override
    public boolean isDonePlaying() {
        return donePlaying;
    }

    public abstract boolean shouldPlaySound(@Nonnull EntityPlayer player);

    @Override
    public float getVolume() {
        return (float) (super.getVolume() * MekanismConfig.current().client.baseSoundVolume.val());
    }

    public enum SoundType {
        FLAMETHROWER,
        JETPACK,
        GAS_MASK
    }
}