/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 *
 * The BuildCraft API is distributed under the terms of the MIT License. Please check the contents of the license, which
 * should be located as "LICENSE.API" in the BuildCraft source code distribution. */
package buildcraft.api.boards;

import java.util.Collection;

import net.minecraft.nbt.NBTTagCompound;

public abstract class RedstoneBoardRegistry {

    public static RedstoneBoardRegistry instance;

    /** Register a redstone board type.
     * 
     * @param redstoneBoardNBT The RedstoneBoardNBT instance containing the board information.
     * @param energyCost RF price of the board, in RF. */
    public abstract void registerBoardType(RedstoneBoardNBT<?> redstoneBoardNBT, int energyCost);

    @Deprecated
    /** Register a redstone board type. Deprecated, use registerBoardType now.
     * 
     * @param redstoneBoardNBT The RedstoneBoardNBT instance containing the board information.
     * @param probability Since 6.4.x, this is used to calculate the RF price of the board in the Programming Table by
     *            doing (160000 / probability). */
    public abstract void registerBoardClass(RedstoneBoardNBT<?> redstoneBoardNBT, float probability);

    public abstract void setEmptyRobotBoard(RedstoneBoardRobotNBT redstoneBoardNBT);

    public abstract RedstoneBoardRobotNBT getEmptyRobotBoard();

    public abstract RedstoneBoardNBT<?> getRedstoneBoard(NBTTagCompound nbt);

    public abstract RedstoneBoardNBT<?> getRedstoneBoard(String id);

    public abstract Collection<RedstoneBoardNBT<?>> getAllBoardNBTs();

    public abstract int getEnergyCost(RedstoneBoardNBT<?> board);
}
