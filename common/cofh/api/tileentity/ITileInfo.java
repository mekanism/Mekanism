package cofh.api.tileentity;

import java.util.List;

import net.minecraft.entity.player.EntityPlayer;

/**
 * Implement this interface on Tile Entities which can send state information through chat.
 * 
 * @author Zeldo Kavira
 * 
 */
public interface ITileInfo {

	public List<String> getTileInfo();

	public void sendTileInfoToPlayer(EntityPlayer player);

}
