package codechicken.multipart

import codechicken.lib.world.WorldExtensionInstantiator
import codechicken.lib.world.WorldExtension
import codechicken.lib.world.ChunkExtension
import net.minecraft.world.chunk.Chunk
import net.minecraft.world.World
import net.minecraft.nbt.NBTTagCompound
import scala.collection.mutable.ListBuffer
import net.minecraft.nbt.NBTTagList
import codechicken.lib.vec.BlockCoord
import net.minecraft.world.ChunkCoordIntPair
import net.minecraft.world.ChunkPosition
import scala.collection.mutable.HashSet
import java.io.DataOutputStream
import net.minecraftforge.common.DimensionManager
import java.io.File
import java.io.FileOutputStream
import net.minecraft.nbt.CompressedStreamTools
import java.io.DataInputStream
import java.io.FileInputStream
import net.minecraft.world.storage.SaveHandler
import java.util.ArrayList

/**
 * Used for scheduling delayed callbacks to parts.
 * Do not use this for redstone applications that require precise timing.
 * If 2 parts are both scheduled for an update on the same tick, there is no guarantee which one will update first. 
 * These parts should not depend on a state of another part that may have changed before/after them.
 */
object TickScheduler extends WorldExtensionInstantiator
{
    class PartTickEntry(val part:TMultiPart, var time:Long, var random:Boolean)
    {
        def this(part:TMultiPart, ticks:Int) = this(part, ticks, false)
    }
    
    private class WorldTickScheduler(world$:World) extends WorldExtension(world$)
    {
        var schedTime = 0L
        var tickChunks = HashSet[ChunkTickScheduler]()
        private var processing = false
        private val pending = ListBuffer[PartTickEntry]()
        
        def scheduleTick(part:TMultiPart, ticks:Int, random:Boolean)
        {
            if(processing)
                pending+=new PartTickEntry(part, schedTime+ticks, random)
            else
                _scheduleTick(part, schedTime+ticks, random)
        }
        
        def _scheduleTick(part:TMultiPart, time:Long, random:Boolean)
        {
            if(part.tile != null)
                getChunkExtension(part.tile.xCoord>>4, part.tile.zCoord>>4)
                    .asInstanceOf[ChunkTickScheduler].scheduleTick(part, time, random)
        }
        
        def loadRandom(part:TMultiPart) = scheduleTick(part, nextRandomTick, true)
        
        override def preTick()
        {
            processing = true
        }
        
        override def postTick()
        {
            if(!tickChunks.isEmpty)
                tickChunks = tickChunks.filter(_.processTicks())
            
            processing = false
            pending.foreach(e => _scheduleTick(e.part, e.time, e.random))
            pending.clear()
            
            schedTime+=1
        }
        
        def saveDir:File =
        {
            if(world.provider.dimensionId == 0)//Calling DimensionManager.getCurrentSaveRootDirectory too early breaks game saves, we have a world reference, use it
                return world.getSaveHandler.asInstanceOf[SaveHandler].getWorldDirectory
                
            return new File(DimensionManager.getCurrentSaveRootDirectory, world.provider.getSaveFolder)
        }
        
        def saveFile:File = new File(saveDir, "multipart.dat")
        
        override def load() 
        {
            try
            {
                val din = new DataInputStream(new FileInputStream(saveFile))
                loadTag(CompressedStreamTools.readCompressed(din))
                din.close()
            }
            catch
            {
                case e:Exception =>
            }
        
            loadTag(new NBTTagCompound)
        }
        
        def loadTag(tag:NBTTagCompound)
        {
            if(tag.hasKey("schedTime"))
                schedTime = tag.getLong("schedTime")
            else
                schedTime = world.getTotalWorldTime
        }
        
        def saveTag:NBTTagCompound =
        {
            val tag = new NBTTagCompound
            tag.setLong("schedTime", schedTime)
            return tag
        }
        
        override def save() {
            val file = saveFile
            if(!file.getParentFile.exists)
                file.getParentFile.mkdirs()
            if(!file.exists)
                file.createNewFile()
            
            val dout = new DataOutputStream(new FileOutputStream(file))
            CompressedStreamTools.writeCompressed(saveTag, dout)
            dout.close()
        }
        
        def nextRandomTick = world.rand.nextInt(800)+800
    }
    
    def createWorldExtension(world:World):WorldExtension = new WorldTickScheduler(world)
    
    private class ChunkTickScheduler(chunk$:Chunk, world:WorldTickScheduler) extends ChunkExtension(chunk$, world)
    {
        import codechicken.multipart.handler.MultipartProxy._
        
        var tickList = ListBuffer[PartTickEntry]()
        
        def schedTime = world.schedTime
        
        def scheduleTick(part:TMultiPart, time:Long, random:Boolean)
        {
            val it = tickList.iterator
            while(it.hasNext)
            {
                val e = it.next()
                if(e.part == part)
                {
                    if(e.random && !random)//only override an existing tick if we're going from random->scheduled
                    {
                        e.time = time
                        e.random = random
                    }
                    return
                }
            }
            tickList+=new PartTickEntry(part, time, random)
            if(tickList.size == 1)
                world.tickChunks+=this
        }
        
        def nextRandomTick = world.nextRandomTick
        
        def processTicks():Boolean =
        {
            tickList = tickList.filter(processTick)
            return !tickList.isEmpty
        }
        
        def processTick(e:PartTickEntry):Boolean =
        {
            if(e.time <= schedTime)
            {
                if(e.part.tile != null)
                {
                    if(e.random && e.part.isInstanceOf[IRandomUpdateTick])
                        e.part.asInstanceOf[IRandomUpdateTick].randomUpdate()
                    else
                        e.part.scheduledTick()
                    
                    if(e.part.isInstanceOf[IRandomUpdateTick])
                    {
                        e.time = schedTime+nextRandomTick
                        e.random = true
                        return true
                    }
                }
                return false
            }
            return true
        }
        
        override def saveData(data:NBTTagCompound)
        {
            val tagList = new NBTTagList
            tickList.foreach{e =>
                val part = e.part
                if(part.tile != null && !e.random)
                {
                    val tag = new NBTTagCompound
                    tag.setShort("pos", indexInChunk(new BlockCoord(part.tile)).toShort)
                    tag.setByte("i", part.tile.partList.indexOf(part).toByte)
                    tag.setLong("time", e.time)
                    tagList.appendTag(tag)
                }
            }
            if(tagList.tagCount > 0)
                data.setTag("multipartTicks", tagList)
        }
        
        override def loadData(data:NBTTagCompound)
        {
            tickList.clear()
            if(!data.hasKey("multipartTicks"))
                return
            
            val tagList = data.getTagList("multipartTicks")
            val cc = new ChunkCoordIntPair(0, 0)
            for(i <- 0 until tagList.tagCount)
            {
                val tag = tagList.tagAt(i).asInstanceOf[NBTTagCompound]
                val pos = indexInChunk(cc, tag.getShort("pos"))
                val tile = chunk.chunkTileEntityMap.get(new ChunkPosition(pos.x, pos.y, pos.z))
                if(tile.isInstanceOf[TileMultipart])
                    tickList+=new PartTickEntry(tile.asInstanceOf[TileMultipart].partList(tag.getByte("i")), tag.getLong("time"), false)
            }
        }
        
        override def load()
        {
            val it = new ArrayList(chunk.chunkTileEntityMap.values).iterator
            while(it.hasNext)
            {
                val t = it.next
                if(t.isInstanceOf[TileMultipart])
                {
                    val tmp = t.asInstanceOf[TileMultipart]
                    tmp.onChunkLoad()
                    tmp.partList.foreach(p =>
                        if(p.isInstanceOf[IRandomUpdateTick])
                            world.scheduleTick(p, nextRandomTick, true))
                }
            }
            
            if(!tickList.isEmpty)
                world.tickChunks+=this
        }
        
        override def unload()
        {
            if(!tickList.isEmpty)
                world.tickChunks-=this
        }
    }
    
    def createChunkExtension(chunk:Chunk, world:WorldExtension):ChunkExtension = new ChunkTickScheduler(chunk, world.asInstanceOf[WorldTickScheduler])
    
    private[multipart] def loadRandomTick(part:TMultiPart)
    {
        getExtension(part.tile.worldObj).asInstanceOf[WorldTickScheduler].loadRandom(part)
    }
    
    /**
     * Schedule a tick for part relative to the current time.
     */
    def scheduleTick(part:TMultiPart, ticks:Int)
    {
        getExtension(part.tile.worldObj).asInstanceOf[WorldTickScheduler].scheduleTick(part, ticks, false)
    }
    
    /**
     * Returns the current scheduler time. Like the world time, but unaffected by the time set command and other things changing time of day.
     * Deprecated in favor of world.getTotalWorldTime
     */
    @Deprecated
    def getSchedulerTime(world:World):Long = getExtension(world).asInstanceOf[WorldTickScheduler].schedTime
}