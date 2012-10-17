package universalelectricity.network;

import universalelectricity.network.ConnectionHandler.ConnectionType;

public interface ISimpleConnectionHandler
{
	/**
     * Called when a player logs in. Use this to reset some tile entities variables if you need to.
     * @param player
     */
    public void handelConnection(ConnectionType type, Object... data);
}
