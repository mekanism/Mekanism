package mekanism.client;

import java.util.function.Supplier;
import javax.annotation.Nullable;
import mekanism.api.text.EnumColor;
import mekanism.client.render.RenderTickHandler;
import mekanism.client.sound.SoundHandler;
import mekanism.common.CommonProxy;
import mekanism.common.MekanismLang;
import mekanism.common.base.HolidayManager;
import mekanism.common.config.MekanismConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.network.NetworkEvent.Context;

/**
 * Client proxy for the Mekanism mod.
 *
 * @author AidanBrady
 */
public class ClientProxy extends CommonProxy {

    private void doSparkle(TileEntity tile, SparkleAnimation anim) {
        ClientPlayerEntity player = Minecraft.getInstance().player;
        //If player is within 40 blocks (1,600 = 40^2), show the status message/sparkles
        if (tile.getPos().distanceSq(player.getPosition()) <= 1_600) {
            if (MekanismConfig.client.enableMultiblockFormationParticles.get()) {
                anim.run();
            } else {
                player.sendStatusMessage(MekanismLang.MULTIBLOCK_FORMED_CHAT.translateColored(EnumColor.INDIGO), true);
            }
        }
    }

    @Override
    public void doMultiblockSparkle(TileEntity tile, BlockPos renderLoc, int length, int width, int height) {
        doSparkle(tile, new SparkleAnimation(tile, renderLoc, length, width, height));
    }

    @Override
    public void init() {
        super.init();
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
            return Minecraft.getInstance().isGamePaused();
        }
        return false;
    }

    @Override
    public PlayerEntity getPlayer(Supplier<Context> context) {
        if (context.get().getDirection().getReceptionSide().isServer()) {
            return super.getPlayer(context);
        }
        return Minecraft.getInstance().player;
    }

    @Nullable
    @Override
    public World tryGetMainWorld() {
        return Minecraft.getInstance().world;
    }
}