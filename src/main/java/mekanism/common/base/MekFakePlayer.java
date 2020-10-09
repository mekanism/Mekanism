package mekanism.common.base;

import java.lang.ref.WeakReference;
import java.util.function.Function;
import javax.annotation.Nonnull;
import mekanism.common.Mekanism;
import net.minecraft.potion.EffectInstance;
import net.minecraft.world.IWorld;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.util.FakePlayer;

/**
 * Global, shared FakePlayer for Mekanism-specific uses
 *
 * This was introduced to fix https://github.com/dizzyd/Mekanism/issues/2. In that issue,
 * another mod was trying to apply a potion to the fake player and causing the whole system
 * to crash due to essential potion related structures not being initialized for a fake player.
 *
 * The broader problem is that the FakePlayer in Forge 14.23.5.2768 isn't really complete and
 * short of patching Forge and requiring everyone in the world to upgrade, there's no easy fix --
 * so we introduce our own FakePlayer that will let us override other methods as necessary.
 *
 * Use of the fake player is via a consumer type lambda, where usage is only valid inside the lambda.
 * Afterwards it may be garbage collected at any point.
 */
public class MekFakePlayer extends FakePlayer {

    private static WeakReference<MekFakePlayer> INSTANCE;

    public MekFakePlayer(ServerWorld world) {
        super(world, Mekanism.gameProfile);
    }

    @Override
    public boolean isPotionApplicable(@Nonnull EffectInstance effect) {
        return false;
    }

    /**
     * Acquire a Fake Player and call a function which makes use of the player.
     * Afterwards, the Fake Player's world is nulled out to prevent GC issues.
     *
     * Do NOT store a reference to the Fake Player, so that it may be Garbage Collected.
     * A fake player _should_ only need to be short-lived
     *
     * @param world World to set on the fake player
     * @param fakePlayerConsumer consumer of the fake player
     * @param <R> Result of a computation, etc
     * @return the return value of fakePlayerConsumer
     */
    public static <R> R withFakePlayer(ServerWorld world, Function<MekFakePlayer, R> fakePlayerConsumer) {
        MekFakePlayer actual = INSTANCE != null ? INSTANCE.get() : null;
        if (actual == null) {
            actual = new MekFakePlayer(world);
            INSTANCE = new WeakReference<>(actual);
        }
        MekFakePlayer player = actual;
        player.world = world;
        R result = fakePlayerConsumer.apply(player);
        player.world = null;//don't keep reference to the World
        return result;
    }

    /**
     * Same as {@link MekFakePlayer#withFakePlayer(net.minecraft.world.server.ServerWorld, java.util.function.Function)}
     * but sets the Fake Player's position. Use when you think the entity position is relevant.
     *
     * @param world World to set on the fake player
     * @param fakePlayerConsumer consumer of the fake player
     * @param x X pos to set
     * @param y Y pos to set
     * @param z Z pos to set
     * @param <R> Result of a computation, etc
     * @return the return value of fakePlayerConsumer
     */
    public static <R> R withFakePlayer(ServerWorld world, double x, double y, double z, Function<MekFakePlayer, R> fakePlayerConsumer) {
        return withFakePlayer(world, fakePlayer->{
            fakePlayer.setRawPosition(x, y, z);
            return fakePlayerConsumer.apply(fakePlayer);
        });
    }

    public static void releaseInstance(IWorld world) {
        // If the fake player has a reference to the world getting unloaded,
        // null out the fake player so that the world can unload
        MekFakePlayer actual = INSTANCE != null ? INSTANCE.get() : null;
        if (actual != null && actual.world == world) {
            actual.world = null;
        }
    }
}