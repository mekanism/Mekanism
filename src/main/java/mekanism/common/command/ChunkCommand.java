package mekanism.common.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import mekanism.api.text.EnumColor;
import mekanism.api.text.ILangEntry;
import mekanism.api.text.TextComponentUtil;
import mekanism.common.MekanismLang;
import mekanism.common.base.MekanismPermissions;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.ColumnPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ColumnPos;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelAccessor;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.level.ChunkEvent;
import net.neoforged.neoforge.event.level.ChunkTicketLevelUpdatedEvent;
import org.jetbrains.annotations.Nullable;

public class ChunkCommand {

    private ChunkCommand() {
    }

    private static final String NAME_PARAM = "name";
    private static final String POS_PARAM = "pos";

    private static final LangData WATCH = new LangData(MekanismLang.COMMAND_CHUNK_WATCH, MekanismLang.COMMAND_CHUNK_WATCH_NAMED);
    private static final LangData UNWATCH = new LangData(MekanismLang.COMMAND_CHUNK_UNWATCH, MekanismLang.COMMAND_CHUNK_UNWATCH_NAMED);
    private static final LangData LOADED = new LangData(MekanismLang.COMMAND_CHUNK_LOADED, MekanismLang.COMMAND_CHUNK_LOADED_NAMED);
    private static final LangData UNLOADED = new LangData(MekanismLang.COMMAND_CHUNK_UNLOADED, MekanismLang.COMMAND_CHUNK_UNLOADED_NAMED);
    //TODO: Allow specifying watches for chunks in different dimensions?
    private static final Long2ObjectMap<ChunkWatchSettings> chunkWatchers = new Long2ObjectOpenHashMap<>();

    static ArgumentBuilder<CommandSourceStack, ?> register() {
        NeoForge.EVENT_BUS.register(ChunkCommand.class);
        return Commands.literal("chunk")
              .requires(MekanismPermissions.COMMAND_CHUNK)
              .then(WatchCommand.register())
              .then(UnwatchCommand.register())
              .then(ClearCommand.register())
              .then(FlushCommand.register());
    }

    private static class WatchCommand {

        static ArgumentBuilder<CommandSourceStack, ?> register() {
            return Commands.literal("watch")
                  .requires(MekanismPermissions.COMMAND_CHUNK_WATCH)
                  .executes(ctx -> watch(ctx, false, false))
                  .then(Commands.argument(POS_PARAM, ColumnPosArgument.columnPos())
                        .executes(ctx -> watch(ctx, true, false))
                        .then(Commands.argument(NAME_PARAM, StringArgumentType.word())
                              .executes(ctx -> watch(ctx, true, true))
                        )
                  )
                  .then(Commands.argument(NAME_PARAM, StringArgumentType.word())
                        .executes(ctx -> watch(ctx, false, true))
                  );
        }

        private static int watch(CommandContext<CommandSourceStack> ctx, boolean positionFromArgument, boolean hasName) {
            ChunkPos chunkPos;
            if (positionFromArgument) {
                chunkPos = ColumnPosArgument.getColumnPos(ctx, POS_PARAM).toChunkPos();
            } else {
                chunkPos = new ChunkPos(BlockPos.containing(ctx.getSource().getPosition()));
            }
            String name = hasName ? StringArgumentType.getString(ctx, NAME_PARAM) : null;
            ChunkWatchSettings settings = new ChunkWatchSettings(name, chunkPos);
            chunkWatchers.put(ChunkPos.asLong(chunkPos.x, chunkPos.z), settings);
            ctx.getSource().sendSuccess(() -> settings.translate(WATCH), true);
            return Command.SINGLE_SUCCESS;
        }
    }

    private static class UnwatchCommand {

        static ArgumentBuilder<CommandSourceStack, ?> register() {
            return Commands.literal("unwatch")
                  .requires(MekanismPermissions.COMMAND_CHUNK_UNWATCH)
                  .executes(ctx -> {
                      CommandSourceStack source = ctx.getSource();
                      return unwatch(source, new ChunkPos(BlockPos.containing(source.getPosition())));
                  }).then(Commands.argument(POS_PARAM, ColumnPosArgument.columnPos())
                        .executes(ctx -> {
                            ColumnPos column = ColumnPosArgument.getColumnPos(ctx, POS_PARAM);
                            return unwatch(ctx.getSource(), column.toChunkPos());
                        }));
        }

        private static int unwatch(CommandSourceStack source, ChunkPos chunkPos) {
            ChunkWatchSettings settings = chunkWatchers.remove(ChunkPos.asLong(chunkPos.x, chunkPos.z));
            if (settings == null) {
                source.sendFailure(MekanismLang.COMMAND_ERROR_NOT_WATCHED.translate(MekanismLang.GENERIC_WITH_COMMA.translate(chunkPos.x, chunkPos.z)));
                return 0;
            }
            source.sendSuccess(() -> settings.translate(UNWATCH), true);
            return Command.SINGLE_SUCCESS;
        }
    }

    private static class ClearCommand {

        static ArgumentBuilder<CommandSourceStack, ?> register() {
            return Commands.literal("clear")
                  .requires(MekanismPermissions.COMMAND_CHUNK_CLEAR)
                  .executes(ctx -> {
                      int count = chunkWatchers.size();
                      chunkWatchers.clear();
                      ctx.getSource().sendSuccess(() -> MekanismLang.COMMAND_CHUNK_CLEAR.translateColored(EnumColor.GRAY, EnumColor.INDIGO, count), true);
                      return Command.SINGLE_SUCCESS;
                  });
        }
    }

    private static class FlushCommand {

        static ArgumentBuilder<CommandSourceStack, ?> register() {
            return Commands.literal("flush")
                  .requires(MekanismPermissions.COMMAND_CHUNK_FLUSH)
                  .executes(ctx -> {
                      CommandSourceStack source = ctx.getSource();
                      ServerChunkCache sp = source.getLevel().getChunkSource();
                      int startCount = sp.getLoadedChunksCount();
                      //TODO: Check this
                      //sp.queueUnloadAll();
                      sp.tick(() -> true, false);
                      source.sendSuccess(() -> MekanismLang.COMMAND_CHUNK_FLUSH.translateColored(EnumColor.GRAY, EnumColor.INDIGO, startCount - sp.getLoadedChunksCount()), true);
                      return Command.SINGLE_SUCCESS;
                  });
        }
    }

    @SubscribeEvent
    public static void onChunkLoad(ChunkEvent.Load event) {
        handleChunkEvent(event, LOADED);
    }

    @SubscribeEvent
    public static void onChunkUnload(ChunkEvent.Unload event) {
        handleChunkEvent(event, UNLOADED);
    }

    @SubscribeEvent
    public static void onTicketLevelChange(ChunkTicketLevelUpdatedEvent event) {
        if (chunkWatchers.isEmpty() || event.getLevel().players().isEmpty()) {
            return;
        }
        ChunkWatchSettings settings = chunkWatchers.get(event.getChunkPos());
        if (settings != null) {
            Component message = settings.translateTicketLevel(event.getOldTicketLevel(), event.getNewTicketLevel());
            for (Player player : event.getLevel().players()) {
                player.sendSystemMessage(message);
            }
        }
    }

    private static void handleChunkEvent(ChunkEvent event, LangData direction) {
        LevelAccessor level = event.getLevel();
        if (level != null && !level.isClientSide()) {
            if (chunkWatchers.isEmpty() || level.players().isEmpty()) {
                return;
            }
            ChunkWatchSettings settings = chunkWatchers.get(event.getChunk().getPos().toLong());
            if (settings != null) {
                Component message = settings.translate(direction);
                for (Player player : level.players()) {
                    player.sendSystemMessage(message);
                }
            }
        }
    }

    private record LangData(ILangEntry unnamed, ILangEntry named) {
    }

    private record ChunkWatchSettings(Component name, Component position) {

        private ChunkWatchSettings(@Nullable String name, ChunkPos pos) {
            this(name == null ? CommonComponents.EMPTY : TextComponentUtil.getString(name), MekanismLang.GENERIC_WITH_COMMA.translate(pos.x, pos.z));
        }

        public Component translate(LangData langData) {
            if (name == CommonComponents.EMPTY) {
                return langData.unnamed().translateColored(EnumColor.GRAY, EnumColor.INDIGO, position);
            }
            return langData.named().translateColored(EnumColor.GRAY, EnumColor.INDIGO, name, EnumColor.INDIGO, position);
        }

        public Component translateTicketLevel(int oldLevel, int newLevel) {
            if (name == CommonComponents.EMPTY) {
                return MekanismLang.COMMAND_CHUNK_TICKET_LEVEL_CHANGED.translateColored(EnumColor.GRAY, EnumColor.INDIGO, position, EnumColor.INDIGO, oldLevel,
                      EnumColor.INDIGO, newLevel);
            }
            return MekanismLang.COMMAND_CHUNK_TICKET_LEVEL_CHANGED_NAMED.translateColored(EnumColor.GRAY, EnumColor.INDIGO, name, EnumColor.INDIGO, position,
                  EnumColor.INDIGO, oldLevel, EnumColor.INDIGO, newLevel);
        }
    }
}