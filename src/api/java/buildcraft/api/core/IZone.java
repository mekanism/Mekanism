/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 *
 * The BuildCraft API is distributed under the terms of the MIT License. Please check the contents of the license, which
 * should be located as "LICENSE.API" in the BuildCraft source code distribution. */
package buildcraft.api.core;

import java.util.Random;

import net.minecraft.util.BlockPos;
import net.minecraft.util.Vec3;

public interface IZone {
    /** Returns the smallest possible distance that the index would have to be changed by in order for
     * {@link #contains(Vec3)} to return true. If the position is already inside then this will return 0 */
    double distanceTo(BlockPos index);

    /** Returns {@link #distanceTo(BlockPos)} but squared. Usually this will be quicker to calculate. */
    double distanceToSquared(BlockPos index);

    /** Returns true if the point is enclosed by this zone, such that none of the coordinates lie outside the range
     * specified by this zone. */
    boolean contains(Vec3 point);

    /** Gets a random position that {@link #contains(Vec3)} will return true. */
    BlockPos getRandomBlockPos(Random rand);
}
