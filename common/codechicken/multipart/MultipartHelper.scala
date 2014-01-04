package codechicken.multipart

import net.minecraft.tileentity.TileEntity
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.world.World
import codechicken.multipart.handler.MultipartSaveLoad
import com.google.common.collect.LinkedListMultimap
import scala.collection.JavaConversions._
import codechicken.multipart.handler.MultipartSPH
import net.minecraft.world.WorldServer
import java.util.Arrays
import net.minecraft.server.management.PlayerInstance
import codechicken.lib.asm.ObfMapping

/**
 * Static helper class for handling the unusual way that multipart tile entities load from nbt and send description packets
 * <br>
 * Multipart tile entities will all save themselves with the id "savedMultipart" which if normally loaded by minecraft, 
 * will create a dummy tile entity which just holds the NBT it was read from. These dummies are then converted to actual container tiles on the ChunkLoad event.
 * The createTileFromNBT function should be used to construct a multipart tile from NBT without the ChunkLoad event.
 * <br>
 * Multipart tile entities do not send description packets via the conventional means of one packet per tile when PlayerInstance calls for it, to do so would be terribly inefficient.
 * Instead, the ChunkWatch event is used to batch all the describing data for a chunk into one packet which is compressed using relative positions.
 * The sendDescPacket(s) functions should be used to send the description packet of a tile or tiles without a ChunkWatch event.
 * <br>
 * An example of using this class to move blocks/tile entites around can be found at www.chickenbones.craftsaddle.org/Files/Other/ItemDevTool2.java
 */
object MultipartHelper
{
    val f_playersInChunk = classOf[PlayerInstance].getDeclaredField(
                new ObfMapping("net/minecraft/server/management/PlayerInstance", "playersInChunk", "Ljava/util/List;")
                .toRuntime.s_name)
    f_playersInChunk.setAccessible(true)
    
    def playersInChunk(inst:PlayerInstance) = f_playersInChunk.get(inst).asInstanceOf[List[_]]
    
    def createTileFromNBT(world:World, tag:NBTTagCompound):TileEntity = {
        if(!tag.getString("id").equals("savedMultipart"))
            return null
        
        MultipartSaveLoad.loadingWorld = world
        return TileMultipart.createFromNBT(tag)
    }
    
    /**
     * Note. This method should only be used to send tiles that have been created on the server mid-game via an NBT load to clients.
     */
    def sendDescPacket(world:World, tile:TileEntity) {
        val c = world.getChunkFromBlockCoords(tile.xCoord, tile.zCoord)
        val pkt = MultipartSPH.getDescPacket(c, Arrays.asList(tile).iterator)
        if(pkt != null)
            pkt.sendToChunk(world, c.xPosition, c.zPosition)
    }
    
    def sendDescPackets(world:World, tiles:Iterable[TileEntity]) {
        val map = LinkedListMultimap.create[Long, TileEntity]()
        tiles.filter(_.isInstanceOf[TileMultipart]).foreach(t => map.put(t.xCoord.toLong<<32|t.zCoord, t))
        
        val mgr = world.asInstanceOf[WorldServer].getPlayerManager
        map.asMap.entrySet.foreach{e => 
            val coord = e.getKey
            val c = world.getChunkFromBlockCoords((coord>>32).toInt, coord.toInt)
            lazy val pkt = MultipartSPH.getDescPacket(c, e.getValue.iterator)
            val inst = mgr.getOrCreateChunkWatcher(c.xPosition, c.zPosition, false)
            if(!playersInChunk(inst).isEmpty)
                inst.sendToAllPlayersWatchingChunk(pkt.toPacket)
        }
    }
}