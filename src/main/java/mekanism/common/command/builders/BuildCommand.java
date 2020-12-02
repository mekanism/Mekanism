package mekanism.common.command.builders;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.text.EnumColor;
import mekanism.api.text.ILangEntry;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.util.EnumUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.WorldUtils;
import net.minecraft.block.BlockState;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunk;

public class BuildCommand {

    private static final SimpleCommandExceptionType MISS = new SimpleCommandExceptionType(MekanismLang.COMMAND_ERROR_BUILD_MISS.translate());

    private BuildCommand() {
    }

    public static final ArgumentBuilder<CommandSource, ?> COMMAND =
          Commands.literal("build")
                .requires(cs -> cs.hasPermissionLevel(2) && cs.getEntity() instanceof ServerPlayerEntity)
                .then(Commands.literal("remove")
                      .executes(ctx -> {
                          CommandSource source = ctx.getSource();
                          BlockRayTraceResult result = MekanismUtils.rayTrace(source.asPlayer(), 100);
                          if (result.getType() == RayTraceResult.Type.MISS) {
                              throw MISS.create();
                          }
                          destroy(source.getWorld(), result.getPos());
                          source.sendFeedback(MekanismLang.COMMAND_BUILD_REMOVED.translateColored(EnumColor.GRAY), true);
                          return 0;
                      }));

    public static void register(String name, ILangEntry localizedName, StructureBuilder builder) {
        COMMAND.then(Commands.literal(name)
              .executes(ctx -> {
                  CommandSource source = ctx.getSource();
                  BlockRayTraceResult result = MekanismUtils.rayTrace(source.asPlayer(), 100);
                  if (result.getType() == RayTraceResult.Type.MISS) {
                      throw MISS.create();
                  }
                  BlockPos pos = result.getPos().offset(Direction.UP);
                  builder.build(source.getWorld(), pos);
                  source.sendFeedback(MekanismLang.COMMAND_BUILD_BUILT.translateColored(EnumColor.GRAY, EnumColor.INDIGO, localizedName), true);
                  return 0;
              }));
    }

    private static void destroy(World world, BlockPos pos) throws CommandSyntaxException {
        Long2ObjectMap<IChunk> chunkMap = new Long2ObjectOpenHashMap<>();
        if (!isMekanismBlock(world, chunkMap, pos)) {
            //If we didn't hit a mekanism block throw an error that we missed
            throw MISS.create();
        }
        Set<BlockPos> traversed = new HashSet<>();
        Queue<BlockPos> openSet = new ArrayDeque<>();
        openSet.add(pos);
        traversed.add(pos);
        while (!openSet.isEmpty()) {
            BlockPos ptr = openSet.poll();
            if (isMekanismBlock(world, chunkMap, ptr)) {
                world.removeBlock(ptr, false);
                for (Direction side : EnumUtils.DIRECTIONS) {
                    BlockPos offset = ptr.offset(side);
                    if (traversed.add(offset)) {
                        openSet.add(offset);
                    }
                }
            }
        }
    }

    private static boolean isMekanismBlock(@Nullable IWorld world, @Nonnull Long2ObjectMap<IChunk> chunkMap, @Nonnull BlockPos pos) {
        Optional<BlockState> state = WorldUtils.getBlockState(world, chunkMap, pos);
        return state.isPresent() && state.get().getBlock().getRegistryName().getNamespace().startsWith(Mekanism.MODID);
    }
}
