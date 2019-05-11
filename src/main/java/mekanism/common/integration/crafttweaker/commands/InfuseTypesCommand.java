package mekanism.common.integration.crafttweaker.commands;

import crafttweaker.CraftTweakerAPI;
import crafttweaker.mc1120.commands.CraftTweakerCommand;
import crafttweaker.mc1120.commands.SpecialMessagesChat;
import java.util.Set;
import mekanism.api.infuse.InfuseRegistry;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextFormatting;

public class InfuseTypesCommand extends CraftTweakerCommand {

    public InfuseTypesCommand() {
        super("infuseTypes");
    }

    @Override
    protected void init() {
        setDescription(SpecialMessagesChat.getClickableCommandText(TextFormatting.DARK_GREEN + "/ct " + subCommandName,
              "/ct " + subCommandName, true), SpecialMessagesChat.getNormalMessage(TextFormatting.DARK_AQUA +
                                                                                   "Outputs a list of all registered metallurgic infuser infusion types to the crafttweaker.log"));
    }

    @Override
    public void executeCommand(MinecraftServer server, ICommandSender sender, String[] args) {
        CraftTweakerAPI.logCommand("Infuse Types:");
        Set<String> names = InfuseRegistry.getInfuseMap().keySet();
        names.forEach(CraftTweakerAPI::logCommand);
        sender.sendMessage(SpecialMessagesChat.getLinkToCraftTweakerLog("List of " + names.size() +
                                                                        " metallurgic infuser infusion types generated;", sender));
    }
}