package mekanism.common;

import java.util.Arrays;
import java.util.List;

import mekanism.api.EnumColor;
import mekanism.common.util.MekanismUtils;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChatMessageComponent;
import net.minecraft.server.MinecraftServer;

public class CommandMekanism extends CommandBase
{
	@Override
	public boolean canCommandSenderUseCommand(ICommandSender sender)
	{
		return MinecraftServer.getServer().isSinglePlayer() || super.canCommandSenderUseCommand(sender);
	}
	
	@Override
    public String getCommandName()
    {
        return "mk";
    }
    
	@Override
    public String getCommandUsage(ICommandSender sender)
    {
    	return "/mk <parameters>";
    }
	
	@Override
	public List getCommandAliases()
	{
		return Arrays.asList(new String[] {"mekanism", "mek"});
	}
	
	@Override
    public void processCommand(ICommandSender sender, String[] params)
    {
        if(params.length < 1)
        {
            sender.sendChatToPlayer(ChatMessageComponent.func_111066_d(EnumColor.GREY + "------------- " + EnumColor.DARK_BLUE + "[Mekanism]" + EnumColor.GREY + " -------------"));
            sender.sendChatToPlayer(ChatMessageComponent.func_111066_d(EnumColor.GREY + " *Version: " + EnumColor.DARK_GREY + Mekanism.versionNumber));
            sender.sendChatToPlayer(ChatMessageComponent.func_111066_d(EnumColor.GREY + " *Latest Version: " + EnumColor.DARK_GREY + Mekanism.latestVersionNumber));
            sender.sendChatToPlayer(ChatMessageComponent.func_111066_d(EnumColor.GREY + " *Developed on Mac OS X 10.8 Mountain Lion"));
            sender.sendChatToPlayer(ChatMessageComponent.func_111066_d(EnumColor.GREY + " *Code, textures, and ideas by aidancbrady"));
            sender.sendChatToPlayer(ChatMessageComponent.func_111066_d(EnumColor.GREY + " *Recent News: " + EnumColor.INDIGO + Mekanism.recentNews));
            sender.sendChatToPlayer(ChatMessageComponent.func_111066_d(EnumColor.GREY + "------------- " + EnumColor.DARK_BLUE + "[=======]" + EnumColor.GREY + " -------------"));
        }
        else if(params.length == 1)
        {
	        if(params[0].equalsIgnoreCase("help"))
	        {
	            sender.sendChatToPlayer(ChatMessageComponent.func_111066_d(EnumColor.GREY + "------------- " + EnumColor.DARK_BLUE + "[Mekanism]" + EnumColor.GREY + " -------------"));
	            sender.sendChatToPlayer(ChatMessageComponent.func_111066_d(EnumColor.INDIGO + " /mk" + EnumColor.GREY + " -- displays the main page."));
	            sender.sendChatToPlayer(ChatMessageComponent.func_111066_d(EnumColor.INDIGO + " /mk help" + EnumColor.GREY + " -- displays this guide."));
	            sender.sendChatToPlayer(ChatMessageComponent.func_111066_d(EnumColor.INDIGO + " /mk version" + EnumColor.GREY + " -- displays the version number."));
	            sender.sendChatToPlayer(ChatMessageComponent.func_111066_d(EnumColor.INDIGO + " /mk latest" + EnumColor.GREY + " -- displays the latest version number."));
	            sender.sendChatToPlayer(ChatMessageComponent.func_111066_d(EnumColor.INDIGO + " /mk news" + EnumColor.GREY + " -- displays most recent recent news."));
	            sender.sendChatToPlayer(ChatMessageComponent.func_111066_d(EnumColor.INDIGO + " /mk debug" + EnumColor.GREY + " -- toggles Mekanism's debug mode."));
	            sender.sendChatToPlayer(ChatMessageComponent.func_111066_d(EnumColor.GREY + "------------- " + EnumColor.DARK_BLUE + "[=======]" + EnumColor.GREY + " -------------"));
	        }
	        else if(params[0].equalsIgnoreCase("version"))
	        {
	        	if(!MekanismUtils.checkForUpdates((EntityPlayer)sender))
	        	{
	        		if(Mekanism.updateNotifications || Mekanism.latestVersionNumber == null || Mekanism.recentNews == null || Mekanism.latestVersionNumber.equals("null"))
	        		{
	        			sender.sendChatToPlayer(ChatMessageComponent.func_111066_d(EnumColor.DARK_BLUE + "[Mekanism]" + EnumColor.GREY + " Minecraft is in offline mode, could not check for updates."));
	        		}
	        		else {
	        			sender.sendChatToPlayer(ChatMessageComponent.func_111066_d(EnumColor.DARK_BLUE + "[Mekanism]" + EnumColor.GREY + " Your client is up to date."));
	        		}
	        	}
	        }
	        else if(params[0].equalsIgnoreCase("news"))
	        {
	        	sender.sendChatToPlayer(ChatMessageComponent.func_111066_d(EnumColor.DARK_BLUE + "[Mekanism]" + EnumColor.GREY + " Most recent news: " + EnumColor.INDIGO + Mekanism.recentNews));
	        }
	        else if(params[0].equalsIgnoreCase("latest"))
	        {
	        	sender.sendChatToPlayer(ChatMessageComponent.func_111066_d(EnumColor.DARK_BLUE + "[Mekanism]" + EnumColor.GREY + " The latest version for this mod is " + EnumColor.DARK_GREY + Mekanism.latestVersionNumber + EnumColor.GREY + "."));
	        }
	        else if(params[0].equalsIgnoreCase("debug"))
	        {
	        	Mekanism.debug = !Mekanism.debug;
	        	sender.sendChatToPlayer(ChatMessageComponent.func_111066_d(EnumColor.DARK_BLUE + "[Mekanism]" + EnumColor.GREY + " Debug mode set to " + EnumColor.DARK_GREY + Mekanism.debug));
	        }
	        else {
	        	sender.sendChatToPlayer(ChatMessageComponent.func_111066_d(EnumColor.DARK_BLUE + "[Mekanism]" + EnumColor.GREY + " Unknown command. Type '" + EnumColor.INDIGO + "/mk help" + EnumColor.GREY + "' for help."));
	        }
        }
    }
}
