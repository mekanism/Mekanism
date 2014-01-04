package codechicken.multipart

import codechicken.lib.vec.Cuboid6
import scala.collection.JavaConversions._
import java.lang.Iterable

/**
 * This suite of 3 classes provides simple functions for standard bounding box based occlusion testing.
 * If any two parts have overlapping bounding boxes, the test fails
 * 
 * See TIconHitEffects for notes on the Scala|Java composition setup.
 */
object NormalOcclusionTest
{
    /**
     * Performs the test, returns true if the test fails
     */
    def apply(boxes1:Traversable[Cuboid6], boxes2:Traversable[Cuboid6]):Boolean =
        boxes1.forall(v1 => boxes2.forall(v2 => !v1.intersects(v2)))
    
    /**
     * Performs the test, returns true if the test fails
     */
    def apply(part1:JNormalOcclusion, part2:TMultiPart):Boolean = 
    {
        var boxes = Seq[Cuboid6]()
        if(part2.isInstanceOf[JNormalOcclusion])
            boxes = boxes++part2.asInstanceOf[JNormalOcclusion].getOcclusionBoxes
        
        if(part2.isInstanceOf[JPartialOcclusion])
            boxes = boxes++part2.asInstanceOf[JPartialOcclusion].getPartialOcclusionBoxes

        return NormalOcclusionTest(boxes, part1.getOcclusionBoxes) 
    }
}

/**
 * Java interface containing callbacks for normal occlusion testing.
 * Make sure to override occlusionTest as in TNormalOcclusion
 */
trait JNormalOcclusion
{
    /**
     * Return a list of normal occlusion boxes
     */
    def getOcclusionBoxes:Iterable[Cuboid6]
}

/**
 * Trait for scala programmers
 */
trait TNormalOcclusion extends TMultiPart with JNormalOcclusion
{
    override def occlusionTest(npart:TMultiPart):Boolean =
        NormalOcclusionTest(this, npart) && super.occlusionTest(npart)
}

/**
 * Utility part class for performing 3rd party occlusion tests
 */
class NormallyOccludedPart(bounds:Iterable[Cuboid6]) extends TMultiPart with TNormalOcclusion
{
    def this(bound:Cuboid6) = this(Seq(bound))
    
    def getType = null
    
    def getOcclusionBoxes = bounds
}