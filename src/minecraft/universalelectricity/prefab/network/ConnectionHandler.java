package universalelectricity.prefab.network;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.network.INetworkManager;
import net.minecraft.network.NetLoginHandler;
import net.minecraft.network.packet.NetHandler;
import net.minecraft.network.packet.Packet1Login;
import net.minecraft.server.MinecraftServer;
import cpw.mods.fml.common.network.IConnectionHandler;
import cpw.mods.fml.common.network.Player;

public class ConnectionHandler implements IConnectionHandler
{
	private static final List<ISimpleConnectionHandler> simpleConnectionHandlers = new ArrayList<ISimpleConnectionHandler>();

	public static enum ConnectionType
	{
		LOGIN_SERVER, LOGIN_CLIENT, RECEIVED, OPEN_REMOTE, OPEN_LOCAL, CLOSED
	}

	/**
	 * Registers a simple connection handler
	 * 
	 * @param tileEntity
	 */
	public static void registerConnectionHandler(ISimpleConnectionHandler tileEntity)
	{
		simpleConnectionHandlers.add(tileEntity);
	}

	@Override
	public void playerLoggedIn(Player player, NetHandler netHandler, INetworkManager manager)
	{
		for (ISimpleConnectionHandler handler : simpleConnectionHandlers)
		{
			handler.handelConnection(ConnectionType.LOGIN_SERVER, player, netHandler, manager);
		}
	}

	@Override
	public void clientLoggedIn(NetHandler clientHandler, INetworkManager manager, Packet1Login login)
	{
		for (ISimpleConnectionHandler handler : simpleConnectionHandlers)
		{
			handler.handelConnection(ConnectionType.LOGIN_CLIENT, clientHandler, manager, login);
		}
	}

	@Override
	public String connectionReceived(NetLoginHandler netHandler, INetworkManager manager)
	{
		for (ISimpleConnectionHandler handler : simpleConnectionHandlers)
		{
			handler.handelConnection(ConnectionType.RECEIVED, netHandler, manager);
		}

		return null;
	}

	@Override
	public void connectionOpened(NetHandler netClientHandler, String server, int port, INetworkManager manager)
	{
		for (ISimpleConnectionHandler handler : simpleConnectionHandlers)
		{
			handler.handelConnection(ConnectionType.OPEN_REMOTE, netClientHandler, server, port, manager);
		}
	}

	@Override
	public void connectionOpened(NetHandler netClientHandler, MinecraftServer server, INetworkManager manager)
	{
		for (ISimpleConnectionHandler handler : simpleConnectionHandlers)
		{
			handler.handelConnection(ConnectionType.OPEN_LOCAL, netClientHandler, server, manager);
		}
	}

	@Override
	public void connectionClosed(INetworkManager manager)
	{
		for (ISimpleConnectionHandler handler : simpleConnectionHandlers)
		{
			handler.handelConnection(ConnectionType.CLOSED, manager);
		}
	}
}
