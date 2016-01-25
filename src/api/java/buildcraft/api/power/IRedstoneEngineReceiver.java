/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 *
 * The BuildCraft API is distributed under the terms of the MIT License. Please check the contents of the license, which
 * should be located as "LICENSE.API" in the BuildCraft source code distribution. */
package buildcraft.api.power;

import net.minecraft.util.EnumFacing;

import cofh.api.energy.IEnergyReceiver;

/** Implement this on tiles that you wish to be able to receive Redstone Engine (low-power) energy.
 *
 * Please do not implement it on batteries, pipes or machines which can have their energy extracted from. That could
 * lead to exploits and abuse. */
public interface IRedstoneEngineReceiver extends IEnergyReceiver {
    /** This function is queried on every attempt to receive energy from a redstone engine as well.
     * 
     * @param side
     * @return */
    boolean canConnectRedstoneEngine(EnumFacing side);
}
