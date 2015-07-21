/**
 * Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.api.core;

import io.netty.buffer.ByteBuf;

/**
 * Implemented by classes representing serializable packet state
 */
public interface ISerializable {
	/**
	 * Serializes the state to the stream
	 *
	 * @param data
	 */
	void writeData(ByteBuf data);

	/**
	 * Deserializes the state from the stream
	 *
	 * @param data
	 */
	void readData(ByteBuf data);
}
