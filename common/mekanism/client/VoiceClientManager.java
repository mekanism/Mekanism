package mekanism.client;

import java.net.InetAddress;
import java.net.Socket;

import net.minecraft.network.INetworkManager;
import net.minecraft.network.NetLoginHandler;
import net.minecraft.network.packet.NetHandler;
import net.minecraft.network.packet.Packet1Login;
import net.minecraft.server.MinecraftServer;
import cpw.mods.fml.common.network.IConnectionHandler;
import cpw.mods.fml.common.network.Player;

public class VoiceClientManager implements IConnectionHandler
{
	public Socket socket;

	@Override
	public void playerLoggedIn(Player player, NetHandler netHandler, INetworkManager manager)
	{
		try {
			socket = new Socket(manager.getSocketAddress().toString(), 36123);
		} catch(Exception e) {}
	}

	@Override
	public String connectionReceived(NetLoginHandler netHandler, INetworkManager manager)
	{
		return null;
	}

	@Override
	public void connectionOpened(NetHandler netClientHandler, String server, int port, INetworkManager manager) 
	{
		//connecting to foreign server
		try {
			socket = new Socket(server, 36123);
		} catch(Exception e) {}
	}

	@Override
	public void connectionOpened(NetHandler netClientHandler, MinecraftServer server, INetworkManager manager) 
	{
		//connecting to LAN server on same instance
		try {
			socket = new Socket(InetAddress.getLocalHost().getHostAddress(), 36123);
		} catch(Exception e) {}
	}

	@Override
	public void connectionClosed(INetworkManager manager) 
	{
		try {
			socket.close();
			socket = null;
		} catch(Exception e) {}
	}

	@Override
	public void clientLoggedIn(NetHandler clientHandler, INetworkManager manager, Packet1Login login)
	{

	}
}
