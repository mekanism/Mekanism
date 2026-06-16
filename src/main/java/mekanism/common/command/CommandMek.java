package mekanism.common.command;


import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.UUID;
import mekanism.api.MekanismAPI;
import mekanism.api.text.EnumColor;
import mekanism.common.MekanismLang;
import mekanism.common.base.MekanismPermissions;
import mekanism.common.command.builders.BuildCommand;
import mekanism.common.util.text.BooleanStateDisplay.OnOff;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.Coordinates;
import net.minecraft.commands.arguments.coordinates.Vec3Argument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.clock.ClockTimeMarkers;
import net.minecraft.world.clock.WorldClocks;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.phys.Vec3;

public class CommandMek {

    private CommandMek() {
    }

    private static final Map<UUID, Deque<BlockPos>> tpStack = new Object2ObjectOpenHashMap<>();

    public static LiteralArgumentBuilder<CommandSourceStack> register() {
        return Commands.literal("mek")
              .requires(MekanismPermissions.COMMAND)
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

        static ArgumentBuilder<CommandSourceStack, ?> register() {
            return Commands.literal("debug")
                  .requires(MekanismPermissions.COMMAND_DEBUG)
                  .executes(ctx -> {
                      MekanismAPI.debug = !MekanismAPI.debug;
                      ctx.getSource().sendSuccess(() -> MekanismLang.COMMAND_DEBUG.translateColored(EnumColor.GRAY, OnOff.of(MekanismAPI.debug, true)), true);
                      return Command.SINGLE_SUCCESS;
                  });
        }
    }

    private static class TestRulesCommand {

        static ArgumentBuilder<CommandSourceStack, ?> register() {
            return Commands.literal("testrules")
                  .requires(MekanismPermissions.COMMAND_TEST_RULES)
                  .executes(ctx -> {
                      CommandSourceStack source = ctx.getSource();
                      MinecraftServer server = source.getServer();
                      GameRules rules = source.getLevel().getGameRules();
                      rules.set(GameRules.KEEP_INVENTORY, true, server);
                      rules.set(GameRules.SPAWN_MOBS, false, server);
                      rules.set(GameRules.SPAWN_MONSTERS, false, server);
                      rules.set(GameRules.SPAWN_PATROLS, false, server);
                      rules.set(GameRules.SPAWN_PHANTOMS, false, server);
                      rules.set(GameRules.SPAWN_WANDERING_TRADERS, false, server);
                      rules.set(GameRules.ADVANCE_TIME, false, server);
                      rules.set(GameRules.ADVANCE_WEATHER, false, server);
                      rules.set(GameRules.MOB_GRIEFING, false, server);
                      //TODO - 26.2: check this is correct
                      source.getLevel().clockManager().moveToTimeMarker(server.registryAccess().holderOrThrow(WorldClocks.OVERWORLD), ClockTimeMarkers.NOON);
                      //Act as if /weather clear was ran
                      source.getLevel().getWeatherData().setRaining(false);
                      source.getLevel().getWeatherData().setThundering(false);
                      source.sendSuccess(() -> MekanismLang.COMMAND_TEST_RULES.translateColored(EnumColor.GRAY), true);
                      return Command.SINGLE_SUCCESS;
                  });
        }
    }

    private static class TpCommand {

        static ArgumentBuilder<CommandSourceStack, ?> register() {
            return Commands.literal("tp")
                  .requires(MekanismPermissions.COMMAND_TP.and(cs -> cs.getEntity() instanceof ServerPlayer))
                  .then(Commands.argument("location", Vec3Argument.vec3())
                        .executes(ctx -> {
                            CommandSourceStack source = ctx.getSource();
                            ServerPlayer player = source.getPlayerOrException();
                            // Save the current location on the stack
                            UUID uuid = player.getUUID();
                            Deque<BlockPos> playerLocations = tpStack.computeIfAbsent(uuid, u -> new ArrayDeque<>());
                            playerLocations.push(player.blockPosition());

                            Coordinates location = Vec3Argument.getCoordinates(ctx, "location");
                            Vec3 position = location.getPosition(source);
                            // Teleport user to new location
                            player.connection.teleport(position.x(), position.y(), position.z(), player.getYRot(), player.getXRot());
                            source.sendSuccess(() -> MekanismLang.COMMAND_TP.translateColored(EnumColor.GRAY, EnumColor.INDIGO, getPosition(position)), true);
                            return Command.SINGLE_SUCCESS;
                        })
                  );
        }

        private static Component getPosition(Vec3 pos) {
            return MekanismLang.GENERIC_BLOCK_POS.translate(pos.x(), pos.y(), pos.z());
        }
    }

    private static class TppopCommand {

        private static final SimpleCommandExceptionType TPOP_EMPTY = new SimpleCommandExceptionType(MekanismLang.COMMAND_ERROR_TPOP_EMPTY.translate());

        static ArgumentBuilder<CommandSourceStack, ?> register() {
            return Commands.literal("tpop")
                  .requires(MekanismPermissions.COMMAND_TP_POP.and(cs -> cs.getEntity() instanceof ServerPlayer))
                  .executes(ctx -> {
                      CommandSourceStack source = ctx.getSource();
                      ServerPlayer player = source.getPlayerOrException();
                      UUID uuid = player.getUUID();

                      // Get stack of locations for the user; if there's at least one entry, pop it off
                      // and send the user back there
                      Deque<BlockPos> playerLocations = tpStack.computeIfAbsent(uuid, u -> new ArrayDeque<>());
                      if (playerLocations.isEmpty()) {
                          throw TPOP_EMPTY.create();
                      }
                      BlockPos lastPos = playerLocations.pop();
                      player.connection.teleport(lastPos.getX(), lastPos.getY(), lastPos.getZ(), player.getYRot(), player.getXRot());
                      source.sendSuccess(() -> MekanismLang.COMMAND_TPOP.translateColored(EnumColor.GRAY, EnumColor.INDIGO, getPosition(lastPos), EnumColor.INDIGO, playerLocations.size()), true);
                      return Command.SINGLE_SUCCESS;
                  });
        }

        private static Component getPosition(BlockPos pos) {
            return MekanismLang.GENERIC_BLOCK_POS.translate(pos.getX(), pos.getY(), pos.getZ());
        }
    }
}