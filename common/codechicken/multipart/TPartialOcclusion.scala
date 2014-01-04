package codechicken.multipart

import codechicken.lib.vec.Cuboid6
import scala.collection.JavaConversions._
import java.lang.Iterable

/**
 * This class provides a special type of occlusion model used by microblocks.
 * The partial occlusion test defines bounding boxes that may intersect, so long as no part is completely obscured by a combination of the others.
 * Partial bounding boxes may not intersect with normal bounding boxes from NormalOcclusionTest
 * 
 * This test is actually managed by the mixin trait TPartialOcclusionTile which is generated when the marker interface JPartialOcclusion is found
 */
class PartialOcclusionTest(size:Int)
{
    /**
     * The resolution of the test, set to 1/8th of a block
     */
    val res = 8
    val bits = new Array[Byte](res*res*res)
    val partial = new Array[Boolean](size)
    
    def fill(i:Int, part:JPartialOcclusion)
    {
        fill(i, part.getPartialOcclusionBoxes, part.allowCompleteOcclusion)
    }
    
    def fill(i:Int, boxes:Iterable[Cuboid6], complete:Boolean)
    {
        partial(i) = !complete
        boxes.foreach(box => fill(i+1, box))
    }
    
    def fill(v:Int, box:Cuboid6)
    {
        for(x <- (box.min.x*res+0.5).toInt until (box.max.x*res+0.5).toInt)
            for(y <- (box.min.y*res+0.5).toInt until (box.max.y*res+0.5).toInt)
                for(z <- (box.min.z*res+0.5).toInt until (box.max.z*res+0.5).toInt)
                {
                    val i = (x*res+y)*res+z
                    if(bits(i) == 0)
                        bits(i) = v.toByte
                    else
                        bits(i) = -1
                }
    }
    
    def apply():Boolean =
    {
        val visible = new Array[Boolean](size)
        bits.foreach(n => if(n > 0) visible(n-1) = true)
        
        var i = 0
        while(i < partial.length)
        {
            if(partial(i) && !visible(i))
                return false
            i+=1
        }
        
        return true
    }
}

trait JPartialOcclusion
{
    /**
     * Return a list of partial occlusion boxes
     */
    def getPartialOcclusionBoxes:Iterable[Cuboid6]
    
    /**
     * Return true if this part may be completely obscured
     */
    def allowCompleteOcclusion = false
}