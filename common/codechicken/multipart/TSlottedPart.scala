package codechicken.multipart

/**
 * Interface for parts that fill a slot based configuration as defined in PartMap.
 * If this is implemented, calling partMap(slot) on the host tile will return this part if the corresponding bit in the slotMask is set
 * 
 * Marker interface for TSlottedTile
 */
trait TSlottedPart extends TMultiPart
{
    /**
     * a bitmask of slots that this part fills. slot x is 1<<x
     */
    def getSlotMask:Int
}