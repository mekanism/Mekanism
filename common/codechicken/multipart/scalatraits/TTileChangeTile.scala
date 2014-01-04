package codechicken.multipart.scalatraits

import codechicken.multipart.TMultiPart
import codechicken.multipart.INeighborTileChange
import codechicken.multipart.TileMultipart
import codechicken.lib.vec.BlockCoord

/**
 * Mixin implementation for INeighborTileChange
 * 
 * Reduces unnecessary computation
 */
trait TTileChangeTile extends TileMultipart {
    var weakTileChanges = false
    
    override def copyFrom(that:TileMultipart)
    {
        super.copyFrom(that)
        if(that.isInstanceOf[TTileChangeTile])
            weakTileChanges = that.asInstanceOf[TTileChangeTile].weakTileChanges
    }
    
    override def bindPart(part:TMultiPart)
    {
        super.bindPart(part)
        if(part.isInstanceOf[INeighborTileChange])
            weakTileChanges|=part.asInstanceOf[INeighborTileChange].weakTileChanges
    }
    
    override def clearParts()
    {
        super.clearParts()
        weakTileChanges = false
    }
    
    override def partRemoved(part:TMultiPart, p:Int) {
        super.partRemoved(part, p)
        weakTileChanges = partList.exists(p => p.isInstanceOf[INeighborTileChange] && p.asInstanceOf[INeighborTileChange].weakTileChanges)
    }
    
    override def onNeighborTileChange(tileX:Int, tileY:Int, tileZ:Int)
    {
        super.onNeighborTileChange(tileX, tileY, tileZ)
        val offset = new BlockCoord(tileX, tileY, tileZ).sub(xCoord, yCoord, zCoord)
        val diff = offset.absSum
        val side = offset.toSide
        
        if(side < 0 || diff <= 0 || diff > 2)
            return
            
        val weak = diff == 2
        if(weak && !weakTileChanges)
            return
        
        operate{ p => 
            if(p.isInstanceOf[INeighborTileChange])
                p.asInstanceOf[INeighborTileChange].onNeighborTileChanged(side, weak)
        }
    }
}