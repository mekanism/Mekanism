package mekanism.common.voice;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import mekanism.common.Mekanism;
import mekanism.common.item.ItemWalkieTalkie;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import cpw.mods.fml.common.FMLCommonHandler;

public class VoiceConnection extends Thread
{
	public Socket socket;

	public String username;

	public boolean open = true;

	public DataInputStream input;
	public DataOutputStream output;

	public MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();

	public VoiceConnection(Socket s)
	{
		socket = s;
	}

	@Override
	public void run()
	{
		try {
			input = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
			output = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));

			synchronized(Mekanism.voiceManager)
			{
				int retryCount = 0;

				while(username == null && retryCount <= 100)
				{
					try {
						List l = Collections.synchronizedList((List)((ArrayList)server.getConfigurationManager().playerEntityList).clone());

						for(Object obj : l)
						{
							if(obj instanceof EntityPlayerMP)
							{
								EntityPlayerMP playerMP = (EntityPlayerMP)obj;
								String playerIP = playerMP.getPlayerIP();

								if(!server.isDedicatedServer() && playerIP.equals("local") && !Mekanism.voiceManager.foundLocal)
								{
									Mekanism.voiceManager.foundLocal = true;
									username = playerMP.getCommandSenderName();
									break;
								}
								else if(playerIP.equals(socket.getInetAddress().getHostAddress()))
								{
									username = playerMP.getCommandSenderName();
									break;
								}
							}
						}

						retryCount++;
						Thread.sleep(50);
					} catch(Exception e) {}
				}

				if(username == null)
				{
					Mekanism.logger.error("VoiceServer: Unable to trace connection's IP address.");
					kill();
					return;
				}
				else {
					Mekanism.logger.info("VoiceServer: Traced IP in " + retryCount + " attempts.");
				}
			}
		} catch(Exception e) {
			Mekanism.logger.error("VoiceServer: Error while starting server-based connection.");
			e.printStackTrace();
			open = false;
		}

		//Main client listen thread
		new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				while(open)
				{
					try {
						short byteCount = VoiceConnection.this.input.readShort();
						byte[] audioData = new byte[byteCount];
						VoiceConnection.this.input.readFully(audioData);

						if(byteCount > 0)
						{
							Mekanism.voiceManager.sendToPlayers(byteCount, audioData, VoiceConnection.this);
						}
					} catch(Exception e) {
						open = false;
					}
				}

				if(!open)
				{
					kill();
				}
			}
		}).start();
	}

	public void kill()
	{
		try {
			input.close();
			output.close();
			socket.close();

			Mekanism.voiceManager.connections.remove(this);
		} catch(Exception e) {
			Mekanism.logger.error("VoiceServer: Error while stopping server-based connection.");
			e.printStackTrace();
		}
	}

	public void sendToPlayer(short byteCount, byte[] audioData, VoiceConnection connection)
	{
		if(!open)
		{
			kill();
		}

		try {
			output.writeShort(byteCount);
			output.write(audioData);

			output.flush();
		} catch(Exception e) {
			Mekanism.logger.error("VoiceServer: Error while sending data to player.");
			e.printStackTrace();
		}
	}

	public boolean canListen(int channel)
	{
		for(ItemStack itemStack : getPlayer().inventory.mainInventory)
		{
			if(itemStack != null)
			{
				if(itemStack.getItem() instanceof ItemWalkieTalkie)
				{
					if(((ItemWalkieTalkie)itemStack.getItem()).getOn(itemStack))
					{
						if(((ItemWalkieTalkie)itemStack.getItem()).getChannel(itemStack) == channel)
						{
							return true;
						}
					}
				}
			}
		}

		return false;
	}

	public int getCurrentChannel()
	{
		ItemStack itemStack = getPlayer().getCurrentEquippedItem();

		if(itemStack != null)
		{
			ItemWalkieTalkie walkieTalkie = (ItemWalkieTalkie)itemStack.getItem();

			if(walkieTalkie != null)
			{
				if(walkieTalkie.getOn(itemStack))
				{
					return walkieTalkie.getChannel(itemStack);
				}
			}
		}

		return 0;
	}

	public EntityPlayerMP getPlayer()
	{
		return server.getConfigurationManager().func_152612_a(username); //TODO getPlayerForUsername
	}
}
