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
            sender.sendChatToPlayer(EnumColor.GREY.code + "-------- " + EnumColor.DARK_BLUE.code + "[ObsidianIngots]" + EnumColor.GREY.code + " --------");
            sender.sendChatToPlayer(EnumColor.GREY.code + " *Version: " + EnumColor.DARK_GREY.code + ObsidianIngots.versionNumber);
            sender.sendChatToPlayer(EnumColor.GREY.code + " *Latest Version: " + EnumColor.DARK_GREY.code + ObsidianIngots.latestVersionNumber);
            sender.sendChatToPlayer(EnumColor.GREY.code + " *Developed on Mac OS X 10.8 Mountain Lion");
            sender.sendChatToPlayer(EnumColor.GREY.code + " *Code, textures, and ideas by aidancbrady");
            sender.sendChatToPlayer(EnumColor.GREY.code + " *Recent News: " + EnumColor.INDIGO.code + ObsidianIngots.recentNews);
            sender.sendChatToPlayer(EnumColor.GREY.code + "-------- " + EnumColor.DARK_BLUE.code + "[============]" + EnumColor.GREY.code + " --------");
        }
        else if(params.length == 1)
        {
	        if(params[0].equalsIgnoreCase("update"))
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
	        
	        else if(params[0].equalsIgnoreCase("help"))
	        {
	            sender.sendChatToPlayer(EnumColor.GREY.code + "-------- " + EnumColor.DARK_BLUE.code + "[ObsidianIngots]" + EnumColor.GREY.code + " --------");
	            sender.sendChatToPlayer(EnumColor.INDIGO.code + " /oi" + EnumColor.GREY.code + " -- displays the main page.");
	            sender.sendChatToPlayer(EnumColor.INDIGO.code + " /oi help" + EnumColor.GREY.code + " -- displays this guide.");
	            sender.sendChatToPlayer(EnumColor.INDIGO.code + " /oi update" + EnumColor.GREY.code + " -- updates the Obsidian Ingots server.");
	            sender.sendChatToPlayer(EnumColor.INDIGO.code + " /oi version" + EnumColor.GREY.code + " -- displays the version number.");
	            sender.sendChatToPlayer(EnumColor.INDIGO.code + " /oi news" + EnumColor.GREY.code + " -- displays most recent recent news.");
	            sender.sendChatToPlayer(EnumColor.GREY.code + "-------- " + EnumColor.DARK_BLUE.code + "[============]" + EnumColor.GREY.code + " --------");
	        }
	        
	        else if(params[0].equalsIgnoreCase("version"))
	        {
	        	sender.sendChatToPlayer(EnumColor.DARK_BLUE.code + "[ObsidianIngots]" + EnumColor.GREY.code + " This server is running on version " + EnumColor.DARK_GREY.code + ObsidianIngots.versionNumber.toString() + EnumColor.GREY.code + ".");
	        }
	        
	        else if(params[0].equalsIgnoreCase("news"))
	        {
	        	sender.sendChatToPlayer(EnumColor.DARK_BLUE.code + "[ObsidianIngots]" + EnumColor.GREY.code + " Most recent news: " + EnumColor.INDIGO.code + ObsidianIngots.recentNews);
	        }
	        
	        else if(params[0].equalsIgnoreCase("latest"))
	        {
	        	sender.sendChatToPlayer(EnumColor.DARK_BLUE.code + "[ObsidianIngots]" + EnumColor.GREY.code + " The latest version for this mod is " + EnumColor.DARK_GREY.code + ObsidianIngots.latestVersionNumber + EnumColor.GREY.code + ".");
	        }
	        
	        else {
	        	sender.sendChatToPlayer(EnumColor.DARK_BLUE.code + "[ObsidianIngots]" + EnumColor.GREY.code + " Unknown command. Type '" + EnumColor.INDIGO.code + "/oi help" + EnumColor.GREY.code + "' for help.");
	        }
        }
    }
}
