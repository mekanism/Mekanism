package codechicken.multipart

/**
 * Interface which must be implemented by parts that go in an edge slot.
 */
trait TEdgePart extends TSlottedPart
{
    /**
     * Return true if this part can conduct redstone signal or let redstone signal pass through it.
     */
    def conductsRedstone = false
}