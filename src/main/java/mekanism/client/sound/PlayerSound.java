package mekanism.client.sound;

import mekanism.common.config.MekanismConfig.client;
import net.minecraft.client.audio.ITickableSound;
import net.minecraft.client.audio.PositionedSound;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public abstract class PlayerSound extends PositionedSound implements ITickableSound {

    protected EntityPlayer player;

    private float fadeUpStep = 0.1f;
    private float fadeDownStep = 0.1f;

    private boolean donePlaying = false;

    public PlayerSound(EntityPlayer player, ResourceLocation sound) {
        super(sound, SoundCategory.PLAYERS);
        this.player = player;
        this.repeat = true;
        this.repeatDelay = 0;

        // N.B. the volume must be > 0 on first time it's processed by sound system or else it will not
        // get registered for tick events.
        this.volume = 0.1f;
    }

    @Override
    public float getXPosF() {
        return (float) player.posX;
    }

    @Override
    public float getYPosF() {
        return (float) player.posY;
    }

    @Override
    public float getZPosF() {
        return (float) player.posZ;
    }

    @Override
    public void update() {
        if (player.isDead) {
            this.donePlaying = true;
            this.volume = 0.0f;
            return;
        }

        if (shouldPlaySound()) {
            if (volume < 1.0f) {
                // If we weren't max volume, start fading up
                volume = Math.max(1.0f, (volume + fadeUpStep));
            }
        } else {
            // Not yet fully muted, fade down
            if (volume > 0.0f) {
                volume = Math.max(0.0f, (volume - fadeDownStep));
            }
        }
    }

    @Override
    public boolean isDonePlaying() {
        return donePlaying;
    }

    public abstract boolean shouldPlaySound();

    @Override
    public float getVolume() {
        return super.getVolume() * client.baseSoundVolume;
    }
}
