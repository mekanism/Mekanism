package mekanism.common.command;

import com.mojang.brigadier.builder.ArgumentBuilder;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import mekanism.api.text.ILangEntry;
import mekanism.common.MekanismLang;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.entity.Entity;
import net.minecraft.util.Util;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.server.ServerChunkProvider;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class ChunkCommand {

    private ChunkCommand() {
    }

    private static final LongSet chunkWatchers = new LongOpenHashSet();

    static ArgumentBuilder<CommandSource, ?> register() {
        MinecraftForge.EVENT_BUS.register(ChunkCommand.class);
        return Commands.literal("chunk")
              .then(WatchCommand.register())
              .then(UnwatchCommand.register())
              .then(ClearCommand.register())
              .then(FlushCommand.register());
    }

    private static class WatchCommand {

        static ArgumentBuilder<CommandSource, ?> register() {
            return Commands.literal("watch")
                  .requires(cs -> cs.hasPermissionLevel(2))
                  .executes(ctx -> {
                      CommandSource source = ctx.getSource();
                      Entity entity = source.getEntity();
                      ChunkPos chunkPos = new ChunkPos(entity.getPosition());
                      chunkWatchers.add(ChunkPos.asLong(chunkPos.x, chunkPos.z));
                      source.sendFeedback(MekanismLang.COMMAND_CHUNK_WATCH.translate(chunkPos.x, chunkPos.z), true);
                      return 0;
                  });
        }
    }

    private static class UnwatchCommand {

        static ArgumentBuilder<CommandSource, ?> register() {
            return Commands.literal("unwatch")
                  .requires(cs -> cs.hasPermissionLevel(2))
                  .executes(ctx -> {
                      CommandSource source = ctx.getSource();
                      Entity entity = source.getEntity();
                      ChunkPos chunkPos = new ChunkPos(entity.getPosition());
                      chunkWatchers.remove(ChunkPos.asLong(chunkPos.x, chunkPos.z));
                      source.sendFeedback(MekanismLang.COMMAND_CHUNK_UNWATCH.translate(chunkPos.x, chunkPos.z), true);
                      return 0;
                  });
        }
    }

    private static class ClearCommand {

        static ArgumentBuilder<CommandSource, ?> register() {
            return Commands.literal("clear")
                  .requires(cs -> cs.hasPermissionLevel(2))
                  .executes(ctx -> {
                      int count = chunkWatchers.size();
                      chunkWatchers.clear();
                      ctx.getSource().sendFeedback(MekanismLang.COMMAND_CHUNK_CLEAR.translate(count), true);
                      return 0;
                  });
        }
    }

    private static class FlushCommand {

        static ArgumentBuilder<CommandSource, ?> register() {
            return Commands.literal("flush")
                  .requires(cs -> cs.hasPermissionLevel(2))
                  .executes(ctx -> {
                      CommandSource source = ctx.getSource();
                      ServerChunkProvider sp = source.getWorld().getChunkProvider();
                      int startCount = sp.getLoadedChunkCount();
                      //TODO: Check this
                      //sp.queueUnloadAll();
                      sp.tick(() -> false);
                      ctx.getSource().sendFeedback(MekanismLang.COMMAND_CHUNK_FLUSH.translate(startCount - sp.getLoadedChunkCount()), true);
                      return 0;
                  });
        }
    }

    @SubscribeEvent
    public static void onChunkLoad(ChunkEvent.Load event) {
        handleChunkEvent(event, MekanismLang.COMMAND_CHUNK_LOADED);
    }

    @SubscribeEvent
    public static void onChunkUnload(ChunkEvent.Unload event) {
        handleChunkEvent(event, MekanismLang.COMMAND_CHUNK_UNLOADED);
    }

    private static void handleChunkEvent(ChunkEvent event, ILangEntry direction) {
        if (event.getWorld() == null || event.getWorld().isRemote()) {
            return;
        }
        ChunkPos pos = event.getChunk().getPos();
        if (chunkWatchers.contains(pos.asLong())) {
            ITextComponent message = MekanismLang.COMMAND_CHUNK.translate(direction, pos.x, pos.z);
            event.getWorld().getPlayers().forEach(player -> player.sendMessage(message, Util.DUMMY_UUID));
        }
    }
}