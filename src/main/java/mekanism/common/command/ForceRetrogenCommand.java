package mekanism.common.command;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import mekanism.api.text.EnumColor;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.config.MekanismConfig;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.BlockPosArgument;
import net.minecraft.command.arguments.ColumnPosArgument;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.ColumnPos;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

public class ForceRetrogenCommand {

    private static final SimpleCommandExceptionType RETROGEN_NOT_ENABLED = new SimpleCommandExceptionType(MekanismLang.COMMAND_ERROR_RETROGEN_DISABLED.translate());
    private static final SimpleCommandExceptionType NO_CHUNKS_QUEUED = new SimpleCommandExceptionType(MekanismLang.COMMAND_ERROR_RETROGEN_FAILURE.translate());

    static ArgumentBuilder<CommandSource, ?> register() {
        return Commands.literal("retrogen")
              .requires(cs -> cs.hasPermissionLevel(2))
              .executes(ctx -> {
                  ColumnPos pos = new ColumnPos(new BlockPos(ctx.getSource().getPos()));
                  return addChunksToRegen(ctx.getSource(), pos, pos);
              }).then(Commands.argument("from", ColumnPosArgument.columnPos())
                    .executes(ctx -> {
                        ColumnPos from = ColumnPosArgument.fromBlockPos(ctx, "from");
                        return addChunksToRegen(ctx.getSource(), from, from);
                    }).then(Commands.argument("to", ColumnPosArgument.columnPos())
                          .executes(ctx -> addChunksToRegen(ctx.getSource(), ColumnPosArgument.fromBlockPos(ctx, "from"),
                                ColumnPosArgument.fromBlockPos(ctx, "to")))));
    }

    private static int addChunksToRegen(CommandSource source, ColumnPos start, ColumnPos end) throws CommandSyntaxException {
        if (!MekanismConfig.world.enableRegeneration.get()) {
            throw RETROGEN_NOT_ENABLED.create();
        }
        int xStart = Math.min(start.x, end.x);
        int xEnd = Math.max(start.x, end.x);
        int zStart = Math.min(start.z, end.z);
        int zEnd = Math.max(start.z, end.z);
        //TODO: Switch this to something like !World.isValidXZPosition (issue is it is private)
        if (xStart < -30000000 || zStart < -30000000 || xEnd >= 30000000 || zEnd >= 30000000) {
            throw BlockPosArgument.POS_OUT_OF_WORLD.create();
        }
        int chunkXStart = xStart >> 4;
        int chunkXEnd = xEnd >> 4;
        int chunkZStart = zStart >> 4;
        int chunkZEnd = zEnd >> 4;
        ServerWorld world = source.getWorld();
        RegistryKey<World> registryKey = world.getDimensionKey();
        boolean hasChunks = false;
        for (int chunkX = chunkXStart; chunkX <= chunkXEnd; chunkX++) {
            for (int chunkZ = chunkZStart; chunkZ <= chunkZEnd; chunkZ++) {
                if (world.chunkExists(chunkX, chunkZ)) {
                    Mekanism.worldTickHandler.addRegenChunk(registryKey, new ChunkPos(chunkX, chunkZ));
                    source.sendFeedback(MekanismLang.COMMAND_RETROGEN_CHUNK_QUEUED.translateColored(EnumColor.GRAY, EnumColor.INDIGO,
                          MekanismLang.GENERIC_WITH_COMMA.translate(chunkX, chunkZ), EnumColor.INDIGO, registryKey.getLocation()), true);
                    hasChunks = true;
                }
            }
        }
        if (!hasChunks) {
            throw NO_CHUNKS_QUEUED.create();
        }
        return 0;
    }
}