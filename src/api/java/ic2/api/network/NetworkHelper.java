package ic2.api.network;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;

import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.relauncher.Side;

import ic2.api.info.Info;

/**
 * Provides methods to initiate events and synchronize tile entity fields in SMP.
 *
 * The methods are transparent between singleplayer and multiplayer - if a method is called in
 * singleplayer, the associated callback will be locally executed. The implementation is different
 * between the client and server versions of IC2.
 *
 * You'll usually want to use the server->client methods defined here to synchronize information
 * which is needed by the clients outside the GUI, such as rendering the block, playing sounds or
 * producing effects. Anything which is only visible inside the GUI should be synchronized through
 * the Container class associated to the GUI in Container.updateProgressBar().
 *
 * All methods in this class use the current effective side to determine the Network Manager to use.
 * If you do not want it to use the effective side, use {@link #getNetworkManager(Side)} to get the
 * Network Manager for a specific side.
 */
public final class NetworkHelper {
	// server -> client


	/**
	 * Schedule a TileEntity's field to be updated to the clients in range.
	 *
	 * The updater will query the field's value during the next update, updates happen usually
	 * every 2 ticks. If low latency is important use initiateTileEntityEvent instead.
	 *
	 * IC2's network updates have to get triggered every time, it doesn't continuously poll/send
	 * the field value. Just call updateTileEntityField after every change to a field which needs
	 * network synchronization.
	 *
	 * The following field data types are currently supported:
	 *  - int, int[], short, short[], byte, byte[], long, long[]
	 *  - float, float[], double, double[]
	 *  - boolean, boolean[]
	 *  - String, String[]
	 *  - ItemStack
	 *  - NBTBase (includes NBTTagCompound)
	 *  - Block, Item, Achievement, Potion, Enchantment
	 *  - BlockPos, ChunkCoordIntPair
	 *  - TileEntity (does not sync the actual tile entity, instead looks up the tile entity by its position in the client world)
	 *  - World (does not sync the actual world, instead looks up the world by its dimension ID)
	 *
	 * Once the update has been processed by the client, it'll call onNetworkUpdate on the client-
	 * side TileEntity if it implements INetworkUpdateListener.
	 *
	 * If this method is being executed on the client (i.e. Singleplayer), it'll just call
	 * INetworkUpdateListener.onNetworkUpdate (if implemented by the te).
	 *
	 * @param te TileEntity to update
	 * @param field Name of the field to update
	 */
	public static void updateTileEntityField(TileEntity te, String field) {
		getNetworkManager(FMLCommonHandler.instance().getEffectiveSide()).updateTileEntityField(te, field);
	}

	/**
	 * Immediately send an event for the specified TileEntity to the clients in range.
	 *
	 * If this method is being executed on the client (i.e. Singleplayer), it'll just call
	 * INetworkTileEntityEventListener.onNetworkEvent (if implemented by the te).
	 *
	 * @param te TileEntity to notify, should implement INetworkTileEntityEventListener
	 * @param event Arbitrary integer to represent the event, choosing the values is up to you
	 * @param limitRange Limit the notification range to (currently) 20 blocks instead of the
	 *        tracking distance if true
	 */
	public static void initiateTileEntityEvent(TileEntity te, int event, boolean limitRange) {
		getNetworkManager(FMLCommonHandler.instance().getEffectiveSide()).initiateTileEntityEvent(te, event, limitRange);
	}

	/**
	 * Immediately send an event for the specified Item to the clients in range.
	 *
	 * The item should implement INetworkItemEventListener to receive the event.
	 *
	 * If this method is being executed on the client (i.e. Singleplayer), it'll just call
	 * INetworkItemEventListener.onNetworkEvent (if implemented by the item).
	 *
	 * @param player EntityPlayer holding the item
	 * @param stack ItemStack containing the item
	 * @param event Arbitrary integer to represent the event, choosing the values is up to you
	 * @param limitRange Limit the notification range to (currently) 20 blocks instead of the
	 *        tracking distance if true
	 */
	public static void initiateItemEvent(EntityPlayer player, ItemStack stack, int event, boolean limitRange) {
		getNetworkManager(FMLCommonHandler.instance().getEffectiveSide()).initiateItemEvent(player, stack, event, limitRange);
	}

	/**
	 * Send initial TileEntity data to the clients. Requires the te to implement
	 * {@link INetworkDataProvider}.
	 * @param te The te to send the initial data for.
	 */
	public static void sendInitialData(TileEntity te) {
		getNetworkManager(FMLCommonHandler.instance().getEffectiveSide()).sendInitialData(te);
	}


	// client -> server

	/**
	 * Immediately send an event for the specified TileEntity to the server.
	 *
	 * This method doesn't do anything if executed on the server.
	 *
	 * @param te TileEntity to notify, should implement INetworkClientTileEntityEventListener
	 * @param event Arbitrary integer to represent the event, choosing the values is up to you
	 */
	public static void initiateClientTileEntityEvent(TileEntity te, int event) {
		getNetworkManager(FMLCommonHandler.instance().getEffectiveSide()).initiateClientTileEntityEvent(te, event);
	}

	/**
	 * Immediately send an event for the specified Item to the clients in range.
	 *
	 * The item should implement INetworkItemEventListener to receive the event.
	 *
	 * This method doesn't do anything if executed on the server.
	 *
	 * @param stack ItemStack containing the item
	 * @param event Arbitrary integer to represent the event, choosing the values is up to you
	 */
	public static void initiateClientItemEvent(ItemStack stack, int event) {
		getNetworkManager(FMLCommonHandler.instance().getEffectiveSide()).initiateClientItemEvent(stack, event);
	}

	/**
	 * This will return the NetworkManager for the given side.
	 * @param side the side to get the NetworkManager for.
	 * @return The NetworkManager for the given side.
	 */
	public static INetworkManager getNetworkManager(Side side) {
		if (side.isClient()) {
			return clientInstance;
		}
		else {
			return serverInstance;
		}
	}

	private static INetworkManager serverInstance;
	private static INetworkManager clientInstance;

	/**
	 * Sets the internal INetworkManager instance.
	 * ONLY IC2 CAN DO THIS!!!!!!!
	 */
	public static void setInstance(INetworkManager server, INetworkManager client) {
		ModContainer mc = Loader.instance().activeModContainer();
		if (mc == null || !Info.MOD_ID.equals(mc.getModId())) {
			throw new IllegalAccessError();
		}
		serverInstance = server;
		clientInstance = client;
	}
}

