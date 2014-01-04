package codechicken.multipart

/**
 * Mixin interface for parts that want to be notified of neighbor tile change events (comparators or inventory maintainers)
 */
trait INeighborTileChange {
    /**
     * Returns whether this part needs calls for tile changes through one solid block
     */
    def weakTileChanges():Boolean
    /**
     * Callback for neighbor tile changes, from same function in Block
     */
    def onNeighborTileChanged(side:Int, weak:Boolean)
}