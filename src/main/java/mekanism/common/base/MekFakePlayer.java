package mekanism.common.base;

import com.mojang.authlib.GameProfile;
import java.lang.ref.WeakReference;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;
import mekanism.common.Mekanism;
import mekanism.common.util.MekanismUtils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.neoforged.neoforge.common.util.FakePlayer;
import org.jetbrains.annotations.NotNull;

/**
 * Global, shared FakePlayer for Mekanism-specific uses
 * <p>
 * This was introduced to fix <a href="https://github.com/dizzyd/Mekanism/issues/2">https://github.com/dizzyd/Mekanism/issues/2</a>. In that issue, another mod was trying
 * to apply a potion to the fake player and causing the whole system to crash due to essential potion related structures not being initialized for a fake player.
 * <p>
 * The broader problem is that the FakePlayer in Forge 14.23.5.2768 isn't really complete and short of patching Forge and requiring everyone in the world to upgrade,
 * there's no easy fix -- so we introduce our own FakePlayer that will let us override other methods as necessary.
 * <p>
 * Use of the fake player is via a consumer type lambda, where usage is only valid inside the lambda. Afterwards it may be garbage collected at any point.
 * <p>
 * Supports emulating a specific UUID, for use with TileComponentSecurity
 */
public class MekFakePlayer extends FakePlayer {

    private static WeakReference<MekFakePlayer> INSTANCE;

    /**
     * UUID of a player we are pretending to be, null to use the default Mek one
     */
    private UUID emulatingUUID = null;

    private MekFakePlayer(ServerLevel world) {
        super(world, new FakeGameProfile());
        ((FakeGameProfile) this.getGameProfile()).myFakePlayer = this;
    }

    @Override
    public boolean canBeAffected(@NotNull MobEffectInstance effect) {
        return false;
    }

    public void setEmulatingUUID(UUID uuid) {
        this.emulatingUUID = uuid;
    }

    @NotNull
    @Override
    public UUID getUUID() {
        return this.emulatingUUID == null ? super.getUUID() : this.emulatingUUID;
    }

    public void cleanupFakePlayer(ServerLevel world) {
        emulatingUUID = null;
        //don't keep reference to the World, note we set it to the overworld to avoid any potential null pointers
        setServerLevel(world.getServer().overworld());
    }

    @SuppressWarnings("WeakerAccess")
    public static MekFakePlayer setupFakePlayer(ServerLevel world) {
        MekFakePlayer actual = INSTANCE == null ? null : INSTANCE.get();
        if (actual == null) {
            actual = new MekFakePlayer(world);
            INSTANCE = new WeakReference<>(actual);
        }
        MekFakePlayer player = actual;
        player.setServerLevel(world);
        return player;
    }

    public static MekFakePlayer setupFakePlayer(ServerLevel world, double x, double y, double z) {
        MekFakePlayer player = setupFakePlayer(world);
        player.setPosRaw(x, y, z);
        return player;
    }

    /**
     * Acquire a Fake Player and call a function which makes use of the player. Afterwards, the Fake Player's world is nulled out to prevent GC issues. Emulated UUID is
     * also reset.
     * <br>
     * Do NOT store a reference to the Fake Player, so that it may be Garbage Collected. A fake player _should_ only need to be short-lived
     *
     * @param world              World to set on the fake player
     * @param fakePlayerConsumer consumer of the fake player
     * @param <R>                Result of a computation, etc
     *
     * @return the return value of fakePlayerConsumer
     */
    @SuppressWarnings("WeakerAccess")
    public static <R> R withFakePlayer(ServerLevel world, Function<MekFakePlayer, R> fakePlayerConsumer) {
        MekFakePlayer player = setupFakePlayer(world);
        R result = fakePlayerConsumer.apply(player);
        player.cleanupFakePlayer(world);
        return result;
    }

    /**
     * Same as {@link MekFakePlayer#withFakePlayer(ServerLevel, java.util.function.Function)} but sets the Fake Player's position. Use when you think the entity position
     * is relevant.
     *
     * @param world              World to set on the fake player
     * @param fakePlayerConsumer consumer of the fake player
     * @param x                  X pos to set
     * @param y                  Y pos to set
     * @param z                  Z pos to set
     * @param <R>                Result of a computation, etc
     *
     * @return the return value of fakePlayerConsumer
     */
    public static <R> R withFakePlayer(ServerLevel world, double x, double y, double z, Function<MekFakePlayer, R> fakePlayerConsumer) {
        MekFakePlayer player = setupFakePlayer(world, x, y, z);
        R result = fakePlayerConsumer.apply(player);
        player.cleanupFakePlayer(world);
        return result;
    }

    public static void releaseInstance(ServerLevel world) {
        // If the fake player has a reference to the world getting unloaded,
        // null out the fake player so that the world can unload
        MekFakePlayer actual = INSTANCE == null ? null : INSTANCE.get();
        if (actual != null && actual.serverLevel() == world) {
            //don't keep reference to the World, note we set it to the overworld to avoid any potential null pointers
            actual.setServerLevel(world.getServer().overworld());
        }
    }

    /**
     * Game profile supporting our UUID emulation
     */
    private static class FakeGameProfile extends GameProfile {

        private MekFakePlayer myFakePlayer = null;

        public FakeGameProfile() {
            super(Mekanism.gameProfile.getId(), Mekanism.gameProfile.getName());
        }

        private UUID getEmulatingUUID() {
            return myFakePlayer == null ? null : myFakePlayer.emulatingUUID;
        }

        @Override
        public UUID getId() {
            UUID emulatingUUID = getEmulatingUUID();
            return emulatingUUID == null ? super.getId() : emulatingUUID;
        }

        @Override
        public String getName() {
            UUID emulatingUUID = getEmulatingUUID();
            return emulatingUUID == null ? super.getName() : MekanismUtils.getLastKnownUsername(emulatingUUID);
        }

        //NB: super check they're the same class, we only check that name & id match
        @Override
        public boolean equals(final Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof GameProfile that)) {
                return false;
            }
            return Objects.equals(getId(), that.getId()) && Objects.equals(getName(), that.getName());
        }

        @Override
        public int hashCode() {
            UUID id = getId();
            String name = getName();
            int result = id == null ? 0 : id.hashCode();
            result = 31 * result + (name == null ? 0 : name.hashCode());
            return result;
        }
    }
}