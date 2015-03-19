package mekanism.common.base;

import java.io.IOException;

import mekanism.common.Version;

import io.netty.buffer.ByteBuf;

/**
 * Implement in your main class if your mod happens to be completely reliant on Mekanism, or in other words, is a Mekanism module.
 * @author aidancbrady
 *
 */
public interface IModule
{
	/**
	 * Gets the version of the module.
	 * @return the module's version
	 */
	public Version getVersion();

	/**
	 * Gets the name of the module.  Note that this doesn't include "Mekanism" like the actual module's name does, just the
	 * unique name.  For example, MekanismGenerators returns "Generators" here.
	 * @return unique name of the module
	 */
	public String getName();

	/**
	 * Writes this module's configuration to a ConfigSync packet.
	 * @param dataStream - the ByteBuf of the sync packet
	 */
	public void writeConfig(ByteBuf dataStream) throws IOException;

	/**
	 * Reads this module's configuration from the original ConfigSync packet.
	 * @param dataStream - the incoming ByteBuf of the sync packet
	 */
	public void readConfig(ByteBuf dataStream) throws IOException;
}
