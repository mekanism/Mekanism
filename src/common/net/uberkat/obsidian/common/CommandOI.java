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
    
    public String getCommandUsage(ICommandSender sender)
    {
    	return "/oi <parameters>";
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
        if(params.length < 1)
        {
            sender.sendChatToPlayer(EnumColor.GREY + "-------- " + EnumColor.DARK_BLUE + "[ObsidianIngots]" + EnumColor.GREY + " --------");
            sender.sendChatToPlayer(EnumColor.GREY + " *Version: " + EnumColor.DARK_GREY + ObsidianIngots.versionNumber);
            sender.sendChatToPlayer(EnumColor.GREY + " *Latest Version: " + EnumColor.DARK_GREY + ObsidianIngots.latestVersionNumber);
            sender.sendChatToPlayer(EnumColor.GREY + " *Developed on Mac OS X 10.8 Mountain Lion");
            sender.sendChatToPlayer(EnumColor.GREY + " *Code, textures, and ideas by aidancbrady");
            sender.sendChatToPlayer(EnumColor.GREY + " *Recent News: " + EnumColor.INDIGO + ObsidianIngots.recentNews);
            sender.sendChatToPlayer(EnumColor.GREY + "-------- " + EnumColor.DARK_BLUE + "[============]" + EnumColor.GREY + " --------");
        }
        else if(params.length == 1)
        {
	        if(params[0].equalsIgnoreCase("help"))
	        {
	            sender.sendChatToPlayer(EnumColor.GREY + "-------- " + EnumColor.DARK_BLUE + "[ObsidianIngots]" + EnumColor.GREY + " --------");
	            sender.sendChatToPlayer(EnumColor.INDIGO + " /oi" + EnumColor.GREY + " -- displays the main page.");
	            sender.sendChatToPlayer(EnumColor.INDIGO + " /oi help" + EnumColor.GREY + " -- displays this guide.");
	            sender.sendChatToPlayer(EnumColor.INDIGO + " /oi version" + EnumColor.GREY + " -- displays the version number.");
	            sender.sendChatToPlayer(EnumColor.INDIGO + " /oi latest" + EnumColor.GREY + " -- displays the latest version number.");
	            sender.sendChatToPlayer(EnumColor.INDIGO + " /oi news" + EnumColor.GREY + " -- displays most recent recent news.");
	            sender.sendChatToPlayer(EnumColor.GREY + "-------- " + EnumColor.DARK_BLUE + "[============]" + EnumColor.GREY + " --------");
	        }
	        
	        else if(params[0].equalsIgnoreCase("version"))
	        {
	        	sender.sendChatToPlayer(EnumColor.DARK_BLUE + "[ObsidianIngots]" + EnumColor.GREY + " This server is running on version " + EnumColor.DARK_GREY + ObsidianIngots.versionNumber.toString() + EnumColor.GREY + ".");
	        }
	        
	        else if(params[0].equalsIgnoreCase("news"))
	        {
	        	sender.sendChatToPlayer(EnumColor.DARK_BLUE + "[ObsidianIngots]" + EnumColor.GREY + " Most recent news: " + EnumColor.INDIGO + ObsidianIngots.recentNews);
	        }
	        
	        else if(params[0].equalsIgnoreCase("latest"))
	        {
	        	sender.sendChatToPlayer(EnumColor.DARK_BLUE + "[ObsidianIngots]" + EnumColor.GREY + " The latest version for this mod is " + EnumColor.DARK_GREY + ObsidianIngots.latestVersionNumber + EnumColor.GREY + ".");
	        }
	        
	        else {
	        	sender.sendChatToPlayer(EnumColor.DARK_BLUE + "[ObsidianIngots]" + EnumColor.GREY + " Unknown command. Type '" + EnumColor.INDIGO + "/oi help" + EnumColor.GREY + "' for help.");
	        }
        }
    }
}
