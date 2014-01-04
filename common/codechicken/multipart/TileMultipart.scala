package codechicken.multipart

import net.minecraft.tileentity.TileEntity
import scala.collection.mutable.ListBuffer
import codechicken.lib.packet.PacketCustom
import codechicken.lib.vec.BlockCoord
import net.minecraft.world.World
import java.util.List
import net.minecraft.nbt.NBTTagCompound
import codechicken.lib.data.MCDataOutput
import codechicken.multipart.handler.MultipartProxy
import net.minecraft.block.Block
import net.minecraft.item.ItemStack
import codechicken.lib.vec.Vector3
import net.minecraft.nbt.NBTTagList
import java.util.Random
import codechicken.multipart.handler.MultipartSPH
import codechicken.lib.lighting.LazyLightMatrix
import net.minecraft.entity.item.EntityItem
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.entity.Entity
import scala.collection.JavaConversions._
import java.util.Collection
import codechicken.lib.raytracer.ExtendedMOP
import net.minecraft.util.Vec3
import java.lang.Iterable
import scala.collection.mutable.{Map => MMap}
import net.minecraft.util.AxisAlignedBB

class TileMultipart extends TileEntity
{
    /**
     * List of parts in this tile space
     */
    var partList = Seq[TMultiPart]()
    
    private var doesTick = false
    
    private[multipart] def from(that:TileMultipart)
    {
        copyFrom(that)
        loadFrom(that)
    }
    
    /**
     * This method should be used for copying all the data from the fields in that container tile. 
     * This method will be automatically generated on java tile traits with fields if it is not overridden.
     */
    def copyFrom(that:TileMultipart)
    {
        partList = that.partList
        doesTick = that.doesTick
    }
    
    def loadFrom(that:TileMultipart)
    {
        partList.foreach(_.bind(this))
        if(doesTick)
        {
            doesTick = false
            setTicking(true)
        }
    }
    
    /**
     * Overidden in TSlottedTile when a part that goes in a slot is added
     */
    def partMap(slot:Int):TMultiPart = null
    
    /**
     * Implicit java conversion of part list
     */
    def jPartList():List[TMultiPart] = partList
    
    override def canUpdate = doesTick
    
    def operate(f:(TMultiPart)=>Unit) {
        val it = partList.iterator
        while(it.hasNext) {
            val p = it.next()
            if(p.tile != null) f(p)
        }
    }
    
    override def updateEntity()
    {
        operate(_.update())
    }
    
    override def onChunkUnload()
    {
        operate(_.onChunkUnload())
    }
    
    def onChunkLoad()
    {
        operate(_.onChunkLoad())
    }

    final def setValid(b:Boolean)
    {
        if(b)
            super.validate()
        else
            super.invalidate()
    }
    
    def onMoved()
    {
        operate(_.onMoved())
    }
    
    override def invalidate()
    {
        if(!isInvalid)
        {
            super.invalidate()
            if(worldObj != null) {
                partList.foreach(_.onWorldSeparate())
                if(worldObj.isRemote)
                    TileMultipart.putClientCache(this)
            }
        }
    }
    
    /**
     * Called by parts when they have changed in some form that affects the world.
     * Notifies neighbor blocks, parts that share this host and recalculates lighting
     */
    def notifyPartChange(part:TMultiPart)
    {
        internalPartChange(part)
        
        worldObj.notifyBlocksOfNeighborChange(xCoord, yCoord, zCoord, MultipartProxy.block.blockID)
        worldObj.updateAllLightTypes(xCoord, yCoord, zCoord)
    }
    
    /**
     * Notifies parts sharing this host of a change
     */
    def internalPartChange(part:TMultiPart)
    {
        operate(p => if(part != p) p.onPartChanged(part))
    }
    
    /**
     * Notifies all parts not in the passed collection of a change from all the parts in the collection
     */
    def multiPartChange(parts:Collection[TMultiPart])
    {
        operate(p => if(!parts.contains(p)) parts.foreach(p.onPartChanged))
    }
    
    /**
     * Notifies neighboring blocks that this tile has changed
     */
    def notifyTileChange()
    {
    	worldObj.func_96440_m(xCoord, yCoord, zCoord, 0)
    }
    
    def onNeighborBlockChange()
    {
        operate(_.onNeighborChanged())
    }
    
    /**
     * Blank implementation, overriden by TTileChangeTile
     */
    def onNeighborTileChange(tileX:Int, tileY:Int, tileZ:Int) {}
    
    def getLightValue = partList.view.map(_.getLightValue).max

    def getExplosionResistance(entity:Entity) = partList.view.map(_.explosionResistance(entity)).max
    
    /**
     * Callback for parts to mark the chunk as needs saving
     */
    def markDirty()
    {
        worldObj.markTileEntityChunkModified(xCoord, yCoord, zCoord, this)
    }
    
    /**
     * Mark this block space for a render update. 
     */
    def markRender()
    {
        worldObj.markBlockForRenderUpdate(xCoord, yCoord, zCoord)
    }
    
    /**
     * Helper function for calling a second level notify on a side (eg indirect power from a lever) 
     */
    def notifyNeighborChange(side:Int)
    {
        val pos = new BlockCoord(this).offset(side)
        worldObj.notifyBlocksOfNeighborChange(pos.x, pos.y, pos.z, MultipartProxy.block.blockID)
    }
    
    def isSolid(side:Int):Boolean = 
    {
        val part = partMap(side)
        if(part != null) 
            return part.asInstanceOf[TFacePart].solid(side)
        
        return false
    }
    
    private def setTicking(tick:Boolean)
    {
        if(doesTick == tick)
            return
        
        doesTick = tick
        if(worldObj != null && worldObj.getBlockTileEntity(xCoord, yCoord, zCoord) == this)
        {
            if(tick)
                worldObj.addTileEntity(this)
            else
                worldObj.markTileEntityForDespawn(this)
        }
    }
    
    /**
     * Returns true if part can be added to this space
     */
    def canAddPart(part:TMultiPart):Boolean =
    {
        if(partList.contains(part))
            return false
        
        return occlusionTest(partList, part)
    }
    
    /**
     * Returns true if opart can be replaced with npart (note opart and npart may be the exact same object)
     * 
     * This function should be used for testing if a part can change it's shape (eg. rotation, expansion, cable connection)
     * For example, to test whether a cable part can connect to it's neighbor:
     *  1. Set the cable part's bounding boxes as if the connection is established
     *  2. Call canReplacePart(part, part)
     *  3. If canReplacePart succeeds, perform connection, else, revert bounding box
     */
    def canReplacePart(opart:TMultiPart, npart:TMultiPart):Boolean = 
    {
        val olist = partList.filterNot(_ == opart)
        if(olist.contains(npart))
            return false
        
        return occlusionTest(olist, npart)
    }
    
    /**
     * Returns true if parts do not occlude npart
     */
    def occlusionTest(parts:Seq[TMultiPart], npart:TMultiPart):Boolean =
    {
        return parts.forall(part => part.occlusionTest(npart) && npart.occlusionTest(part))
    }
    
    /**
     * Get the write stream for updates to part
     */
    def getWriteStream(part:TMultiPart):MCDataOutput = getWriteStream.writeByte(partList.indexOf(part))
    
    private def getWriteStream = MultipartSPH.getTileStream(worldObj, new BlockCoord(this))
    
    private[multipart] def addPart_impl(part:TMultiPart)
    {
        if(!worldObj.isRemote)
            writeAddPart(part)
            
        addPart_do(part)
        part.onAdded()
        partAdded(part)
        notifyPartChange(part)
        notifyTileChange()
        markDirty()
        markRender()
    }
    
    private[multipart] def writeAddPart(part:TMultiPart)
    {
        val stream = getWriteStream.writeByte(253)
        MultiPartRegistry.writePartID(stream, part)
        part.writeDesc(stream)
    }
    
    private[multipart] def addPart_do(part:TMultiPart)
    {
        assert(partList.size < 250, "Tried to add more than 250 parts to the one tile. You're doing it wrong")
        
        partList = partList:+part
        bindPart(part)
        part.bind(this)
        
        if(!doesTick && part.doesTick)
            setTicking(true)
    }
    
    /**
     * Bind this part to an internal cache.
     * Provided for trait overrides, do not call externally.
     */
    def bindPart(part:TMultiPart){}
    
    /**
     * Called when a part is added (placement)
     * Provided for trait overrides, do not call externally.
     */
    def partAdded(part:TMultiPart)
    {
        if(part.isInstanceOf[IRandomUpdateTick])
            TickScheduler.loadRandomTick(part)
    }
    
    /**
     * Removes part from this tile. Note that due to the operation sync, the part may not be removed until the call stack has been passed to all other parts in the space.
     */
    def remPart(part:TMultiPart):TileMultipart =
    {
        assert(!worldObj.isRemote, "Cannot remove multi parts from a client tile")
        remPart_impl(part)
    }
    
    private[multipart] def remPart_impl(part:TMultiPart):TileMultipart =
    {
        remPart_do(part, !worldObj.isRemote)
        
        if(!isInvalid)
        {
            val tile = MultipartGenerator.partRemoved(this, part)
            notifyPartChange(part)
            markDirty()
            markRender()
            return tile
        }
        
        return null
    }
    
    private def remPart_do(part:TMultiPart, sendPacket:Boolean):Int =
    {
        val r = partList.indexOf(part)
        if(r < 0)
            throw new IllegalArgumentException("Tried to remove a non-existant part")
        
        part.preRemove()
        partList = partList.filterNot(_ == part)
        
        if(sendPacket)
            getWriteStream.writeByte(254).writeByte(r)
        
        partRemoved(part, r)
        part.onRemoved()
        part.tile = null
        
        if(partList.isEmpty)
        {
            worldObj.setBlockToAir(xCoord, yCoord, zCoord)
        }
        else
        {
            if(part.doesTick && doesTick)
            {
                var ntick = false
                partList.foreach(part => ntick |= part.doesTick)
                if(!ntick)
                    setTicking(false)
            }
        }
        return r
    }
    
    /**
     * Remove this part from internal cache.
     * Provided for trait overrides, do not call externally.
     */
    def partRemoved(part:TMultiPart, p:Int){}

    private[multipart] def loadParts(parts:ListBuffer[TMultiPart])
    {
        clearParts()
        parts.foreach(p => addPart_do(p))
        if(worldObj != null) {
            if(worldObj.isRemote)
                operate(_.onWorldJoin())
            notifyPartChange(null)
        }
    }
    
    /**
     * Remove all parts from internal cache
     * Provided for trait overrides, do not call externally.
     */
    def clearParts()
    {
        partList = Seq()
    }
    
    /**
     * Writes the description of this tile, and all parts composing it, to packet
     */
    def writeDesc(packet:MCDataOutput)
    {
        packet.writeByte(partList.size)
        partList.foreach{part =>
            MultiPartRegistry.writePartID(packet, part)
            part.writeDesc(packet)
        }
    }
    
    /**
     * Perform a raytrace returning all intersecting parts sorted nearest to farthest
     */
    def rayTraceAll(start:Vec3, end:Vec3):Iterable[ExtendedMOP] = 
    {
        var list = ListBuffer[ExtendedMOP]()
        for((p, i) <- partList.view.zipWithIndex)
            p.collisionRayTrace(start, end) match {
                case mop:ExtendedMOP =>
                    mop.data = (i, mop.data)
                    list+=mop
                case _ =>
            }
        
        return list.sorted
    }
    
    /**
     * Perform a raytrace returning the nearest intersecting part
     */
    def collisionRayTrace(start:Vec3, end:Vec3):ExtendedMOP = rayTraceAll(start, end).headOption.getOrElse(null)
    
    /**
     * Drop and remove part at index (internal mining callback)
     */
    def harvestPart(index:Int, hit:ExtendedMOP, player:EntityPlayer) = partList(index) match {
        case null =>
        case part => part.harvest(hit, player)
    }
    
    /**
     * Utility function for dropping items around the center of this space
     */
    def dropItems(items:Iterable[ItemStack])
    {
        val pos = Vector3.fromTileEntityCenter(this)
        items.foreach(item => TileMultipart.dropItem(item, worldObj, pos))
    }
    
    override def writeToNBT(tag:NBTTagCompound)
    {
        super.writeToNBT(tag)
        val taglist = new NBTTagList
        partList.foreach{part => 
            val parttag = new NBTTagCompound
            parttag.setString("id", part.getType)
            part.save(parttag)
            taglist.appendTag(parttag)
    }
        tag.setTag("parts", taglist)
    }
    
    /**
     * Internal callback
     */
    def onEntityCollision(entity:Entity)
    {
        operate(_.onEntityCollision(entity))
    }
    
    /**
     * Internal callback, overriden in TRedstoneTile
     */
    def strongPowerLevel(side:Int) = 0
    
    /**
     * Internal callback, overriden in TRedstoneTile
     */
    def weakPowerLevel(side:Int) = 0
    
    /**
     * Internal callback, overriden in TRedstoneTile
     */
    def canConnectRedstone(side:Int) = false
}

class TileMultipartClient extends TileMultipart
{
    def renderStatic(pos:Vector3, olm:LazyLightMatrix, pass:Int)
    {
        partList.foreach(part => part.renderStatic(pos, olm, pass))
    }
    
    def renderDynamic(pos:Vector3, frame:Float, pass:Int)
    {
        partList.foreach(part => part.renderDynamic(pos, frame, pass:Int))
    }
    
    def randomDisplayTick(random:Random){}
    
    override def shouldRenderInPass(pass:Int) = 
    {
        MultipartRenderer.pass = pass
        true
    }
    
    override def getRenderBoundingBox = AxisAlignedBB.getAABBPool.getAABB(xCoord, yCoord, zCoord, xCoord + 1, yCoord + 1, zCoord + 1)
}

/**
 * Static class with multipart manipulation helper functions
 */
object TileMultipart
{
    var renderID:Int = -1
    
    /**
     * Playerinstance will often remove the tile entity instance and set the block to air on the client before the multipart packet handler fires it's updates.
     * In order to maintain the packet data, and make sure all written data is read, the tile needs to be kept around internally until 
     */
    private val clientFlushMap = MMap[BlockCoord, TileMultipart]()
    
    private[multipart] def flushClientCache() = clientFlushMap.clear()
    
    private[multipart] def putClientCache(t:TileMultipart) = clientFlushMap.put(new BlockCoord(t), t)
    
    /**
     * Gets a multipar ttile instance at pos, converting if necessary.
     */
    def getOrConvertTile(world:World, pos:BlockCoord) = getOrConvertTile2(world, pos)._1
    
    /**
     * Gets a multipart tile instance at pos, converting if necessary.
     * Note converted tiles are merely a structure formality, 
     * they do not actually exist in world until they are required to by the addition of another multipart to their space.
     * @return (The tile or null if there was none, true if the tile is a result of a conversion)
     */
    def getOrConvertTile2(world:World, pos:BlockCoord):(TileMultipart, Boolean) =
    {
        val t = world.getBlockTileEntity(pos.x, pos.y, pos.z)
        if(t.isInstanceOf[TileMultipart])
            return (t.asInstanceOf[TileMultipart], false)
        
        val id = world.getBlockId(pos.x, pos.y, pos.z)
        val p = MultiPartRegistry.convertBlock(world, pos, id)
        if(p != null)
        {
            val t = MultipartGenerator.generateCompositeTile(null, Seq(p), world.isRemote)
            t.xCoord = pos.x
            t.yCoord = pos.y
            t.zCoord = pos.z
            t.setWorldObj(world)
            t.addPart_do(p)
            return (t, true)
        }
        return (null, false)
    }
    
    /**
     * Gets the multipart tile instance at pos, or null if it doesn't exist or is not a multipart tile
     */
    def getTile(world:World, pos:BlockCoord) =
        world.getBlockTileEntity(pos.x, pos.y, pos.z) match {
            case t:TileMultipart => t
            case _ => null
        }

    /**
     * Returns whether part can be added to the space at pos. Will do conversions as necessary.
     * This function is the recommended way to add parts to the world.
     */
    def canPlacePart(world:World, pos:BlockCoord, part:TMultiPart):Boolean =
    {
        part.getCollisionBoxes.foreach{b => 
            if(!world.checkNoEntityCollision(b.toAABB.offset(pos.x, pos.y, pos.z)))
                return false
        }
        
        val t = getOrConvertTile(world, pos)
        if(t != null)
            return t.canAddPart(part)
        
        if(!replaceable(world, pos))
            return false
        
        return true
    }
    
    /**
     * Returns if the block at pos is replaceable (air, vines etc)
     */
    def replaceable(world:World, pos:BlockCoord):Boolean = 
    {
        val block = Block.blocksList(world.getBlockId(pos.x, pos.y, pos.z))
        return block == null || block.isAirBlock(world, pos.x, pos.y, pos.z) || block.isBlockReplaceable(world, pos.x, pos.y, pos.z)
    }
    
    /**
     * Adds a part to a block space. canPlacePart should always be called first.
     * The addition of parts on the client is handled internally.
     */
    def addPart(world:World, pos:BlockCoord, part:TMultiPart):TileMultipart =
    {
        assert(!world.isRemote, "Cannot add multi parts to a client tile.")
        return MultipartGenerator.addPart(world, pos, part)
    }
    
    /**
     * Constructs this tile and its parts from a desc packet
     */
    def handleDescPacket(world:World, pos:BlockCoord, packet:PacketCustom)
    {
        val nparts = packet.readUByte
        val parts = new ListBuffer[TMultiPart]()
        for(i <- 0 until nparts)
        {
            val part:TMultiPart = MultiPartRegistry.readPart(packet)
            part.readDesc(packet)
            parts+=part
        }
        
        if(parts.size == 0)
            return
        
        val t = world.getBlockTileEntity(pos.x, pos.y, pos.z)
        val tilemp = MultipartGenerator.generateCompositeTile(t, parts, true)
        if(tilemp != t) {
            world.setBlock(pos.x, pos.y, pos.z, MultipartProxy.block.blockID)
            MultipartGenerator.silentAddTile(world, pos, tilemp)
        }
        
        tilemp.loadParts(parts)
        tilemp.notifyTileChange()
        tilemp.markRender()
    }
    
    /**
     * Handles an update packet, addition, removal and otherwise
     */
    def handlePacket(pos:BlockCoord, world:World, i:Int, packet:PacketCustom)
    {
        lazy val tilemp = Option(BlockMultipart.getTile(world, pos.x, pos.y, pos.z)).getOrElse(clientFlushMap(pos))
        
        i match
        {
            case 253 =>
                val part = MultiPartRegistry.readPart(packet)
                part.readDesc(packet)
                MultipartGenerator.addPart(world, pos, part)
            case 254 => tilemp.remPart_impl(tilemp.partList(packet.readUByte))
            case _ => tilemp.partList(i).read(packet)
        }
    }
    
    /**
     * Creates this tile from an NBT tag
     */
    def createFromNBT(tag:NBTTagCompound):TileMultipart =
    {
        val partList = tag.getTagList("parts")
        val parts = ListBuffer[TMultiPart]()
        
        for(i <- 0 until partList.tagCount)
        {
            val partTag = partList.tagAt(i).asInstanceOf[NBTTagCompound]
            val partID = partTag.getString("id")
            val part = MultiPartRegistry.createPart(partID, false)
            if(part != null)
            {
                part.load(partTag)
                parts+=part
            }
        }
        
        if(parts.size == 0)
            return null
        
        val tmb = MultipartGenerator.generateCompositeTile(null, parts, false)
        tmb.readFromNBT(tag)
        tmb.loadParts(parts)
        return tmb
    }
    
    /**
     * Drops an item around pos
     */
    def dropItem(stack:ItemStack, world:World, pos:Vector3)
    {
        val item = new EntityItem(world, pos.x, pos.y, pos.z, stack)
        item.motionX = world.rand.nextGaussian() * 0.05
        item.motionY = world.rand.nextGaussian() * 0.05 + 0.2
        item.motionZ = world.rand.nextGaussian() * 0.05
        item.delayBeforeCanPickup = 10
        world.spawnEntityInWorld(item)
    }
}
