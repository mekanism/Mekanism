package codechicken.multipart

import codechicken.lib.vec.Cuboid6
import codechicken.lib.vec.Vector3
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.MovingObjectPosition
import codechicken.lib.raytracer.IndexedCuboid6
import cpw.mods.fml.relauncher.SideOnly
import cpw.mods.fml.relauncher.Side
import net.minecraft.client.particle.EffectRenderer
import net.minecraft.client.renderer.RenderBlocks
import codechicken.lib.vec.BlockCoord
import net.minecraft.tileentity.TileEntity
import codechicken.lib.lighting.LazyLightMatrix
import codechicken.lib.data.MCDataOutput
import codechicken.lib.data.MCDataInput
import net.minecraft.entity.Entity
import java.lang.Iterable
import scala.collection.JavaConversions._
import net.minecraft.util.Vec3
import codechicken.lib.raytracer.ExtendedMOP
import codechicken.lib.raytracer.RayTracer

abstract class TMultiPart
{
    /**
     * Reference to the container TileMultipart instance
     */
    var tile:TileMultipart = _
    
    /**
     * Legacy helper function for getting the tile entity (from when TileMultipart was a trait). Use tile() now.
     */
    @Deprecated
    def getTile:TileEntity = tile
    /**
     * Getter for tile.worldObj
     */
    def world = if(tile == null) null else tile.worldObj
    /**
     * Short getter for xCoord
     */
    def x = tile.xCoord
    /**
     * Short getter for yCoord
     */
    def y = tile.yCoord
    /**
     * Short getter for zCoord
     */
    def z = tile.zCoord
    
    /**
     * The unique string identifier for this class of multipart.
     */
    def getType:String
    /**
     * Called when the container tile instance is changed to update reference
     */
    def bind(t:TileMultipart)
    {
        tile = t
    }
    
    /**
     * Perform an occlusion test to determine whether this and npart can 'fit' in this block space.
     */
    def occlusionTest(npart:TMultiPart):Boolean = true
    /**
     * Return a list of entity collision boxes.
     * Note all Cuboid6's returned by methods in TMultiPart should be within (0,0,0)->(1,1,1)
     */
    def getCollisionBoxes:Iterable[Cuboid6] = Seq()
    /**
     * Perform a raytrace of this part. The default implementation does a Cuboid6 ray trace on bounding boxes returned from getSubParts.
     * This should only be overridden if you need special ray-tracing capabilities such as triangular faces.
     * The returned ExtendedMOP will be passed to methods such as 'activate' so it is recommended to use the data field to indicate information about the hit area.
     */
    def collisionRayTrace(start: Vec3, end: Vec3): ExtendedMOP = {
      val offset = new Vector3(x, y, z)
      val boxes = getSubParts.map(c => new IndexedCuboid6(c.data, c.copy.add(offset)))
      return RayTracer.instance.rayTraceCuboids(new Vector3(start), new Vector3(end), boxes.toList,
              new BlockCoord(x, y, z), tile.getBlockType).asInstanceOf[ExtendedMOP]
    } 
    /**
     * For the default collisionRayTrace implementation, returns a list of indexed bounding boxes. The data field of ExtendedMOP will be set to the index of the cuboid the raytrace hit.
     */
    def getSubParts:Iterable[IndexedCuboid6] = Seq()
    
    /**
     * Return a list of items that should be dropped when this part is destroyed.
     */
    def getDrops:Iterable[ItemStack] = Seq()
    /**
     * Return a value indicating how hard this part is to break
     * @param hit An instance of ExtendedMOP from collisionRayTrace
     */
    def getStrength(hit:MovingObjectPosition, player:EntityPlayer):Float = 1
    /**
     * Harvest this part, removing it from the container tile and dropping items if necessary.
     * @param hit An instance of ExtendedMOP from collisionRayTrace
     * @param player The player harvesting the part
     */
    def harvest(hit:MovingObjectPosition, player:EntityPlayer)
    {
        if(!player.capabilities.isCreativeMode)
            tile.dropItems(getDrops)
        tile.remPart(this)
    }
    /**
     * The light level emitted by this part
     */
    def getLightValue = 0

    /**
     * Explosion resistance of the host tile is the maximum explosion resistance of the contained parts
     * @param entity The entity responsible for this explosion
     * @return The resistance of this part the the explosion
     */
    def explosionResistance(entity:Entity) = 0F

    /**
     * Add particles and other effects when a player is mining this part
     * @param hit An instance of ExtendedMOP from collisionRayTrace
     */
    @SideOnly(Side.CLIENT)
    def addHitEffects(hit:MovingObjectPosition, effectRenderer:EffectRenderer){}
    /**
     * Add particles and other effects when a player broke this part
     * @param hit An instance of ExtendedMOP from collisionRayTrace
     */
    @SideOnly(Side.CLIENT)
    def addDestroyEffects(hit:MovingObjectPosition, effectRenderer:EffectRenderer) {addDestroyEffects(effectRenderer)}
    
    @SideOnly(Side.CLIENT)
    @Deprecated
    def addDestroyEffects(effectRenderer:EffectRenderer){}
    /**
     * Render the static, unmoving faces of this part into the world renderer.
     * The Tessellator is already drawing.
     * @param olm An optional light matrix to be used for rendering things with perfect MC blended lighting (eg microblocks). Only use this if you have to.
     * @param pass The render pass, 1 or 0
     */
    @SideOnly(Side.CLIENT)
    def renderStatic(pos:Vector3, olm:LazyLightMatrix, pass:Int){}
    /**
     * Render the dynamic, changing faces of this part and other gfx as in a TESR. 
     * The Tessellator will need to be started if it is to be used.
     * @param pos The position of this block space relative to the renderer, same as x, y, z passed to TESR.
     * @param frame The partial interpolation frame value for animations between ticks
     * @param pass The render pass, 1 or 0
     */
    @SideOnly(Side.CLIENT)
    def renderDynamic(pos:Vector3, frame:Float, pass:Int){}
    /**
     * Draw the breaking overlay for this part. The overrideIcon in RenderBlocks will be set to the fracture icon.
     */
    @SideOnly(Side.CLIENT)
    def drawBreaking(renderBlocks:RenderBlocks){}
    /**
     * Override the drawing of the selection box around this part.
     * @param hit An instance of ExtendedMOP from collisionRayTrace
     * @return true if highlight rendering was overridden.
     */
    @SideOnly(Side.CLIENT)
    def drawHighlight(hit:MovingObjectPosition, player:EntityPlayer, frame:Float):Boolean = false
    
    /**
     * Write all the data required to describe a client version of this part to the packet.
     * Called serverside, when a client loads this part for the first time.
     */
    def writeDesc(packet:MCDataOutput){}
    /**
     * Fill out this part with the description information contained in packet. Will be exactly as written from writeDesc.
     * Called clientside when a client loads this part for the first time.
     */
    def readDesc(packet:MCDataInput){}
    /**
     * Save part to NBT (only called serverside)
     */
    def save(tag:NBTTagCompound){}
    /**
     * Load part from NBT (only called serverside)
     */
    def load(tag:NBTTagCompound){}
    /**
     * Gets a MCDataOutput instance for writing update data to clients with this part loaded.
     * The write stream functions as a buffer which is flushed in a compressed databurst packet at the end of the tick.
     */
    def getWriteStream:MCDataOutput = tile.getWriteStream(this)
    /**
     * Read and operate on data written to getWriteStream. Ensure all data this part wrote is read even if it's not going to be used.
     * The default implementation assumes a call to sendDescUpdate as the only use of getWriteStream.
     */
    def read(packet:MCDataInput)
    {
        readDesc(packet)
        tile.markRender()
    }
    /**
     * Quick and easy method to re-describe the whole part on the client. This will call read on the client which calls readDesc unless overriden.
     * Incremental changes should be sent rather than the whole description packet if possible.
     */
    def sendDescUpdate() = writeDesc(getWriteStream)
    
    /**
     * Called when a part is added or removed from this block space.
     * The part parameter may be null if several things have changed.
     */
    def onPartChanged(part:TMultiPart){}
    /**
     * Called when a neighbor block changed
     */
    def onNeighborChanged(){}
    /**
     * Called when this part is added to the block space
     */
    def onAdded() = onWorldJoin()
    /**
     * Called when this part is removed from the block space
     */
    def onRemoved() = onWorldSeparate()
    /**
     * Called when the containing chunk is loaded on the server.
     */
    def onChunkLoad() = onWorldJoin()
    /**
     * Called when the containing chunk is unloaded on the server.
     */
    def onChunkUnload() = onWorldSeparate()
    /**
     * Called when this part separates from the world (due to removal, chunk unload or other). Use this to sync with external data structures.
     */
    def onWorldSeparate(){}
    /**
     * Called when this part separates from the world (due to removal, chunk unload or other). Use this to sync with external data structures.
     */
    def onWorldJoin(){}
    /**
     * Called when this part is converted from a normal block/tile (only applicable if a converter has been registered)
     */
    def onConverted() = onAdded()
    /**
     * Called when this part is converted from a normal block/tile (only applicable if a converter has been registered) before the original tile has been replaced
     * Use this to clear out things like inventory from the old tile.
     */
    def invalidateConvertedTile(){}
    /**
     * Called when this part has been moved without a save/load.
     */
    def onMoved() = onWorldJoin()
    /**
     * Called just before this part is actually removed from the container tile
     */
    def preRemove(){}
    
    /**
     * Return whether this part needs update ticks. This will only be called on addition/removal so it should be a constant for this instance.
     */
    def doesTick = true
    /**
     * Called once per world tick. This will be called even if doesTick returns false if another part in the space needs ticks.
     */
    def update(){}
    /**
     * Called when a scheduled tick is executed. 
     */
    def scheduledTick(){}
    /**
     * Sets a scheduledTick callback for this part ticks in the future. This is a world time value, so if the chunk is unloaded and reloaded some time later, the tick may fire immediately.
     */
    def scheduleTick(ticks:Int) = TickScheduler.scheduleTick(this, ticks)
    
    /**
     * Return the itemstack for the middle click pick-block function.
     */
    def pickItem(hit:MovingObjectPosition):ItemStack = null
    /**
     * Called on block right click. item is the player's held item.
     * This should not modify the part client side. If the client call returns false, the server will not call this function.
     * @param hit An instance of ExtendedMOP from collisionRayTrace
     */
    def activate(player:EntityPlayer, hit:MovingObjectPosition, item:ItemStack) = false
    /**
     * Called on block left click. item is the player's held item.
     * @param hit An instance of ExtendedMOP from collisionRayTrace
     */
    def click(player:EntityPlayer, hit:MovingObjectPosition, item:ItemStack){}
    /**
     * Called when an entity is within this block space. May not actually collide with this part.
     */
    def onEntityCollision(entity:Entity){}
}