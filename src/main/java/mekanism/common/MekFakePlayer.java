package mekanism.common;

import com.mojang.authlib.GameProfile;
import java.lang.ref.WeakReference;
import javax.annotation.Nonnull;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.potion.PotionEffect;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.util.FakePlayer;

// Global, shared FakePlayer for Mekanism-specific uses
//
// This was introduced to fix https://github.com/dizzyd/Mekanism/issues/2. In that issue,
// another mod was trying to apply a potion to the fake player and causing the whole system
// to crash due to essential potion related structures not being initialized for a fake player.
//
// The broader problem is that the FakePlayer in Forge 14.23.5.2768 isn't really complete and
// short of patching Forge and requiring everyone in the world to upgrade, there's no easy fix --
// so we introduce our own FakePlayer that will let us override other methods as necessary.
//
// It's worth noting that there is only a single instance of this player in an entire server; this
// is in keeping with the original semantics, where a single instance of FakePlayer was shared globally
// and the world/position coordinates adjusted every time it was accessed. It's possible there are race
// conditions here if the player is used in two threads at once, but given the nature of the calls and
// the history, I believe this singleton approach is not unreasonable.
public class MekFakePlayer extends FakePlayer {

    private static MekFakePlayer INSTANCE;

    public MekFakePlayer(WorldServer world, GameProfile name) {
        super(world, name);
    }

    public static WeakReference<EntityPlayer> getInstance(WorldServer world) {
        if (INSTANCE == null) {
            INSTANCE = new MekFakePlayer(world, Mekanism.gameProfile);
        }
        INSTANCE.world = world;
        return new WeakReference<>(INSTANCE);
    }

    public static WeakReference<EntityPlayer> getInstance(WorldServer world, double x, double y, double z) {
        if (INSTANCE == null) {
            INSTANCE = new MekFakePlayer(world, Mekanism.gameProfile);
        }

        INSTANCE.world = world;
        INSTANCE.posX = x;
        INSTANCE.posY = y;
        INSTANCE.posZ = z;
        return new WeakReference<>(INSTANCE);
    }

    public static void releaseInstance(World world) {
        // If the fake player has a reference to the world getting unloaded,
        // null out the fake player so that the world can unload
        if (INSTANCE != null && INSTANCE.world == world) {
            INSTANCE = null;
        }
    }

    @Override
    public boolean isPotionApplicable(@Nonnull PotionEffect effect) {
        return false;
    }
}
