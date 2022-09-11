package cofh.api.energy;

import net.minecraftforge.common.util.ForgeDirection;

/**
 * Implement this interface on Tile Entities which transport energy.
 * <p>
 * This is used to "negotiate" connection types between two separate IEnergyTransports, allowing users to set flow direction and allowing for networks Of
 * IEnergyTransports to intelligently transfer energy to other networks.
 */
public interface IEnergyTransport extends IEnergyProvider, IEnergyReceiver {

	/**
	 * The type of interface for a given side of a {@link IEnergyTransport}.
	 * <p>
	 * Values are:<br>
	 * {@link SEND} for sending only<br>
	 * {@link RECEIVE} for receiving only<br>
	 * {@link BALANCE} for sending and receiving, and the default state
	 */
	public enum InterfaceType {
		/**
		 * Indicates that this {@link IEnergyTransport} is only sending power on this side.
		 */
		SEND,
		/**
		 * Indicates that this {@link IEnergyTransport} is only receiving power on this side.
		 */
		RECEIVE,
		/**
		 * Indicates that this {@link IEnergyTransport} wants to balance power between itself and the
		 * senders/receivers on this side. This is the default state.<br>
		 * To block any connection, use {@link IEnergyConnection#canConnectEnergy}
		 * <p>
		 * IEnergyTransport based senders should check that the total power in the destination IEnergyTransport is less than the power in themselves before sending.
		 * <br>
		 * Active IEnergyTransport receivers (i.e., those that call {@link IEnergyProvider#extractEnergy}) should check that they contain less power than the
		 * source IEnergyTransport.
		 */
		BALANCE;

		/**
		 * Returns the opposite state to this InterfaceType.
		 * <p>
		 * {@link #BALANCE} is considered its own opposite.<br>
		 * {@link #SEND} is the opposite of {@link #RECEIVE} and visa versa.
		 */
		public InterfaceType getOpposite() {

			return this == BALANCE ? BALANCE : this == SEND ? RECEIVE : SEND;
		}

		/**
		 * Returns the next InterfaceType as described in {@link IEnergyTransport#getTransportState}
		 */
		public InterfaceType rotate() {

			return rotate(true);
		}

		/**
		 * Returns the next InterfaceType as described in {@link IEnergyTransport#getTransportState}
		 *
		 * @param forward
		 *            Whether to step in the order specified by {@link IEnergyTransport#getTransportState} (<tt>true</tt>) or to step in the opposite direction
		 */
		public InterfaceType rotate(boolean forward) {

			if (forward) {
				return this == BALANCE ? RECEIVE : this == RECEIVE ? SEND : BALANCE;
			} else {
				return this == BALANCE ? SEND : this == SEND ? RECEIVE : BALANCE;
			}
		}
	}

	/**
	 * {@inheritDoc}<br>
	 * This method <b>cannot</b> be a no-op for IEnergyTransport.
	 */
	@Override
	int getEnergyStored(ForgeDirection from);

	/**
	 * Indicates to other IEnergyTransports the state of the given side. See {@link #InterfaceType} for details.
	 * <p>
	 * For clarity of state tracking, on a tile update from another IEnergyTransport, if its mode has changed from the opposite of your own mode on that side, you
	 * should change your mode to the opposite of its mode.
	 * <p>
	 * When the user alters your mode and your state is:<br>
	 * BALANCE, your mode should change to {@link InterFaceType#RECEIVE}.<br>
	 * RECEIVE, your mode should change to {@link InterFaceType#SEND}.<br>
	 * SEND, your mode should change to {@link InterFaceType#BALANCE}.<br>
	 * This is not required, but will be easier for users.
	 *
	 * @return The type of connection to establish on this side. <b>null is NOT a valid value</b>
	 */
	InterfaceType getTransportState(ForgeDirection from);

	/**
	 * This method is provided primarily for the purposes of automation tools, and should not need to be called by another IEnergyTransport.
	 * <p>
	 * Calls to this method may fail if this IEnergyTransport has been secured by a user.
	 *
	 * @return Whether or not state was successfully altered.
	 */
	boolean setTransportState(InterfaceType state, ForgeDirection from);

}
