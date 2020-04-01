package mekanism.common.command;


import java.util.Map;
import java.util.Stack;
import java.util.UUID;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import mekanism.api.Coord4D;
import mekanism.api.MekanismAPI;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.util.text.BooleanStateDisplay.OnOff;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.ILocationArgument;
import net.minecraft.command.arguments.Vec3Argument;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameRules;

public class CommandMek {

    private static Map<UUID, Stack<BlockPos>> tpStack = new Object2ObjectOpenHashMap<>();

    public static LiteralArgumentBuilder<CommandSource> register() {
        //TODO: Check permission levels for everything, also see if this is proper way to ensure only players use it and if so should all of the commands be like this
        // or should we allow some to be used from console
        return Commands.literal("mek")
              .requires(cs -> cs.getEntity() instanceof ServerPlayerEntity)
              .then(DebugCommand.register())
              .then(TestRulesCommand.register())
              .then(TpCommand.register())
              .then(TppopCommand.register())
              .then(ChunkCommand.register())
              .then(RadiationCommand.register());
    }

    private static class DebugCommand {

        static ArgumentBuilder<CommandSource, ?> register() {
            return Commands.literal("debug")
                  .requires(cs -> cs.hasPermissionLevel(4))
                  .executes(ctx -> {
                      MekanismAPI.debug = !MekanismAPI.debug;
                      ctx.getSource().sendFeedback(MekanismLang.COMMAND_DEBUG.translate(OnOff.of(MekanismAPI.debug)), true);
                      return 0;
                  });
        }
    }

    private static class TestRulesCommand {

        static ArgumentBuilder<CommandSource, ?> register() {
            return Commands.literal("testrules")
                  .requires(cs -> cs.hasPermissionLevel(4))
                  .executes(ctx -> {
                      CommandSource source = ctx.getSource();
                      MinecraftServer server = source.getServer();
                      GameRules rules = server.getGameRules();
                      rules.get(GameRules.DO_MOB_SPAWNING).set(false, server);
                      rules.get(GameRules.DO_DAYLIGHT_CYCLE).set(false, server);
                      rules.get(GameRules.DO_WEATHER_CYCLE).set(false, server);
                      source.asPlayer().getEntityWorld().setDayTime(2_000);
                      source.sendFeedback(MekanismLang.COMMAND_TEST_RULES.translate(), true);
                      return 0;
                  });
        }
    }

    private static class TpCommand {

        static ArgumentBuilder<CommandSource, ?> register() {
            return Commands.literal("tp")
                  .requires(cs -> cs.hasPermissionLevel(4))
                  .then(Commands.argument("location", Vec3Argument.vec3())
                        .executes(ctx -> {
                            CommandSource source = ctx.getSource();
                            Entity entity = source.getEntity();
                            // Save the current location on the stack
                            UUID player = entity.getUniqueID();
                            Stack<BlockPos> playerLocations = tpStack.getOrDefault(player, new Stack<>());
                            playerLocations.push(entity.getPosition());
                            tpStack.put(player, playerLocations);

                            ILocationArgument location = Vec3Argument.getLocation(ctx, "location");
                            Vec3d position = location.getPosition(source);
                            // Teleport user to new location
                            teleport(entity, position.getX(), position.getY(), position.getZ());
                            source.sendFeedback(MekanismLang.COMMAND_TP.translate(position.getX(), position.getY(), position.getZ()), true);
                            return 0;
                        }));
        }
    }

    private static class TppopCommand {

        static ArgumentBuilder<CommandSource, ?> register() {
            return Commands.literal("tpop")
                  .requires(cs -> cs.hasPermissionLevel(4))
                  .executes(ctx -> {
                      CommandSource source = ctx.getSource();
                      UUID player = source.getEntity().getUniqueID();

                      // Get stack of locations for the user; if there's at least one entry, pop it off
                      // and send the user back there
                      Stack<BlockPos> playerLocations = tpStack.getOrDefault(player, new Stack<>());
                      if (playerLocations.isEmpty()) {
                          source.sendFeedback(MekanismLang.COMMAND_TPOP_EMPTY.translate(), true);
                      } else {
                          BlockPos lastPos = playerLocations.pop();
                          tpStack.put(player, playerLocations);
                          teleport(source.getEntity(), lastPos.getX(), lastPos.getY(), lastPos.getZ());
                          source.sendFeedback(MekanismLang.COMMAND_TPOP.translate(lastPos.getX(), lastPos.getY(), lastPos.getZ(), playerLocations.size()), true);
                      }
                      return 0;
                  });
        }
    }

    private static class RadiationCommand {

        static ArgumentBuilder<CommandSource, ?> register() {
            return Commands.literal("radiation")
                .requires(cs -> cs.hasPermissionLevel(4))
                .then(Commands.argument("create", DoubleArgumentType.doubleArg(0, 10000))
                    .executes(ctx -> {
                        CommandSource source = ctx.getSource();
                        Coord4D location = new Coord4D(source.getPos().x, source.getPos().y, source.getPos().z, source.getWorld().getDimension().getType());
                        double magnitude = DoubleArgumentType.getDouble(ctx, "duration");
                        Mekanism.radiationManager.createSource(location, magnitude);
                        source.sendFeedback(MekanismLang.COMMAND_RADIATION_ADD.translate(location), true);
                        return 0;
                    }))
                .then(Commands.literal("removeAll")
                    .executes(ctx -> {
                        CommandSource source = ctx.getSource();
                        Mekanism.radiationManager.clearSources();
                        source.sendFeedback(MekanismLang.COMMAND_RADIATION_REMOVE_ALL.translate(), true);
                        return 0;
                    }));
        }
    }

    private static void teleport(Entity player, double x, double y, double z) {
        if (player instanceof ServerPlayerEntity) {
            ServerPlayerEntity mp = (ServerPlayerEntity) player;
            mp.connection.setPlayerLocation(x, y, z, mp.rotationYaw, mp.rotationPitch);
        } else {
            ClientPlayerEntity sp = (ClientPlayerEntity) player;
            sp.setLocationAndAngles(x, y, z, sp.rotationYaw, sp.rotationPitch);
        }
    }
}