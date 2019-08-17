package mekanism.common;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.function.Supplier;
import mekanism.api.Coord4D;
import mekanism.api.MekanismAPI;
import mekanism.api.Pos3D;
import mekanism.client.SparkleAnimation.INodeChecker;
import mekanism.common.config.MekanismConfig;
import mekanism.common.network.PacketPortableTeleporter;
import mekanism.common.voice.VoiceServerManager;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.World;
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

    /**
     * Register tile entities that have special models. Overwritten in client to register TESRs.
     */
    public void registerTESRs() {
    }

    public void handleTeleporterUpdate(PacketPortableTeleporter message) {
    }

    /**
     * Register and load client-only item render information.
     */
    public void registerItemRenders() {
    }

    /**
     * Register and load client-only block render information.
     */
    public void registerBlockRenders() {
    }

    /**
     * Set and load the mod's common configuration properties.
     */
    public void loadConfiguration() {
        //TODO??
        /*MekanismConfigOld.local().general.load(Mekanism.configuration);
        MekanismConfigOld.local().usage.load(Mekanism.configuration);
        MekanismConfigOld.local().storage.load(Mekanism.configuration);
        if (Mekanism.configuration.hasChanged()) {
            Mekanism.configuration.save();
        }*/
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

    public void registerScreenHandlers() {
    }

    public void preInit() {
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
        if (MekanismConfig.general.voiceServerEnabled.get() && Mekanism.voiceManager == null) {
            Mekanism.voiceManager = new VoiceServerManager();
        }
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

    public void handlePacket(Runnable runnable, PlayerEntity player) {
        if (player instanceof ServerPlayerEntity) {
            //TODO
            //player.world.getWorldInfo().getScheduledEvents().scheduleReplaceDuplicate();
            ((ServerWorld) player.world).addScheduledTask(runnable);
        }
    }

    public void renderLaser(World world, Pos3D from, Pos3D to, Direction direction, double energy) {
    }

    public Object getFontRenderer() {
        return null;
    }

    public void throwApiPresentException() {
        throw new RuntimeException(String.join(" ", API_PRESENT_MESSAGE));
    }
}