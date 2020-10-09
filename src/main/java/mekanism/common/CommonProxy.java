package mekanism.common;

import java.lang.ref.WeakReference;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import mekanism.client.ClientProxy;
import mekanism.common.base.MekFakePlayer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.network.NetworkEvent.Context;
import net.minecraftforge.fml.server.ServerLifecycleHooks;

/**
 * Common proxy for the Mekanism mod.
 *
 * @author AidanBrady
 */
public class CommonProxy {

    public static CommonProxy createClientProxy() {
        return new ClientProxy();
    }

    /**
     * Set up and load the utilities this mod uses.
     */
    public void init() {
        MinecraftForge.EVENT_BUS.register(Mekanism.worldTickHandler);
    }

    /**
     * Whether or not the game is paused.
     */
    public boolean isPaused() {
        return false;
    }

    /**
     * Does the multiblock creation animation, starting from the rendering block.
     */
    public void doMultiblockSparkle(TileEntity tile, BlockPos renderLoc, int length, int width, int height) {
    }

    public double getReach(PlayerEntity player) {
        if (player instanceof ServerPlayerEntity) {
            return player.getAttribute(net.minecraftforge.common.ForgeMod.REACH_DISTANCE.get()).getValue();
        }
        return 0;
    }

    public PlayerEntity getPlayer(Supplier<Context> context) {
        return context.get().getSender();
    }

    @Nullable
    public World tryGetMainWorld() {
        return ServerLifecycleHooks.getCurrentServer().func_241755_D_();
    }
}