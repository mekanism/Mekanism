package net.uberkat.obsidian.common;

import java.util.Arrays;
import java.util.List;

import net.minecraft.server.MinecraftServer;
import net.minecraft.src.CommandBase;
import net.minecraft.src.EntityPlayerMP;
import net.minecraft.src.ICommandSender;
import net.minecraft.src.PlayerNotFoundException;
import net.minecraft.src.WrongUsageException;

public class CommandOI extends CommandBase
{
    public String getCommandName()
    {
        return "oi";
    }
	
	public List getCommandAliases()
	{
		return Arrays.asList(new String[] {"obsidian", "obsidianingots"});
	}
	
    public boolean canCommandSenderUseCommand(ICommandSender sender)
    {
        return !MinecraftServer.getServer().isSinglePlayer() && super.canCommandSenderUseCommand(sender);
    }
	
    public void processCommand(ICommandSender sender, String[] params)
    {
        if(params.length < 2)
        {
            sender.sendChatToPlayer(EnumColor.GREY.code + "-------- " + EnumColor.DARK_BLUE.code + "[ObsidianIngots]" + EnumColor.GREY.code + " --------");
            sender.sendChatToPlayer(EnumColor.GREY.code + " *Version: " + EnumColor.DARK_GREY.code + ObsidianIngots.versionNumber);
            sender.sendChatToPlayer(EnumColor.GREY.code + " *Latest Version: " + EnumColor.DARK_GREY.code + ObsidianIngots.latestVersionNumber);
            sender.sendChatToPlayer(EnumColor.GREY.code + " *Developed on Mac OS X 10.8 Mountain Lion");
            sender.sendChatToPlayer(EnumColor.GREY.code + " *Code, textures, and ideas by aidancbrady");
            sender.sendChatToPlayer(EnumColor.GREY.code + " *Recent News: " + EnumColor.DARK_BLUE + ObsidianIngots.recentNews);
            sender.sendChatToPlayer(EnumColor.GREY.code + "-------- " + EnumColor.DARK_BLUE.code + "[==============]" + EnumColor.GREY.code + " --------");
        }
        else if(params.length == 2 && params[1].equalsIgnoreCase("update"))
        {
        	if(ObsidianUtils.isLatestVersion())
        	{
        		sender.sendChatToPlayer(EnumColor.DARK_BLUE.code + "[ObsidianIngots] " + EnumColor.GREY.code + "Obsidian Ingots is already up-to-date.");
        	}
        	else {
        		sender.sendChatToPlayer(EnumColor.DARK_BLUE.code + "[ObsidianIngots] " + EnumColor.GREY.code + "Preparing to update...");
        		new ThreadServerUpdate("http://dl.dropbox.com/u/90411166/ObsidianIngots.jar", sender);
        	}
        }
    }
}
