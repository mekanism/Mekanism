package ic2.api.network;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;

/**
 * This is just here for the API functions.
 * Do not implement by yourself.
 *
 * @see NetworkHelper
 *
 * @author Aroma1997
 *
 */
public interface INetworkManager {

	void updateTileEntityField(TileEntity te, String field);

	void initiateTileEntityEvent(TileEntity te, int event, boolean limitRange);

	void initiateItemEvent(EntityPlayer player, ItemStack stack, int event, boolean limitRange);

	void initiateClientTileEntityEvent(TileEntity te, int event);

	void initiateClientItemEvent(ItemStack stack, int event);

	void sendInitialData(TileEntity te);

}
