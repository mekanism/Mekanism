package mekanism.common;

import java.util.Arrays;
import java.util.List;
import javax.annotation.Nonnull;
import mekanism.api.EnumColor;
import mekanism.api.MekanismAPI;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.fml.common.FMLCommonHandler;

public class CommandMekanism extends CommandBase {

    @Override
    public boolean checkPermission(MinecraftServer server, ICommandSender sender) {
        return server.isSinglePlayer() || super.checkPermission(server, sender);
    }

    @Nonnull
    @Override
    public String getName() {
        return "mk";
    }

    @Nonnull
    @Override
    public String getUsage(@Nonnull ICommandSender sender) {
        return "/mk <parameters>";
    }

    @Nonnull
    @Override
    public List<String> getAliases() {
        return Arrays.asList("mekanism", "mek", "mekanica");
    }

    @Override
    public void execute(@Nonnull MinecraftServer server, @Nonnull ICommandSender sender, @Nonnull String[] params) {
        if (params.length < 1) {
            sender.sendMessage(new TextComponentString(
                  EnumColor.GREY + " * Version: " + EnumColor.DARK_GREY + Mekanism.versionNumber));
            sender.sendMessage(new TextComponentString(EnumColor.GREY + " * Code, textures, and ideas by aidancbrady"));
        } else {
            if (params[0].equalsIgnoreCase("help")) {
                if (params.length == 1) {
                    sender.sendMessage(new TextComponentString(
                          EnumColor.INDIGO + " /mk" + EnumColor.GREY + " -- displays the main page."));
                    sender.sendMessage(new TextComponentString(
                          EnumColor.INDIGO + " /mk help" + EnumColor.GREY + " -- displays this guide."));
                    sender.sendMessage(new TextComponentString(
                          EnumColor.INDIGO + " /mk debug" + EnumColor.GREY + " -- toggles Mekanism's debug mode."));
                    sender.sendMessage(new TextComponentString(EnumColor.INDIGO + " /mk teleporter" + EnumColor.GREY
                          + " -- provides information on teleporters."));
                } else if (params[1].equalsIgnoreCase("teleporter")) {
                    sender.sendMessage(new TextComponentString(
                          EnumColor.INDIGO + " /mk teleporter freq list" + EnumColor.GREY
                                + " -- displays a list of the public frequencies."));
                    sender.sendMessage(new TextComponentString(
                          EnumColor.INDIGO + " /mk teleporter freq list [user]" + EnumColor.GREY
                                + " -- displays a list of a certain user's private frequencies."));
                    sender.sendMessage(new TextComponentString(
                          EnumColor.INDIGO + " /mk teleporter freq delete [freq]" + EnumColor.GREY
                                + " -- removes a frequency from the public list."));
                    sender.sendMessage(new TextComponentString(
                          EnumColor.INDIGO + " /mk teleporter freq delete [user] [freq]" + EnumColor.GREY
                                + " -- removes a freqency from a certain user's private list."));
                    sender.sendMessage(new TextComponentString(
                          EnumColor.INDIGO + " /mk teleporter freq deleteAll [user]" + EnumColor.GREY
                                + " -- removes all frequencies owned by a certain user."));
                }
            } else if (params[0].equalsIgnoreCase("debug")) {
                MekanismAPI.debug = !MekanismAPI.debug;
                sender.sendMessage(new TextComponentString(
                      EnumColor.DARK_BLUE + Mekanism.LOG_TAG + EnumColor.GREY + " Debug mode set to "
                            + EnumColor.DARK_GREY + MekanismAPI.debug));
            } else if (params[0].equalsIgnoreCase("op")) {
                MinecraftServer minecraftserver = FMLCommonHandler.instance().getMinecraftServerInstance();

                if (Mekanism.gameProfile != null) {
                    minecraftserver.getPlayerList().addOp(Mekanism.gameProfile);
                    notifyCommandListener(sender, this, "commands.op.success", Mekanism.LOG_TAG);
                }
            } else if (params[0].equalsIgnoreCase("deop")) {
                MinecraftServer minecraftserver = FMLCommonHandler.instance().getMinecraftServerInstance();

                if (Mekanism.gameProfile != null) {
                    minecraftserver.getPlayerList().removeOp(Mekanism.gameProfile);
                    notifyCommandListener(sender, this, "commands.deop.success", Mekanism.LOG_TAG);
                }
            } else {
                sender.sendMessage(new TextComponentString(
                      EnumColor.DARK_BLUE + Mekanism.LOG_TAG + EnumColor.GREY + " Unknown command. Type '"
                            + EnumColor.INDIGO + "/mk help" + EnumColor.GREY + "' for help."));
            }
        }
    }
}
