package codechicken.multipart.scalatraits

import codechicken.multipart.TileMultipartClient
import codechicken.multipart.IRandomDisplayTick
import java.util.Random

/**
 * Saves processor time looping on tiles that don't need it
 */
trait TRandomDisplayTickTile extends TileMultipartClient
{
    override def randomDisplayTick(random:Random)
    {
        for(p@(_p: IRandomDisplayTick) <- partList.iterator)
            p.randomDisplayTick(random)
    }
}