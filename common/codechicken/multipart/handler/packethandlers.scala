package codechicken.multipart.handler

import codechicken.lib.packet.PacketCustom.IClientPacketHandler
import codechicken.lib.packet.PacketCustom.IServerPacketHandler
import codechicken.lib.packet.PacketCustom
import net.minecraft.client.multiplayer.NetClientHandler
import net.minecraft.client.Minecraft
import codechicken.multipart.MultiPartRegistry
import net.minecraft.network.NetServerHandler
import net.minecraft.entity.player.EntityPlayerMP
import codechicken.multipart.ControlKeyModifer
import net.minecraft.network.packet.Packet255KickDisconnect
import net.minecraft.world.World
import net.minecraft.world.chunk.Chunk
import java.util.Map
import java.util.Iterator
import net.minecraft.tileentity.TileEntity
import codechicken.multipart.TileMultipart
import net.minecraft.entity.player.EntityPlayer
import scala.collection.mutable.{Map => MMap}
import codechicken.lib.vec.BlockCoord
import codechicken.lib.data.MCOutputStreamWrapper
import java.io.DataOutputStream
import java.io.ByteArrayOutputStream
import net.minecraft.world.WorldServer
import net.minecraft.world.ChunkCoordIntPair
import MultipartProxy._
import codechicken.multipart.PacketScheduler

class MultipartPH
{
    val channel = MultipartMod
    val registryChannel = "ForgeMultipart"//Must use the 250 system for ID registry as the NetworkMod idMap hasn't been properly initialized from the server yet.
}

object MultipartCPH extends MultipartPH with IClientPacketHandler
{
    def handlePacket(packet:PacketCustom, netHandler:NetClientHandler, mc:Minecraft)
    {
        packet.getType match
        {
            case 1 => handlePartRegistration(packet, netHandler)
            case 2 => handleCompressedTileDesc(packet, mc.theWorld)
            case 3 => handleCompressedTileData(packet, mc.theWorld)
        }
    }
    
    def handlePartRegistration(packet:PacketCustom, netHandler:NetClientHandler)
    {
        val missing = MultiPartRegistry.readIDMap(packet)
        if(!missing.isEmpty)
            netHandler.handleKickDisconnect(new Packet255KickDisconnect("The following multiparts are not installed on this client: "+missing.mkString(", ")))
    }
    
    def handleCompressedTileDesc(packet:PacketCustom, world:World)
    {
        val cc = new ChunkCoordIntPair(packet.readInt, packet.readInt)
        val num = packet.readUShort
        for(i <- 0 until num)
            TileMultipart.handleDescPacket(world, indexInChunk(cc, packet.readShort), packet)
    }
    
    def handleCompressedTileData(packet:PacketCustom, world:World)
    {
        var x = packet.readInt
        while(x != Int.MaxValue)
        {
            val pos = new BlockCoord(x, packet.readInt, packet.readInt)
            var i = packet.readUByte
            while(i < 255)
            {
                TileMultipart.handlePacket(pos, world, i, packet)
                i = packet.readUByte
            }
            x = packet.readInt
        }
        TileMultipart.flushClientCache()
    }
}

object MultipartSPH extends MultipartPH with IServerPacketHandler
{
    class MCByteStream(bout:ByteArrayOutputStream) extends MCOutputStreamWrapper(new DataOutputStream(bout))
    {
        def getBytes = bout.toByteArray
    }
    
    private val updateMap = MMap[World, MMap[BlockCoord, MCByteStream]]()
    
    def handlePacket(packet:PacketCustom, netHandler:NetServerHandler, sender:EntityPlayerMP)
    {
        packet.getType match
        {
            case 1 => ControlKeyModifer.map.put(sender, packet.readBoolean)
        }
    }
    
    def onWorldUnload(world:World)
    {
        if(!world.isRemote)
            updateMap.remove(world)
    }
    
    def getTileStream(world:World, pos:BlockCoord) =
        updateMap.getOrElseUpdate(world, {
            if(world.isRemote)
                throw new IllegalArgumentException("Cannot use MultipartSPH on a client world")
            MMap()
        }).getOrElseUpdate(pos, {
            val s = new MCByteStream(new ByteArrayOutputStream)
            s.writeCoord(pos)
            s
        })
    
    def onTickEnd(players:Seq[EntityPlayerMP])
    {
        PacketScheduler.sendScheduled()
        
        players.foreach{p =>
            val m = updateMap.getOrElse(p.worldObj, null)
            if(m != null && !m.isEmpty)
            {
                val manager = p.worldObj.asInstanceOf[WorldServer].getPlayerManager
                val packet = new PacketCustom(channel, 3).setChunkDataPacket().compressed()
                var send = false
                m.foreach(e => 
                    if(manager.isPlayerWatchingChunk(p, e._1.x>>4, e._1.z>>4))
                    {
                        send = true
                        packet.writeByteArray(e._2.getBytes)
                        packet.writeByte(255)//terminator
                    })
                if(send)
                {
                    packet.writeInt(Int.MaxValue)//terminator
                    packet.sendToPlayer(p)
                }
            }
        }
        updateMap.foreach(_._2.clear())
    }
    
    def onChunkWatch(player:EntityPlayer, chunk:Chunk)
    {
        val pkt = getDescPacket(chunk, chunk.chunkTileEntityMap.asInstanceOf[Map[_, TileEntity]].values.iterator)
        if(pkt != null)
            pkt.sendToPlayer(player)
    }
    
    def getDescPacket(chunk:Chunk, it:Iterator[TileEntity]):PacketCustom =
    {
        val s = new MCByteStream(new ByteArrayOutputStream)
        
        var num = 0
        while(it.hasNext)
        {
            val tile = it.next
            if(tile.isInstanceOf[TileMultipart])
            {
                s.writeShort(indexInChunk(new BlockCoord(tile)))
                tile.asInstanceOf[TileMultipart].writeDesc(s)
                num+=1
            }
        }
        if(num != 0)
        {
            return new PacketCustom(channel, 2).setChunkDataPacket().compressed()
                .writeInt(chunk.xPosition).writeInt(chunk.zPosition)
                .writeShort(num)
                .writeByteArray(s.getBytes)
        }
        return null
    }
}
