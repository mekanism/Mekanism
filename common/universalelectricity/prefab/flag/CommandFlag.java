package universalelectricity.prefab.flag;

import java.util.Iterator;
import java.util.List;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayer;
import universalelectricity.core.vector.Vector3;

/**
 * Commands used for flags and regions. This can be used for protection for specific mod components
 * that might be dangerous.
 * 
 * @author Calclavia
 * 
 */
public class CommandFlag extends CommandBase
{
	public static final String[] COMMANDS = new String[] { "list", "setregion", "removeregion", "set" };
	public String commandName = "modflag";
	public ModFlag modFlagData;

	public CommandFlag(ModFlag modFlag)
	{
		this.modFlagData = modFlag;
	}

	public CommandFlag(ModFlag modFlag, String commandName)
	{
		this(modFlag);
		this.commandName = commandName;
	}

	@Override
	public String getCommandName()
	{
		return commandName;
	}

	@Override
	public String getCommandUsage(ICommandSender par1ICommandSender)
	{
		String returnString = "";

		for (String command : COMMANDS)
		{
			returnString = returnString + "\n/" + this.getCommandName() + " " + command;
		}

		return returnString;
	}

	@Override
	public void processCommand(ICommandSender sender, String[] args)
	{
		if (args.length > 0)
		{
			EntityPlayer entityPlayer = (EntityPlayer) sender;

			// The world data the player is on.
			FlagWorld flagWorld = this.modFlagData.getFlagWorld(entityPlayer.worldObj);

			String commandName = args[0].toLowerCase();

			if (commandName.equalsIgnoreCase("list"))
			{
				/**
				 * The list command lists out all regions in this world/region.
				 */
				if (args.length > 1)
				{
					String regionName = args[1];

					if (regionName.equalsIgnoreCase("all"))
					{
						String msg = "";

						Iterator<FlagWorld> itWorlds = this.modFlagData.getFlagWorlds().iterator();

						while (itWorlds.hasNext())
						{
							Iterator<FlagRegion> itRegion = itWorlds.next().getRegions().iterator();

							while (itRegion.hasNext())
							{
								FlagRegion flagRegion = itRegion.next();
								msg = msg + " " + flagRegion.name + " (" + flagRegion.region.min.x + "," + flagRegion.region.min.z + ")" + ",";
							}
						}

						if (msg != "")
						{
							msg = "List of regions in world:\n" + msg;
						}
						else
						{
							msg = "No regions in this world.";
						}

						sender.sendChatToPlayer(msg);
					}
					else if (flagWorld.getRegion(regionName) != null)
					{
						String msg = "";

						Iterator<Flag> i = flagWorld.getRegion(regionName).getFlags().iterator();

						while (i.hasNext())
						{
							Flag flag = i.next();
							msg = msg + " " + flag.name + " => " + flag.value + ",";
						}

						if (msg != "")
						{
							msg = "List of flags in region " + regionName + ":\n" + msg;
						}
						else
						{
							msg = "No flags in this region.";
						}

						sender.sendChatToPlayer(msg);
					}
					else
					{
						String msg = "Region does not exist, but here are existing flags in the position you are standing on:\n";

						Iterator<Flag> i = flagWorld.getFlagsInPosition(new Vector3(entityPlayer)).iterator();

						while (i.hasNext())
						{
							Flag flag = i.next();
							msg = msg + " " + flag.name + "=>" + flag.value + ",";
						}

						sender.sendChatToPlayer(msg);
					}

				}
				else
				{
					String msg = "";

					Iterator<FlagRegion> i = flagWorld.getRegions().iterator();
					while (i.hasNext())
					{
						FlagRegion flagRegion = i.next();
						msg = msg + " " + flagRegion.name + " (" + flagRegion.region.min.x + "," + flagRegion.region.min.z + ")" + ",";
					}

					if (msg != "")
					{
						msg = "List of regions in this dimension:\n" + msg;
					}
					else
					{
						msg = "No regions in this dimension.";
					}

					sender.sendChatToPlayer(msg);
				}

				return;
			}
			else if (commandName.equalsIgnoreCase("setregion"))
			{
				if (args.length > 1)
				{
					String regionName = args[1];

					if (regionName.equalsIgnoreCase(FlagWorld.GLOBAL_REGION))
					{
						if (flagWorld.addRegion(regionName, new Vector3(entityPlayer), 1))
						{
							sender.sendChatToPlayer("Created global dimension region setting.");
							return;
						}
					}
					else if (args.length > 2)
					{
						int radius = 0;

						try
						{
							radius = Integer.parseInt(args[2]);
						}
						catch (Exception e)
						{
							throw new WrongUsageException("Radius not a number!");
						}

						if (radius > 0)
						{
							FlagRegion region = flagWorld.getRegion(regionName);
							if (region == null)
							{
								if (flagWorld.addRegion(regionName, new Vector3(entityPlayer), radius))
								{
									sender.sendChatToPlayer("Region " + regionName + " added.");
								}
							}
							else
							{
								region.edit(new Vector3(entityPlayer), radius);
								sender.sendChatToPlayer("Region " + regionName + " already exists. Modified region to have a radius of: " + radius);
							}
						}
						else
						{
							throw new WrongUsageException("Radius has to be greater than zero!");
						}

					}
					else
					{
						throw new WrongUsageException("/" + this.getCommandName() + " addregion <name> <radius>");
					}
				}
				else
				{
					throw new WrongUsageException("Please specify the region name.");
				}

				return;
			}
			else if (commandName.equalsIgnoreCase("removeregion"))

			{
				if (args.length > 1)
				{
					String regionName = args[1];

					if (flagWorld.removeRegion(regionName))
					{
						sender.sendChatToPlayer("Region with name " + regionName + " is removed.");
					}
					else
					{
						throw new WrongUsageException("The specified region does not exist in this world.");
					}
				}
				else
				{
					throw new WrongUsageException("Please specify the region name.");
				}

				return;
			}
			else if (commandName.equalsIgnoreCase("set"))
			{
				if (args.length > 2)
				{
					String regionName = args[1];
					String flagName = args[2];
					FlagRegion flagRegion = flagWorld.getRegion(regionName);

					if (flagRegion != null)
					{
						if (FlagRegistry.flags.contains(flagName))
						{
							if (args.length > 3)
							{
								String flagValue = args[3];

								flagRegion.setFlag(flagName, flagValue);
								sender.sendChatToPlayer("Flag '" + flagName + "' has been set to '" + flagValue + "' in " + regionName + ".");
							}
							else
							{
								flagRegion.removeFlag(flagName);
								sender.sendChatToPlayer("Removed flag '" + flagName + "'.");
							}
						}
						else
						{
							String flags = "Flag does not exist. Existing flags:\n";

							for (String registeredFlag : FlagRegistry.flags)
							{
								flags = flags + registeredFlag + ", ";
							}

							throw new WrongUsageException(flags);
						}
					}
					else
					{
						throw new WrongUsageException("The specified region '" + regionName + "' does not exist.");
					}
				}
				else
				{
					throw new WrongUsageException("/" + this.getCommandName() + " set <regionName> <flagName> <value>");
				}

				return;
			}
		}

		throw new WrongUsageException(this.getCommandUsage(sender));

	}

	@Override
	public int getRequiredPermissionLevel()
	{
		return 2;
	}

	@Override
	public List addTabCompletionOptions(ICommandSender sender, String[] args)
	{
		return args.length == 1 ? getListOfStringsMatchingLastWord(args, COMMANDS) : null;
	}

}
