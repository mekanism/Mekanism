package mekanism.common.command;


import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.Map;
import java.util.Stack;
import java.util.UUID;
import mekanism.api.MekanismAPI;
import mekanism.api.text.EnumColor;
import mekanism.common.MekanismLang;
import mekanism.common.command.builders.BuildCommand;
import mekanism.common.util.text.BooleanStateDisplay.OnOff;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.ILocationArgument;
import net.minecraft.command.arguments.Vec3Argument;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.GameRules;

public class CommandMek {

    private CommandMek() {
    }

    private static final Map<UUID, Stack<BlockPos>> tpStack = new Object2ObjectOpenHashMap<>();

    public static LiteralArgumentBuilder<CommandSource> register() {
        return Commands.literal("mek")
              .then(BuildCommand.COMMAND)
              .then(ChunkCommand.register())
              .then(DebugCommand.register())
              .then(ForceRetrogenCommand.register())
              .then(RadiationCommand.register())
              .then(TestRulesCommand.register())
              .then(TpCommand.register())
              .then(TppopCommand.register());
    }

    private static class DebugCommand {

        static ArgumentBuilder<CommandSource, ?> register() {
            return Commands.literal("debug")
                  .requires(cs -> cs.hasPermissionLevel(2))
                  .executes(ctx -> {
                      MekanismAPI.debug = !MekanismAPI.debug;
                      ctx.getSource().sendFeedback(MekanismLang.COMMAND_DEBUG.translateColored(EnumColor.GRAY, OnOff.of(MekanismAPI.debug, true)), true);
                      return 0;
                  });
        }
    }

    private static class TestRulesCommand {

        static ArgumentBuilder<CommandSource, ?> register() {
            return Commands.literal("testrules")
                  .requires(cs -> cs.hasPermissionLevel(2))
                  .executes(ctx -> {
                      CommandSource source = ctx.getSource();
                      MinecraftServer server = source.getServer();
                      GameRules rules = server.getGameRules();
                      rules.get(GameRules.KEEP_INVENTORY).set(true, server);
                      rules.get(GameRules.DO_MOB_SPAWNING).set(false, server);
                      rules.get(GameRules.DO_DAYLIGHT_CYCLE).set(false, server);
                      rules.get(GameRules.DO_WEATHER_CYCLE).set(false, server);
                      rules.get(GameRules.MOB_GRIEFING).set(false, server);
                      source.getWorld().setDayTime(2_000);
                      source.sendFeedback(MekanismLang.COMMAND_TEST_RULES.translateColored(EnumColor.GRAY), true);
                      return 0;
                  });
        }
    }

    private static class TpCommand {

        static ArgumentBuilder<CommandSource, ?> register() {
            return Commands.literal("tp")
                  .requires(cs -> cs.hasPermissionLevel(2) && cs.getEntity() instanceof ServerPlayerEntity)
                  .then(Commands.argument("location", Vec3Argument.vec3())
                        .executes(ctx -> {
                            CommandSource source = ctx.getSource();
                            ServerPlayerEntity player = source.asPlayer();
                            // Save the current location on the stack
                            UUID uuid = player.getUniqueID();
                            Stack<BlockPos> playerLocations = tpStack.getOrDefault(uuid, new Stack<>());
                            playerLocations.push(player.getPosition());
                            tpStack.put(uuid, playerLocations);

                            ILocationArgument location = Vec3Argument.getLocation(ctx, "location");
                            Vector3d position = location.getPosition(source);
                            // Teleport user to new location
                            player.connection.setPlayerLocation(position.getX(), position.getY(), position.getZ(), player.rotationYaw, player.rotationPitch);
                            source.sendFeedback(MekanismLang.COMMAND_TP.translateColored(EnumColor.GRAY, EnumColor.INDIGO, getPosition(position)), true);
                            return 0;
                        })
                  );
        }

        private static ITextComponent getPosition(Vector3d pos) {
            return MekanismLang.GENERIC_BLOCK_POS.translate(pos.getX(), pos.getY(), pos.getZ());
        }
    }

    private static class TppopCommand {

        private static final SimpleCommandExceptionType TPOP_EMPTY = new SimpleCommandExceptionType(MekanismLang.COMMAND_ERROR_TPOP_EMPTY.translate());

        static ArgumentBuilder<CommandSource, ?> register() {
            return Commands.literal("tpop")
                  .requires(cs -> cs.hasPermissionLevel(2) && cs.getEntity() instanceof ServerPlayerEntity)
                  .executes(ctx -> {
                      CommandSource source = ctx.getSource();
                      ServerPlayerEntity player = source.asPlayer();
                      UUID uuid = player.getUniqueID();

                      // Get stack of locations for the user; if there's at least one entry, pop it off
                      // and send the user back there
                      Stack<BlockPos> playerLocations = tpStack.getOrDefault(uuid, new Stack<>());
                      if (playerLocations.isEmpty()) {
                          throw TPOP_EMPTY.create();
                      }
                      BlockPos lastPos = playerLocations.pop();
                      tpStack.put(uuid, playerLocations);
                      player.connection.setPlayerLocation(lastPos.getX(), lastPos.getY(), lastPos.getZ(), player.rotationYaw, player.rotationPitch);
                      source.sendFeedback(MekanismLang.COMMAND_TPOP.translateColored(EnumColor.GRAY, EnumColor.INDIGO, getPosition(lastPos), EnumColor.INDIGO, playerLocations.size()), true);
                      return 0;
                  });
        }

        private static ITextComponent getPosition(BlockPos pos) {
            return MekanismLang.GENERIC_BLOCK_POS.translate(pos.getX(), pos.getY(), pos.getZ());
        }
    }
}