package codechicken.multipart

import java.util.Random

/**
 * Interface for parts that need random display ticks (torches)
 * 
 * Marker interface for TRandomDisplayTickTile
 */
trait IRandomDisplayTick
{
    /**
     * Called on a random display tick.
     */
    def randomDisplayTick(random:Random)
}