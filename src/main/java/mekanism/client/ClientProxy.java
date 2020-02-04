package mekanism.client;

import java.util.function.Supplier;
import mekanism.api.Coord4D;
import mekanism.api.text.EnumColor;
import mekanism.client.SparkleAnimation.INodeChecker;
import mekanism.client.gui.GuiPortableTeleporter;
import mekanism.client.render.RenderTickHandler;
import mekanism.client.sound.SoundHandler;
import mekanism.common.CommonProxy;
import mekanism.common.MekanismLang;
import mekanism.common.config.MekanismConfig;
import mekanism.common.network.PacketPortableTeleporter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkEvent.Context;

/**
 * Client proxy for the Mekanism mod.
 *
 * @author AidanBrady
 */
public class ClientProxy extends CommonProxy {

    @Override
    public void handleTeleporterUpdate(PacketPortableTeleporter message) {
        Screen screen = Minecraft.getInstance().currentScreen;

        if (screen instanceof GuiPortableTeleporter) {
            GuiPortableTeleporter teleporter = (GuiPortableTeleporter) screen;
            teleporter.setStatus(message.getStatus());
            teleporter.setFrequency(message.getFrequency());
            teleporter.setPublicCache(message.getPublicCache());
            teleporter.setPrivateCache(message.getPrivateCache());
            teleporter.updateButtons();
        }
    }

    @Override
    public void addHitEffects(Coord4D coord, BlockRayTraceResult mop) {
        if (Minecraft.getInstance().world != null) {
            Minecraft.getInstance().particles.addBlockHitEffects(coord.getPos(), mop);
        }
    }

    private void doSparkle(TileEntity tile, SparkleAnimation anim) {
        ClientPlayerEntity player = Minecraft.getInstance().player;
        // If player is within 16 blocks (256 = 16^2), show the status message/sparkles
        if (tile.getPos().distanceSq(player.getPosition()) <= 256) {
            if (MekanismConfig.client.enableMultiblockFormationParticles.get()) {
                anim.run();
            } else {
                player.sendStatusMessage(MekanismLang.MULTIBLOCK_FORMED_CHAT.translateColored(EnumColor.INDIGO), true);
            }
        }
    }

    @Override
    public void doMultiblockSparkle(TileEntity tile, BlockPos renderLoc, int length, int width, int height, INodeChecker checker) {
        doSparkle(tile, new SparkleAnimation(tile, renderLoc, length, width, height, checker));
    }

    @Override
    public void doMultiblockSparkle(TileEntity tile, BlockPos corner1, BlockPos corner2, INodeChecker checker) {
        doSparkle(tile, new SparkleAnimation(tile, corner1, corner2, checker));
    }

    @Override
    public void init() {
        super.init();

        //MinecraftForge.EVENT_BUS.register(new ClientConnectionHandler());
        MinecraftForge.EVENT_BUS.register(new ClientPlayerTracker());
        MinecraftForge.EVENT_BUS.register(new ClientTickHandler());
        MinecraftForge.EVENT_BUS.register(new RenderTickHandler());
        MinecraftForge.EVENT_BUS.register(SoundHandler.class);

        new MekanismKeyHandler();

        HolidayManager.init();
    }

    @Override
    public double getReach(PlayerEntity player) {
        return Minecraft.getInstance().playerController == null ? 8 : Minecraft.getInstance().playerController.getBlockReachDistance();
    }

    @Override
    public boolean isPaused() {
        if (Minecraft.getInstance().isSingleplayer() && !Minecraft.getInstance().getIntegratedServer().getPublic()) {
            //TODO: Make sure that gui's that pause game react to this properly
            return Minecraft.getInstance().isGamePaused();
        }
        return false;
    }

    @Override
    public PlayerEntity getPlayer(Supplier<Context> context) {
        if (context.get().getDirection() == NetworkDirection.PLAY_TO_SERVER || context.get().getDirection() == NetworkDirection.LOGIN_TO_SERVER) {
            return context.get().getSender();
        }
        return Minecraft.getInstance().player;
    }

    //TODO
    /*@Override
    public void throwApiPresentException() {
        throw new ApiJarPresentException(API_PRESENT_MESSAGE);
    }*/
}