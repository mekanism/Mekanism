package codechicken.multipart

/**
 * Inteface which must be implemented by parts that go in a face part.
 */
trait TFacePart extends TSlottedPart
{
    /**
     * Passed down from Block.isBlockSolidOnSide. Return true if this part is solid and opaque on the specified side
     */
    def solid(side:Int):Boolean = true
    /**
     * Return the redstone conduction map for which signal can pass through this part on the face. Eg, hollow covers return 0xF as signal can pass through the center hole.
     */
    def redstoneConductionMap = 0
}