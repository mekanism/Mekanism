package mekanism.common.command;


import java.util.HashMap;
import java.util.Map;
import java.util.Stack;
import java.util.UUID;
import mekanism.api.MekanismAPI;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.CommandGameRule;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameRules;
import net.minecraftforge.fml.common.event.FMLServerStartedEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.server.command.CommandTreeBase;

public class CommandMek extends CommandTreeBase {

    private Map<UUID, Stack<BlockPos>> tpStack = new HashMap<>();

    public CommandMek() {
        addSubcommand(new Cmd("debug", "cmd.mek.debug", this::toggleDebug));
        addSubcommand(new Cmd("testrules", "cmd.mek.testrules", this::setupTestRules));
        addSubcommand(new Cmd("tp", "cmd.mek.tp", this::teleportPush));
        addSubcommand(new Cmd("tpop", "cmd.mek.tpop", this::teleportPop));
        addSubcommand(new CommandChunk());
    }

    public static void register(FMLServerStartingEvent event) {
        CommandMek cmd = new CommandMek();
        event.registerServerCommand(cmd);
        event.registerServerCommand(new Cmd("mtp", "cmd.mek.tp", cmd::teleportPush));
        event.registerServerCommand(new Cmd("mtpop", "cmd.mek.tpop", cmd::teleportPop));
    }

    @Override
    public String getName() {
        return "mek";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "cmd.mek.usage";
    }


    public void toggleDebug(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        MekanismAPI.debug = !MekanismAPI.debug;
        CommandBase.notifyCommandListener(sender, this, "cmd.mek.debug", MekanismAPI.debug);
    }

    public void setupTestRules(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        GameRules rules = server.getEntityWorld().getGameRules();
        rules.setOrCreateGameRule("doMobSpawning", "false");
        rules.setOrCreateGameRule("doDaylightCycle", "false");
        rules.setOrCreateGameRule("doWeatherCycle", "false");
        server.getEntityWorld().setWorldTime(2000);
        CommandGameRule.notifyGameRuleChange(rules, "", server);
        CommandBase.notifyCommandListener(sender, this, "cmd.mek.testrules");
    }

    public void teleportPush(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if (args.length < 3) {
            notifyCommandListener(sender, this, "cmd.mek.tp.missing.args");
            return;
        }

        // Parse the target arguments from command line
        CoordinateArg xArg = parseCoordinate(10, args[0], true);
        CoordinateArg yArg = parseCoordinate(10, args[1], true);
        CoordinateArg zArg = parseCoordinate(10, args[2], true);

        // Save the current location on the stack
        UUID player = sender.getCommandSenderEntity().getUniqueID();
        Stack<BlockPos> playerLocations = tpStack.getOrDefault(player, new Stack<BlockPos>());
        playerLocations.push(sender.getPosition());
        tpStack.put(player, playerLocations);

        // Teleport user to new location
        teleport(sender.getCommandSenderEntity(), xArg.getResult(), yArg.getResult(), zArg.getResult());
        notifyCommandListener(sender, this, "cmd.mek.tp", args[0], args[1], args[2]);
    }

    public void teleportPop(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        UUID player = sender.getCommandSenderEntity().getUniqueID();

        // Get stack of locations for the user; if there's at least one entry, pop it off
        // and send the user back there
        Stack<BlockPos> playerLocations = tpStack.getOrDefault(player, new Stack<BlockPos>());
        if (!playerLocations.isEmpty()) {
            BlockPos lastPos = playerLocations.pop();
            tpStack.put(player, playerLocations);
            teleport(sender.getCommandSenderEntity(), lastPos.getX(), lastPos.getY(), lastPos.getZ());
            notifyCommandListener(sender, this, "cmd.mek.tpop",
                  lastPos.getX(), lastPos.getY(), lastPos.getZ(), playerLocations.size());
        } else {
            notifyCommandListener(sender, this, "cmd.mek.tpop.empty.stack");
        }
    }

    private void teleport(Entity player, double x, double y, double z) {
        if (player instanceof EntityPlayerMP) {
            EntityPlayerMP mp = (EntityPlayerMP)player;
            mp.connection.setPlayerLocation(x, y, z, mp.rotationYaw, mp.rotationPitch);
        } else {
            EntityPlayerSP sp = (EntityPlayerSP)player;
            sp.setLocationAndAngles(x, y, z, sp.rotationYaw, sp.rotationPitch);
        }
    }

    // Wrapper class that makes it easier to create single method commands
    public static class Cmd extends CommandBase {

        private String name;
        private String usage;
        private CmdExecute ex;

        Cmd(String name, String usage, CmdExecute ex) {
            this.name = name;
            this.usage = usage;
            this.ex = ex;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public String getUsage(ICommandSender sender) {
            return usage + ".usage";
        }

        @Override
        public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
            ex.execute(server, sender, args);
        }
    }

    interface CmdExecute {
        void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException;
    }
}
