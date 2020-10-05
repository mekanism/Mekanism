package mekanism.common.command;


import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.Map;
import java.util.Stack;
import java.util.UUID;
import mekanism.api.Coord4D;
import mekanism.api.MekanismAPI;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.command.builders.BuildCommand;
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
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.GameRules;
import net.minecraft.world.server.ServerWorld;

public class CommandMek {

    private CommandMek() {
    }

    private static final Map<UUID, Stack<BlockPos>> tpStack = new Object2ObjectOpenHashMap<>();

    public static LiteralArgumentBuilder<CommandSource> register() {
        return Commands.literal("mek")
              .requires(cs -> cs.getEntity() instanceof ServerPlayerEntity)
              .then(DebugCommand.register())
              .then(TestRulesCommand.register())
              .then(TpCommand.register())
              .then(TppopCommand.register())
              .then(ChunkCommand.register())
              .then(BuildCommand.COMMAND)
              .then(RadiationCommand.register());
    }

    private static class DebugCommand {

        static ArgumentBuilder<CommandSource, ?> register() {
            return Commands.literal("debug")
                  .requires(cs -> cs.hasPermissionLevel(2))
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
                      ((ServerWorld) source.asPlayer().getEntityWorld()).func_241114_a_(2_000);
                      source.sendFeedback(MekanismLang.COMMAND_TEST_RULES.translate(), true);
                      return 0;
                  });
        }
    }

    private static class TpCommand {

        static ArgumentBuilder<CommandSource, ?> register() {
            return Commands.literal("tp")
                  .requires(cs -> cs.hasPermissionLevel(2))
                  .then(Commands.argument("location", Vec3Argument.vec3())
                        .executes(ctx -> {
                            CommandSource source = ctx.getSource();
                            Entity entity = source.getEntity();
                            // Save the current location on the stack
                            if (entity != null) {
                                UUID player = entity.getUniqueID();
                                Stack<BlockPos> playerLocations = tpStack.getOrDefault(player, new Stack<>());
                                playerLocations.push(entity.getPosition());
                                tpStack.put(player, playerLocations);

                                ILocationArgument location = Vec3Argument.getLocation(ctx, "location");
                                Vector3d position = location.getPosition(source);
                                // Teleport user to new location
                                teleport(entity, position.getX(), position.getY(), position.getZ());
                                source.sendFeedback(MekanismLang.COMMAND_TP.translate(position.getX(), position.getY(), position.getZ()), true);
                            }
                            return 0;
                        }));
        }
    }

    private static class TppopCommand {

        static ArgumentBuilder<CommandSource, ?> register() {
            return Commands.literal("tpop")
                  .requires(cs -> cs.hasPermissionLevel(2))
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
                  .requires(cs -> cs.hasPermissionLevel(2))
                  .then(Commands.literal("add").then(Commands.argument("magnitude", DoubleArgumentType.doubleArg(0, 10_000))
                        .executes(ctx -> {
                            try {
                                CommandSource source = ctx.getSource();
                                Coord4D location = new Coord4D(source.getPos().x, source.getPos().y, source.getPos().z, source.getWorld().getDimensionKey());
                                double magnitude = DoubleArgumentType.getDouble(ctx, "magnitude");
                                Mekanism.radiationManager.radiate(location, magnitude);
                                source.sendFeedback(MekanismLang.COMMAND_RADIATION_ADD.translate(location), true);
                            } catch (Exception e) {
                                Mekanism.logger.error("Failed to radiate", e);
                            }
                            return 0;
                        })))
                  .then(Commands.literal("get")
                        .executes(ctx -> {
                            CommandSource source = ctx.getSource();
                            Coord4D location = new Coord4D(source.getPos().x, source.getPos().y, source.getPos().z, source.getWorld().getDimensionKey());
                            double radiation = Mekanism.radiationManager.getRadiationLevel(location);
                            source.sendFeedback(MekanismLang.COMMAND_RADIATION_GET.translate(radiation), true);
                            return 0;
                        }))
                  .then(Commands.literal("heal")
                        .executes(ctx -> {
                            if (ctx.getSource().getEntity() instanceof ServerPlayerEntity) {
                                ServerPlayerEntity player = (ServerPlayerEntity) ctx.getSource().getEntity();
                                player.getCapability(Capabilities.RADIATION_ENTITY_CAPABILITY).ifPresent(c -> c.set(0));
                                ctx.getSource().sendFeedback(MekanismLang.COMMAND_RADIATION_CLEAR.translate(), true);
                            }
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