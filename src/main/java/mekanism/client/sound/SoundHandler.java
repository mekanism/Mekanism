package mekanism.client.sound;

import mekanism.common.Mekanism;
import mekanism.common.config.MekanismConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.ISound;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.IdentityHashMap;

// SoundHandler is the central point for sounds on Mek client side. Some sounds, such as hand-held items
// are instantiated the first time user uses one and stays in memory (though muted) until the player
// leaves the server. Block/tile-based sounds are periodically restarted while machine is active; this ensures
// that thousands of idle machines aren't filling up memory with sounds that are rarely running.
//
// As of 1.12.2, this design closely mirrors that of Minecraft sounds and should be generally compatible with any
// other mod that interacts with the sound system (such as mufflers)

@SideOnly(Side.CLIENT)
public class SoundHandler {
    private static IdentityHashMap<EntityPlayer, Boolean> jetpackSounds = new IdentityHashMap();
    private static IdentityHashMap<EntityPlayer, Boolean> gasmaskSounds = new IdentityHashMap<>();

    public static void startSound(EntityPlayer player, String soundName) {
        ISound soundToPlay = null;

        if (soundName.equals("jetpack") && !jetpackSounds.containsKey(player)) {
            jetpackSounds.put(player, true);
            soundToPlay = new JetpackSound(player);
        }
        if (soundName.equals("gasmask") && !gasmaskSounds.containsKey(player)) {
            gasmaskSounds.put(player, true);
            soundToPlay = new GasMaskSound(player);
        }

        if (soundToPlay != null) {
            Mekanism.logger.info("Starting sound object for {}: {}", player.getName(), soundName);
            Minecraft.getMinecraft().getSoundHandler().playSound(soundToPlay);
        }
    }

    public static void playSound(SoundEvent sound)
    {
        playSound(PositionedSoundRecord.getMasterRecord(sound, MekanismConfig.client.baseSoundVolume));
    }

    public static void playSound(ISound sound)
    {
        Minecraft.getMinecraft().getSoundHandler().playSound(sound);
    }
}
