package mekanism.common;

import java.util.Arrays;
import java.util.List;

import mekanism.api.EnumColor;
import mekanism.api.MekanismConfig.general;
import mekanism.common.frequency.Frequency;
import mekanism.common.frequency.FrequencyManager;
import mekanism.common.tile.TileEntityTeleporter;
import mekanism.common.util.MekanismUtils;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentText;

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
			sender.addChatMessage(new ChatComponentText(EnumColor.GREY + "------------- " + EnumColor.DARK_BLUE + "[Mekanism]" + EnumColor.GREY + " -------------"));
			sender.addChatMessage(new ChatComponentText(EnumColor.GREY + " *Version: " + EnumColor.DARK_GREY + Mekanism.versionNumber));
			sender.addChatMessage(new ChatComponentText(EnumColor.GREY + " *Latest Version: " + EnumColor.DARK_GREY + Mekanism.latestVersionNumber));
			sender.addChatMessage(new ChatComponentText(EnumColor.GREY + " *Developed on Mac OS X 10.8 Mountain Lion"));
			sender.addChatMessage(new ChatComponentText(EnumColor.GREY + " *Code, textures, and ideas by aidancbrady"));
			sender.addChatMessage(new ChatComponentText(EnumColor.GREY + " *Recent News: " + EnumColor.INDIGO + Mekanism.recentNews));
			sender.addChatMessage(new ChatComponentText(EnumColor.GREY + "------------- " + EnumColor.DARK_BLUE + "[=======]" + EnumColor.GREY + " -------------"));
		}
		else if(params.length >= 1)
		{
			if(params[0].equalsIgnoreCase("help"))
			{
				if(params.length == 1)
				{
					sender.addChatMessage(new ChatComponentText(EnumColor.GREY + "------------- " + EnumColor.DARK_BLUE + "[Mekanism]" + EnumColor.GREY + " -------------"));
					sender.addChatMessage(new ChatComponentText(EnumColor.INDIGO + " /mk" + EnumColor.GREY + " -- displays the main page."));
					sender.addChatMessage(new ChatComponentText(EnumColor.INDIGO + " /mk help" + EnumColor.GREY + " -- displays this guide."));
					sender.addChatMessage(new ChatComponentText(EnumColor.INDIGO + " /mk version" + EnumColor.GREY + " -- displays the version number."));
					sender.addChatMessage(new ChatComponentText(EnumColor.INDIGO + " /mk latest" + EnumColor.GREY + " -- displays the latest version number."));
					sender.addChatMessage(new ChatComponentText(EnumColor.INDIGO + " /mk news" + EnumColor.GREY + " -- displays most recent recent news."));
					sender.addChatMessage(new ChatComponentText(EnumColor.INDIGO + " /mk debug" + EnumColor.GREY + " -- toggles Mekanism's debug mode."));
					sender.addChatMessage(new ChatComponentText(EnumColor.INDIGO + " /mk teleporter" + EnumColor.GREY + " -- provides information on teleporters."));
					sender.addChatMessage(new ChatComponentText(EnumColor.GREY + "------------- " + EnumColor.DARK_BLUE + "[=======]" + EnumColor.GREY + " -------------"));
				}
				else if(params[1].equalsIgnoreCase("teleporter"))
				{
					sender.addChatMessage(new ChatComponentText(EnumColor.GREY + "------------- " + EnumColor.DARK_BLUE + "[Mekanism]" + EnumColor.GREY + " -------------"));
					sender.addChatMessage(new ChatComponentText(EnumColor.INDIGO + " /mk teleporter freq list" + EnumColor.GREY + " -- displays a list of the public frequencies."));
					sender.addChatMessage(new ChatComponentText(EnumColor.INDIGO + " /mk teleporter freq list [user]" + EnumColor.GREY + " -- displays a list of a certain user's private frequencies."));
					sender.addChatMessage(new ChatComponentText(EnumColor.INDIGO + " /mk teleporter freq delete [freq]" + EnumColor.GREY + " -- removes a frequency from the public list."));
					sender.addChatMessage(new ChatComponentText(EnumColor.INDIGO + " /mk teleporter freq delete [user] [freq]" + EnumColor.GREY + " -- removes a freqency from a certain user's private list."));
					sender.addChatMessage(new ChatComponentText(EnumColor.INDIGO + " /mk teleporter freq deleteAll [user]" + EnumColor.GREY + " -- removes all frequencies owned by a certain user."));
					sender.addChatMessage(new ChatComponentText(EnumColor.GREY + "------------- " + EnumColor.DARK_BLUE + "[=======]" + EnumColor.GREY + " -------------"));
				}
			}
			else if(params[0].equalsIgnoreCase("version"))
			{
				if(!MekanismUtils.checkForUpdates((EntityPlayer)sender))
				{
					if(general.updateNotifications || Mekanism.latestVersionNumber == null || Mekanism.recentNews == null || Mekanism.latestVersionNumber.equals("null"))
					{
						sender.addChatMessage(new ChatComponentText(EnumColor.DARK_BLUE + "[Mekanism]" + EnumColor.GREY + " Minecraft is in offline mode, could not check for updates."));
					}
					else {
						sender.addChatMessage(new ChatComponentText(EnumColor.DARK_BLUE + "[Mekanism]" + EnumColor.GREY + " Your client is up to date."));
					}
				}
			}
			else if(params[0].equalsIgnoreCase("news"))
			{
				sender.addChatMessage(new ChatComponentText(EnumColor.DARK_BLUE + "[Mekanism]" + EnumColor.GREY + " Most recent news: " + EnumColor.INDIGO + Mekanism.recentNews));
			}
			else if(params[0].equalsIgnoreCase("latest"))
			{
				sender.addChatMessage(new ChatComponentText(EnumColor.DARK_BLUE + "[Mekanism]" + EnumColor.GREY + " The latest version for this mod is " + EnumColor.DARK_GREY + Mekanism.latestVersionNumber + EnumColor.GREY + "."));
			}
			else if(params[0].equalsIgnoreCase("teleporter"))
			{
				if(params.length == 2)
				{
					sender.addChatMessage(new ChatComponentText(EnumColor.DARK_BLUE + "[Mekanism]" + EnumColor.GREY + " Invalid parameters."));
				}
				else if(params[1].equalsIgnoreCase("freq") || params[1].equalsIgnoreCase("frequencies"))
				{
					if(params[2].equalsIgnoreCase("list"))
					{
						sender.addChatMessage(new ChatComponentText(EnumColor.GREY + "------------- " + EnumColor.DARK_BLUE + "[Mekanism]" + EnumColor.GREY + " -------------"));
						
						if(params.length == 3)
						{
							for(Frequency freq : Mekanism.publicTeleporters.getFrequencies())
							{
								sender.addChatMessage(new ChatComponentText(EnumColor.INDIGO + " - " + freq.name + EnumColor.GREY + " (" + freq.owner + ")"));
							}
						}
						else {
							FrequencyManager manager = TileEntityTeleporter.loadManager(params[3].trim(), sender.getEntityWorld());
							
							if(manager != null)
							{
								for(Frequency freq : manager.getFrequencies())
								{
									sender.addChatMessage(new ChatComponentText(EnumColor.INDIGO + " - " + freq.name + EnumColor.GREY + " (" + freq.owner + ")"));
								}
							}
							else {
								sender.addChatMessage(new ChatComponentText(EnumColor.DARK_BLUE + "[Mekanism]" + EnumColor.GREY + " User profile doesn't exist."));
							}
						}
						
						sender.addChatMessage(new ChatComponentText(EnumColor.GREY + "------------- " + EnumColor.DARK_BLUE + "[=======]" + EnumColor.GREY + " -------------"));
					}
					else if(params[2].equalsIgnoreCase("delete"))
					{
						if(params.length == 4)
						{
							if(Mekanism.publicTeleporters.containsFrequency(params[3].trim()))
							{
								Mekanism.publicTeleporters.remove(params[3].trim());
								sender.addChatMessage(new ChatComponentText(EnumColor.DARK_BLUE + "[Mekanism]" + EnumColor.GREY + " Successfully removed frequency."));
							}
							else {
								sender.addChatMessage(new ChatComponentText(EnumColor.DARK_BLUE + "[Mekanism]" + EnumColor.GREY + " No such frequency found."));
							}
						}
						else if(params.length == 5)
						{
							FrequencyManager manager = TileEntityTeleporter.loadManager(params[3].trim(), sender.getEntityWorld());
							
							if(manager != null)
							{
								if(manager.containsFrequency(params[4].trim()))
								{
									manager.remove(params[4].trim());
									sender.addChatMessage(new ChatComponentText(EnumColor.DARK_BLUE + "[Mekanism]" + EnumColor.GREY + " Successfully removed frequency."));
								}
								else {
									sender.addChatMessage(new ChatComponentText(EnumColor.DARK_BLUE + "[Mekanism]" + EnumColor.GREY + " No such frequency found."));
								}
							}
							else {
								sender.addChatMessage(new ChatComponentText(EnumColor.DARK_BLUE + "[Mekanism]" + EnumColor.GREY + " User profile doesn't exist."));
							}
						}
					}
					else if(params[2].equalsIgnoreCase("deleteAll"))
					{
						if(params.length == 4)
						{
							String owner = params[3].trim();
							FrequencyManager manager = TileEntityTeleporter.loadManager(owner, sender.getEntityWorld());
							
							if(manager != null)
							{
								int amount = Mekanism.publicTeleporters.removeAll(owner);
								amount += manager.removeAll(owner);
								
								sender.addChatMessage(new ChatComponentText(EnumColor.DARK_BLUE + "[Mekanism]" + EnumColor.GREY + " Successfully removed " + amount + " frequencies."));
							}
							else {
								sender.addChatMessage(new ChatComponentText(EnumColor.DARK_BLUE + "[Mekanism]" + EnumColor.GREY + " User profile doesn't exist."));
							}
						}
					}
				}
			}
			else if(params[0].equalsIgnoreCase("debug"))
			{
				Mekanism.debug = !Mekanism.debug;
				sender.addChatMessage(new ChatComponentText(EnumColor.DARK_BLUE + "[Mekanism]" + EnumColor.GREY + " Debug mode set to " + EnumColor.DARK_GREY + Mekanism.debug));
			}
			else {
				sender.addChatMessage(new ChatComponentText(EnumColor.DARK_BLUE + "[Mekanism]" + EnumColor.GREY + " Unknown command. Type '" + EnumColor.INDIGO + "/mk help" + EnumColor.GREY + "' for help."));
			}
		}
	}

	@Override
	public int compareTo(Object obj)
	{
		return 0;
	}
}
