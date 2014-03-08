package mekanism.common.network;

import java.io.DataOutputStream;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;

import com.google.common.io.ByteArrayDataInput;

public interface IMekanismPacket
{
	/**
	 * Gets this packet's identifier.  This will show up in console logs to specify packet types.
	 * @return this packet's identifier
	 */
	public String getName();

	/**
	 * Sets the parameters of this packet for writing.
	 * @param data - data to set
	 */
	public IMekanismPacket setParams(Object... data);

	/**
	 * Reads this packet's data from a ByteArrayDataInput, and handles it as needed.
	 * @param dataStream - data stream being sent
	 * @param player - player this packet was sent to, or sent by
	 * @param world - world this packet was handled in
	 * @throws Exception - the exception thrown in case anything goes wrong
	 */
	public void read(ByteArrayDataInput dataStream, EntityPlayer player, World world) throws Exception;

	/**
	 * Writes this packet's data to a DataOutputStream.
	 * @param dataStream - data stream being sent
	 * @throws Exception - the exception thrown in case anything goes wrong
	 */
	public void write(DataOutputStream dataStream) throws Exception;
}
