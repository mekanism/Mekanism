package codechicken.multipart

import scala.collection.mutable.{Map => MMap}
import codechicken.lib.data.MCDataOutput
import codechicken.lib.data.MCDataInput

/**
 * Static class for packing update data.
 * When a specific property of a part changes and needs sending to the client, a bit can be set in the mask.
 * This bit can then be checked in the writeScheduled callback.
 * This prevents sending multiple packets if the same property updates more than once per tick.
 */
object PacketScheduler
{
    private val map = MMap[TMultiPart, Long]()
    
    /**
     * Add bits to the current update mask for part. (binary OR)
     */
    def schedulePacket(part:TMultiPart, mask:Long) = {//TODO remove = in 1.7
        if(part.world.isRemote)
            throw new IllegalArgumentException("Cannot use PacketScheduler on a client world")

        map.put(part, map.getOrElse(part, 0L)|mask)
    }

    private[multipart] def sendScheduled() {
        map.foreach{ e =>
            val (part, mask) = e
            if(part.tile != null) {
                val ipart = part.asInstanceOf[IScheduledPacketPart]
                val w = part.getWriteStream
                ipart.maskWidth match {
                    case 1 => w.writeByte(mask.toInt)
                    case 2 => w.writeShort(mask.toInt)
                    case 4 => w.writeInt(mask.toInt)
                    case 8 => w.writeLong(mask)
                }
                
                ipart.writeScheduled(mask, w)
            }
        }
        map.clear()
    }
}

/**
 * Callback interface for PacketScheduler
 */
trait IScheduledPacketPart
{
    /**
     * Write scheduled data to the packet, mask is the cumulative mask from calls to schedulePacket
     */
    def writeScheduled(mask:Long, packet:MCDataOutput)
    
    /**
     * Returns the width (in bytes) of the data type required to hold all valid mask bits. Valid values are 1, 2, 4 and 8
     */
    def maskWidth:Int
    
    /**
     * Read data matching mask. Estiablishes a method for subclasses to override. This should be called from read
     */
    def readScheduled(mask:Long, packet:MCDataInput)
}

trait TScheduledPacketPart extends TMultiPart with IScheduledPacketPart
{
    final override def read(packet:MCDataInput) {
        val mask = maskWidth match {
            case 1 => packet.readUByte
            case 2 => packet.readUShort
            case 4 => packet.readInt
            case 8 => packet.readLong
        }
        readScheduled(mask, packet)
    }
    
    def writeScheduled(mask:Long, packet:MCDataOutput){}
    def readScheduled(mask:Long, packet:MCDataInput){}
}