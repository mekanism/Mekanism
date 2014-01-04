package codechicken.multipart.scalatraits

import codechicken.multipart.TileMultipart
import codechicken.multipart.TMultiPart
import codechicken.multipart.TSlottedPart

/**
 * Mixin implementation for TSlottedPart.
 * Puts parts into a slot array for quick access at the cost of memory consumption
 */
trait TSlottedTile extends TileMultipart
{
    var v_partMap = new Array[TMultiPart](27)
    
    override def copyFrom(that:TileMultipart)
    {
        super.copyFrom(that)
        if(that.isInstanceOf[TSlottedTile])
            v_partMap = that.asInstanceOf[TSlottedTile].v_partMap
    }
    
    override def partMap(slot:Int) = v_partMap(slot)
    
    override def clearParts()
    {
        super.clearParts()
        for(i <- 0 until v_partMap.length)
            v_partMap(i) = null
    }
    
    override def partRemoved(part:TMultiPart, p:Int)
    {
        super.partRemoved(part, p)
        if(part.isInstanceOf[TSlottedPart])
            for(i <- 0 until 27)
                if(partMap(i) == part)
                    v_partMap(i) = null
    }
    
    override def canAddPart(part:TMultiPart):Boolean =
    {
        if(part.isInstanceOf[TSlottedPart])
        {
            val slotMask = part.asInstanceOf[TSlottedPart].getSlotMask
            for(i <- 0 until v_partMap.length)
                if((slotMask&1<<i) != 0 && partMap(i) != null)
                    return false
        }
        
        return super.canAddPart(part)
    }
    
    override def bindPart(part:TMultiPart)
    {
        super.bindPart(part)
        if(part.isInstanceOf[TSlottedPart])
        {
            val mask = part.asInstanceOf[TSlottedPart].getSlotMask
            for(i <- 0 until 27)
                if ((mask&1<<i) > 0)
                    v_partMap(i) = part
        }
    }
}