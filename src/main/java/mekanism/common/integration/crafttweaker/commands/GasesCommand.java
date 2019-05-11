package mekanism.common.integration.crafttweaker.commands;

import crafttweaker.CraftTweakerAPI;
import crafttweaker.mc1120.commands.CraftTweakerCommand;
import crafttweaker.mc1120.commands.SpecialMessagesChat;
import java.util.List;
import mekanism.api.gas.Gas;
import mekanism.api.gas.GasRegistry;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextFormatting;

public class GasesCommand extends CraftTweakerCommand {

    public GasesCommand() {
        super("gases");
    }

    @Override
    protected void init() {
        setDescription(SpecialMessagesChat.getClickableCommandText(TextFormatting.DARK_GREEN + "/ct " + subCommandName,
              "/ct " + subCommandName, true), SpecialMessagesChat.getNormalMessage(TextFormatting.DARK_AQUA +
                                                                                   "Outputs a list of all registered gases to the crafttweaker.log"));
    }

    @Override
    public void executeCommand(MinecraftServer server, ICommandSender sender, String[] args) {
        CraftTweakerAPI.logCommand("Gases:");
        List<Gas> gases = GasRegistry.getRegisteredGasses();
        gases.forEach(
              gas -> CraftTweakerAPI.logCommand(String.format("<gas:%s>, %s", gas.getName(), gas.getLocalizedName())));
        sender.sendMessage(SpecialMessagesChat.getLinkToCraftTweakerLog(
              "List of " + gases.size() + " gases generated;", sender));
    }
}