package buildcraft.api.statements.containers;

import net.minecraft.util.EnumFacing;

public interface IRedstoneStatementContainer {
    /** Get the redstone input from a given side.
     * 
     * @param side The side - use "UNKNOWN" for maximum input.
     * @return The redstone input, from 0 to 15. */
    int getRedstoneInput(EnumFacing side);

    /** Set the redstone input for a given side.
     * 
     * @param side The side - use "UNKNOWN" for all sides.
     * @return Whether the set was successful. */
    boolean setRedstoneOutput(EnumFacing side, int value);
}
