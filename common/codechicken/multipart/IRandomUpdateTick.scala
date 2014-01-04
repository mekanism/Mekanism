package codechicken.multipart

/**
 * Interface for parts with random update ticks.
 */
trait IRandomUpdateTick
{
    /**
     * Called on random update. Random ticks are between 800 and 1600 ticks from their last scheduled/random tick
     */
    def randomUpdate()
}