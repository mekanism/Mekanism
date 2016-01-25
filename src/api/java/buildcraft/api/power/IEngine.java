/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 *
 * The BuildCraft API is distributed under the terms of the MIT License. Please check the contents of the license, which
 * should be located as "LICENSE.API" in the BuildCraft source code distribution. */
package buildcraft.api.power;

import net.minecraft.util.EnumFacing;

/** Engines should implement this interface if they want to support BuildCraft's behaviour of passing energy between
 * engines without using receiveEnergy() (which has other issues). */
public interface IEngine {
    /** Returns true if the engine wants to receive power from another engine on this side.
     * 
     * @param side
     * @return */
    boolean canReceiveFromEngine(EnumFacing side);

    /** Receives energy from an engine. See
     * {@link cofh.api.energy.IEnergyHandler#receiveEnergy(EnumFacing, int, boolean)}
     * 
     * @param side The side the engine is receiving energy from.
     * @param energy The amount of energy given to the engine.
     * @param simulate True if the energy should not actually be added.
     * @return The amount of energy used by the engine. */
    int receiveEnergyFromEngine(EnumFacing side, int energy, boolean simulate);
}
