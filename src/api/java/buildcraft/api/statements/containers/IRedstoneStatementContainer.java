package buildcraft.api.statements.containers;

import net.minecraftforge.common.util.ForgeDirection;

public interface IRedstoneStatementContainer {
	/**
	 * Get the redstone input from a given side.
	 * @param side The side - use "UNKNOWN" for maximum input.
	 * @return The redstone input, from 0 to 15.
	 */
	int getRedstoneInput(ForgeDirection side);

	/**
	 * Set the redstone input for a given side.
	 * @param side The side - use "UNKNOWN" for all sides.
	 * @return Whether the set was successful.
	 */
	boolean setRedstoneOutput(ForgeDirection side, int value);
}
