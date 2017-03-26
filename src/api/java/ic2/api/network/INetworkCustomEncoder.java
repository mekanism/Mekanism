package ic2.api.network;

import java.io.IOException;

/**
 * Allow extensions to {@link ic2.core.network.DataEncoder} for encoding custom data types not already handled by IC2.
 * Register this to DataEncoder itself using #addNetworkEncoder(Class, INetworkCustomEncoder).
 *
 * @author Chocohead
 */
public interface INetworkCustomEncoder {
	/**
	 * Encode the given instance into the buffer
	 *
	 * @param buffer The network buffer
	 * @param instance The instance being encoded
	 *
	 * @throws IOException Because {@link ic2.core.network.DataEncoder#encode(IGrowingBuffer, Object, boolean)} does.
	 */
	void encode(IGrowingBuffer buffer, Object instance) throws IOException;

	/**
	 * @param buffer The network buffer
	 *
	 * @return The instance the information on the network represents
	 *
	 * @throws IOException Because {@link ic2.core.network.DataEncoder#decode(IGrowingBuffer, ic2.core.network.DataEncoder.EncodedType)} does.
	 */
	Object decode(IGrowingBuffer buffer) throws IOException;

	/**
	 * @return <code>false</code> the type accesses the world or otherwise requires to be run on the main thread,
	 * 			<code>true</code> if it doesn't.
	 */
	boolean isThreadSafe();
}