package mekanism.common.command;

import com.mojang.brigadier.builder.ArgumentBuilder;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import mekanism.api.text.EnumColor;
import mekanism.api.text.ILangEntry;
import mekanism.common.MekanismLang;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.ColumnPosArgument;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.ColumnPos;
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
              .requires(cs -> cs.hasPermissionLevel(2))
              .then(WatchCommand.register())
              .then(UnwatchCommand.register())
              .then(ClearCommand.register())
              .then(FlushCommand.register());
    }

    private static class WatchCommand {

        static ArgumentBuilder<CommandSource, ?> register() {
            return Commands.literal("watch")
                  .executes(ctx -> {
                      CommandSource source = ctx.getSource();
                      return watch(source, new ChunkPos(new BlockPos(source.getPos())));
                  }).then(Commands.argument("pos", ColumnPosArgument.columnPos())
                        .executes(ctx -> {
                            ColumnPos column = ColumnPosArgument.fromBlockPos(ctx, "pos");
                            return watch(ctx.getSource(), new ChunkPos(column.x, column.z));
                        }));
        }

        private static int watch(CommandSource source, ChunkPos chunkPos) {
            chunkWatchers.add(ChunkPos.asLong(chunkPos.x, chunkPos.z));
            source.sendFeedback(MekanismLang.COMMAND_CHUNK_WATCH.translateColored(EnumColor.GRAY, EnumColor.INDIGO, getPosition(chunkPos)), true);
            return 0;
        }
    }

    private static class UnwatchCommand {

        static ArgumentBuilder<CommandSource, ?> register() {
            return Commands.literal("unwatch")
                  .executes(ctx -> {
                      CommandSource source = ctx.getSource();
                      return unwatch(source, new ChunkPos(new BlockPos(source.getPos())));
                  }).then(Commands.argument("pos", ColumnPosArgument.columnPos())
                        .executes(ctx -> {
                            ColumnPos column = ColumnPosArgument.fromBlockPos(ctx, "pos");
                            return unwatch(ctx.getSource(), new ChunkPos(column.x, column.z));
                        }));
        }

        private static int unwatch(CommandSource source, ChunkPos chunkPos) {
            chunkWatchers.remove(ChunkPos.asLong(chunkPos.x, chunkPos.z));
            source.sendFeedback(MekanismLang.COMMAND_CHUNK_UNWATCH.translateColored(EnumColor.GRAY, EnumColor.INDIGO, getPosition(chunkPos)), true);
            return 0;
        }
    }

    private static class ClearCommand {

        static ArgumentBuilder<CommandSource, ?> register() {
            return Commands.literal("clear")
                  .executes(ctx -> {
                      int count = chunkWatchers.size();
                      chunkWatchers.clear();
                      ctx.getSource().sendFeedback(MekanismLang.COMMAND_CHUNK_CLEAR.translateColored(EnumColor.GRAY, EnumColor.INDIGO, count), true);
                      return 0;
                  });
        }
    }

    private static class FlushCommand {

        static ArgumentBuilder<CommandSource, ?> register() {
            return Commands.literal("flush")
                  .executes(ctx -> {
                      CommandSource source = ctx.getSource();
                      ServerChunkProvider sp = source.getWorld().getChunkProvider();
                      int startCount = sp.getLoadedChunkCount();
                      //TODO: Check this
                      //sp.queueUnloadAll();
                      sp.tick(() -> false);
                      source.sendFeedback(MekanismLang.COMMAND_CHUNK_FLUSH.translateColored(EnumColor.GRAY, EnumColor.INDIGO, startCount - sp.getLoadedChunkCount()), true);
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
            ITextComponent message = direction.translateColored(EnumColor.GRAY, EnumColor.INDIGO, getPosition(pos));
            event.getWorld().getPlayers().forEach(player -> player.sendMessage(message, Util.DUMMY_UUID));
        }
    }

    private static ITextComponent getPosition(ChunkPos pos) {
        return MekanismLang.GENERIC_WITH_COMMA.translate(pos.x, pos.z);
    }
}