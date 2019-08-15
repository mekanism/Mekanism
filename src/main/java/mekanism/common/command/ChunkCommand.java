package mekanism.common.command;

import com.mojang.brigadier.builder.ArgumentBuilder;
import java.util.HashSet;
import java.util.Set;
import mekanism.common.util.text.TextComponentUtil;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.server.ServerChunkProvider;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class ChunkCommand {

    private static Set<Long> chunkWatchers = new HashSet<>();

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
                  .requires(cs -> cs.hasPermissionLevel(4))
                  .executes(ctx -> {
                      CommandSource source = ctx.getSource();
                      Entity entity = source.getEntity();
                      ChunkPos cpos = new ChunkPos(entity.getPosition());
                      chunkWatchers.add(ChunkPos.asLong(cpos.x, cpos.z));
                      return 0;
                  });
        }
    }

    private static class UnwatchCommand {

        static ArgumentBuilder<CommandSource, ?> register() {
            return Commands.literal("unwatch")
                  .requires(cs -> cs.hasPermissionLevel(4))
                  .executes(ctx -> {
                      CommandSource source = ctx.getSource();
                      Entity entity = source.getEntity();
                      ChunkPos cpos = new ChunkPos(entity.getPosition());
                      chunkWatchers.remove(ChunkPos.asLong(cpos.x, cpos.z));
                      return 0;
                  });
        }
    }

    private static class ClearCommand {

        static ArgumentBuilder<CommandSource, ?> register() {
            return Commands.literal("clear")
                  .requires(cs -> cs.hasPermissionLevel(4))
                  .executes(ctx -> {
                      int count = chunkWatchers.size();
                      chunkWatchers.clear();
                      //TODO: Print number cleared
                      return 0;
                  });
        }
    }

    private static class FlushCommand {

        static ArgumentBuilder<CommandSource, ?> register() {
            return Commands.literal("flush")
                  .requires(cs -> cs.hasPermissionLevel(4))
                  .requires(cs -> !cs.getEntity().getEntityWorld().isRemote)//TODO: Is this the proper way to have this
                  .executes(ctx -> {
                      CommandSource source = ctx.getSource();
                      Entity entity = source.getEntity();
                      ServerChunkProvider sp = (ServerChunkProvider) entity.getEntityWorld().getChunkProvider();
                      int startCount = sp.getLoadedChunkCount();
                      //TODO: Check this
                      //sp.queueUnloadAll();
                      sp.tick(() -> false);
                      return 0;
                  });
        }
    }

    @SubscribeEvent
    public static void onChunkLoad(ChunkEvent.Load event) {
        handleChunkEvent(event, "Loaded");
    }

    @SubscribeEvent
    public static void onChunkUnload(ChunkEvent.Unload event) {
        handleChunkEvent(event, "Unloaded");
    }

    private static void handleChunkEvent(ChunkEvent event, String direction) {
        if (event.getWorld().isRemote()) {
            return;
        }
        ChunkPos pos = event.getChunk().getPos();
        long key = ChunkPos.asLong(pos.x, pos.z);
        if (chunkWatchers.contains(key)) {
            String msg = String.format("%s chunk %d, %d", direction, pos.x, pos.z);
            ITextComponent message = TextComponentUtil.build(msg);
            event.getWorld().getPlayers().forEach(player -> player.sendMessage(message));
        }
    }
}