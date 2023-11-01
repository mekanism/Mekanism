package mekanism.common.base;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;
import mekanism.common.Mekanism;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.server.permission.PermissionAPI;
import net.neoforged.neoforge.server.permission.events.PermissionGatherEvent;
import net.neoforged.neoforge.server.permission.nodes.PermissionDynamicContext;
import net.neoforged.neoforge.server.permission.nodes.PermissionDynamicContextKey;
import net.neoforged.neoforge.server.permission.nodes.PermissionNode;
import net.neoforged.neoforge.server.permission.nodes.PermissionNode.PermissionResolver;
import net.neoforged.neoforge.server.permission.nodes.PermissionType;
import net.neoforged.neoforge.server.permission.nodes.PermissionTypes;
import org.jetbrains.annotations.Nullable;

public class MekanismPermissions {

    private static final List<PermissionNode<?>> NODES_TO_REGISTER = new ArrayList<>();
    private static final PermissionResolver<Boolean> PLAYER_IS_OP = (player, uuid, context) -> player != null && player.hasPermissions(Commands.LEVEL_GAMEMASTERS);
    private static final PermissionResolver<Boolean> ALWAYS_TRUE = (player, uuid, context) -> true;

    public static final PermissionNode<Boolean> BYPASS_SECURITY = node("bypass_security", PermissionTypes.BOOLEAN,
          (player, uuid, context) -> player != null && player.server.getPlayerList().isOp(player.getGameProfile()));

    //Commands
    public static final CommandPermissionNode COMMAND = new CommandPermissionNode(node("command", PermissionTypes.BOOLEAN,
          (player, uuid, contexts) -> player != null && player.hasPermissions(Commands.LEVEL_ALL)), Commands.LEVEL_ALL);

    public static final CommandPermissionNode COMMAND_BUILD = nodeOpCommand("build");
    public static final CommandPermissionNode COMMAND_BUILD_REMOVE = nodeSubCommand(COMMAND_BUILD, "remove");

    public static final CommandPermissionNode COMMAND_CHUNK = nodeOpCommand("chunk");
    public static final CommandPermissionNode COMMAND_CHUNK_CLEAR = nodeSubCommand(COMMAND_CHUNK, "clear");
    public static final CommandPermissionNode COMMAND_CHUNK_FLUSH = nodeSubCommand(COMMAND_CHUNK, "flush");
    public static final CommandPermissionNode COMMAND_CHUNK_UNWATCH = nodeSubCommand(COMMAND_CHUNK, "unwatch");
    public static final CommandPermissionNode COMMAND_CHUNK_WATCH = nodeSubCommand(COMMAND_CHUNK, "watch");

    public static final CommandPermissionNode COMMAND_DEBUG = nodeOpCommand("debug");
    public static final CommandPermissionNode COMMAND_FORCE_RETROGEN = nodeOpCommand("force_retrogen");

    public static final CommandPermissionNode COMMAND_RADIATION = nodeOpCommand("radiation");
    public static final CommandPermissionNode COMMAND_RADIATION_ADD = nodeSubCommand(COMMAND_RADIATION, "add");
    public static final CommandPermissionNode COMMAND_RADIATION_ADD_ENTITY = nodeSubCommand(COMMAND_RADIATION, "add_entity");
    public static final CommandPermissionNode COMMAND_RADIATION_ADD_ENTITY_OTHERS = nodeSubCommand(COMMAND_RADIATION_ADD_ENTITY, "others");
    public static final CommandPermissionNode COMMAND_RADIATION_GET = nodeSubCommand(COMMAND_RADIATION, "get");
    public static final CommandPermissionNode COMMAND_RADIATION_HEAL = nodeSubCommand(COMMAND_RADIATION, "heal");
    public static final CommandPermissionNode COMMAND_RADIATION_HEAL_OTHERS = nodeSubCommand(COMMAND_RADIATION_HEAL, "others");
    public static final CommandPermissionNode COMMAND_RADIATION_REDUCE = nodeSubCommand(COMMAND_RADIATION, "reduce");
    public static final CommandPermissionNode COMMAND_RADIATION_REDUCE_OTHERS = nodeSubCommand(COMMAND_RADIATION_REDUCE, "others");
    public static final CommandPermissionNode COMMAND_RADIATION_REMOVE_ALL = nodeSubCommand(COMMAND_RADIATION, "remove.all");

    public static final CommandPermissionNode COMMAND_TEST_RULES = nodeOpCommand("test_rules");
    public static final CommandPermissionNode COMMAND_TP = nodeOpCommand("tp");
    public static final CommandPermissionNode COMMAND_TP_POP = nodeOpCommand("tp_pop");

    private static CommandPermissionNode nodeOpCommand(String nodeName) {
        PermissionNode<Boolean> node = node("command." + nodeName, PermissionTypes.BOOLEAN, PLAYER_IS_OP);
        return new CommandPermissionNode(node, Commands.LEVEL_GAMEMASTERS);
    }

    private static CommandPermissionNode nodeSubCommand(CommandPermissionNode parent, String nodeName) {
        //Because sub commands can assume that the parent was checked before getting to them, we can have a default resolver of always true
        // The main benefit for them to have their own node is just in case someone wants to do more restricting
        PermissionNode<Boolean> node = subNode(parent.node, nodeName, ALWAYS_TRUE);
        return new CommandPermissionNode(node, parent.fallbackLevel);
    }

    /**
     * @apiNote For use in sub nodes that don't know if there parent has been checked yet.
     */
    private static <T> PermissionNode<T> subNode(PermissionNode<T> parent, String nodeName) {
        return subNode(parent, nodeName, (player, uuid, context) -> getPermission(player, uuid, parent, context));
    }

    private static <T> PermissionNode<T> subNode(PermissionNode<T> parent, String nodeName, ResultTransformer<T> defaultRestrictionIncrease) {
        return subNode(parent, nodeName, (player, uuid, context) -> {
            T result = getPermission(player, uuid, parent, context);
            return defaultRestrictionIncrease.transform(player, uuid, result, context);
        });
    }

    private static <T> PermissionNode<T> subNode(PermissionNode<T> parent, String nodeName, PermissionResolver<T> defaultResolver) {
        String fullParentName = parent.getNodeName();
        //Strip the modid from the parent's node name
        String parentName = fullParentName.substring(fullParentName.indexOf('.') + 1);
        return node(parentName + "." + nodeName, parent.getType(), defaultResolver);
    }

    @SafeVarargs
    private static <T> PermissionNode<T> node(String nodeName, PermissionType<T> type, PermissionResolver<T> defaultResolver, PermissionDynamicContextKey<T>... dynamics) {
        PermissionNode<T> node = new PermissionNode<>(Mekanism.MODID, nodeName, type, defaultResolver, dynamics);
        NODES_TO_REGISTER.add(node);
        return node;
    }

    public static void registerPermissionNodes(PermissionGatherEvent.Nodes event) {
        event.addNodes(NODES_TO_REGISTER);
    }

    private static <T> T getPermission(@Nullable ServerPlayer player, UUID playerUUID, PermissionNode<T> node, PermissionDynamicContext<?>... context) {
        if (player == null) {
            return PermissionAPI.getOfflinePermission(playerUUID, node, context);
        }
        return PermissionAPI.getPermission(player, node, context);
    }

    public record CommandPermissionNode(PermissionNode<Boolean> node, int fallbackLevel) implements Predicate<CommandSourceStack> {

        @Override
        public boolean test(CommandSourceStack source) {
            //See https://github.com/MinecraftForge/MinecraftForge/commit/f7eea35cb9b043aae0a3866a9578724aa7560585 for details on why
            // has permission is checked first and the implications
            return source.hasPermission(fallbackLevel) || source.source instanceof ServerPlayer player && PermissionAPI.getPermission(player, node);
        }
    }

    @FunctionalInterface
    private interface ResultTransformer<T> {

        T transform(@Nullable ServerPlayer player, UUID playerUUID, T resolved, PermissionDynamicContext<?>... context);
    }
}