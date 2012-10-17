package universalelectricity.network;

import java.util.Arrays;
import java.util.List;

import net.minecraft.src.CommandBase;
import net.minecraft.src.ICommand;
import net.minecraft.src.ICommandSender;
import net.minecraft.src.WrongUsageException;
import universalelectricity.UniversalElectricity;

public class UECommand extends CommandBase
{	
	@Override
	public int compareTo(Object arg0)
	{
        return this.getCommandName().compareTo(((ICommand)arg0).getCommandName());
	}

	@Override
	public String getCommandName()
	{
		return "universalelectricity";
	}

	@Override
	public String getCommandUsage(ICommandSender sender)
	{
		return "/" + this.getCommandName();
	}

	@Override
	public List getCommandAliases()
	{
		return Arrays.asList(new String[]{"ue"});
	}

	@Override
	public void processCommand(ICommandSender sender, String[] arguments)
	{
        if(arguments.length <= 0)
        {
        	sender.sendChatToPlayer(String.format("You are using Universal Electricity v" + UniversalElectricity.VERSION));
        	return;
        }

    	throw new WrongUsageException(this.getCommandUsage(sender));
	}
}