package mekanism.common;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.function.Supplier;
import mekanism.api.Coord4D;
import mekanism.api.MekanismAPI;
import mekanism.client.SparkleAnimation.INodeChecker;
import mekanism.common.config.MekanismConfig;
import mekanism.common.network.PacketPortableTeleporter;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.fml.network.NetworkEvent.Context;

/**
 * Common proxy for the Mekanism mod.
 *
 * @author AidanBrady
 */
//TODO: I don't think IGuiProvider is needed with 1.14 and can be done through forge hooks instead
public class CommonProxy/* implements IGuiProvider*/ {

    protected final String[] API_PRESENT_MESSAGE = {"Mekanism API jar detected (Mekanism-<version>-api.jar),",
                                                    "please delete it from your mods folder and restart the game."};

    public void handleTeleporterUpdate(PacketPortableTeleporter message) {
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
     * Adds block hit effects on the client side.
     */
    public void addHitEffects(Coord4D coord, BlockRayTraceResult mop) {
    }

    /**
     * Does the multiblock creation animation, starting from the rendering block.
     */
    public void doMultiblockSparkle(TileEntity tileEntity, BlockPos corner1, BlockPos corner2, INodeChecker checker) {
    }

    /**
     * Does the multiblock creation animation, starting from the rendering block.
     */
    public void doMultiblockSparkle(TileEntity tileEntity, BlockPos renderLoc, int length, int width, int height, INodeChecker checker) {
    }

    public double getReach(PlayerEntity player) {
        if (player instanceof ServerPlayerEntity) {
            return player.getAttribute(PlayerEntity.REACH_DISTANCE).getValue();
        }
        return 0;
    }

    /**
     * Gets the Minecraft base directory.
     *
     * @return base directory
     */
    public File getMinecraftDir() {
        //TODO: Check if this is correct, honestly cardboard box blacklist should be rewritten how the file works
        return FMLPaths.GAMEDIR.get().toFile();
    }

    public void onConfigSync(boolean fromPacket) {
        if (MekanismConfig.general.cardboardSpawners.get()) {
            MekanismAPI.removeBoxBlacklist(Blocks.SPAWNER);
        } else {
            MekanismAPI.addBoxBlacklist(Blocks.SPAWNER);
        }
        //TODO: Move this to additions and fix. Currently there is no config sync so there isn't a great spot to put this
        /*if (MekanismConfig.general.voiceServerEnabled.get() && Mekanism.voiceManager == null) {
            Mekanism.voiceManager = new VoiceServerManager();
        }*/
        if (fromPacket) {
            Mekanism.logger.info("Received config from server.");
        }
    }

    public final WeakReference<PlayerEntity> getDummyPlayer(ServerWorld world) {
        return MekFakePlayer.getInstance(world);
    }

    public final WeakReference<PlayerEntity> getDummyPlayer(ServerWorld world, double x, double y, double z) {
        return MekFakePlayer.getInstance(world, x, y, z);
    }

    public final WeakReference<PlayerEntity> getDummyPlayer(ServerWorld world, BlockPos pos) {
        return getDummyPlayer(world, pos.getX(), pos.getY(), pos.getZ());
    }

    public PlayerEntity getPlayer(Supplier<Context> context) {
        return context.get().getSender();
    }

    public Object getFontRenderer() {
        return null;
    }

    public void throwApiPresentException() {
        throw new RuntimeException(String.join(" ", API_PRESENT_MESSAGE));
    }
}