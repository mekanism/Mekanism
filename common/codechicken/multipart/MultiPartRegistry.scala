package codechicken.multipart

import scala.collection.mutable.HashMap
import codechicken.lib.packet.PacketCustom
import codechicken.lib.data.MCDataOutput
import codechicken.lib.data.MCDataInput
import net.minecraft.world.World
import codechicken.lib.vec.BlockCoord
import scala.collection.mutable.ListBuffer
import cpw.mods.fml.common.ModContainer
import cpw.mods.fml.common.Loader

/**
 * This class handles the registration and internal ID mapping of all multipart classes.
 */
object MultiPartRegistry
{
    /**
     * Interface to be registered for constructing parts.
     * Every instance of every multipart is constructed from an implementor of this.
     */
    trait IPartFactory
    {
        /**
         * Create a new instance of the part with the specified type name identifier
         * @param client If the part instance is for the client or the server
         */
        def createPart(name:String, client:Boolean):TMultiPart
    }
    
    /**
     * An interface for converting existing blocks/tile entities to multipart versions.
     */
    trait IPartConverter
    {
        /**
         * Return true if this converter can handle the specific blockID (may or may not actually convert the block)
         */
        def canConvert(blockID:Int):Boolean
        /**
         * Return a multipart version of the block at pos in world. Return null if no conversion is possible.
         */
        def convert(world:World, pos:BlockCoord):TMultiPart
    }
    
    private val typeMap:HashMap[String, (Boolean)=>TMultiPart] = new HashMap
    private val nameMap:HashMap[String, Int] = new HashMap
    private var idMap:Array[(String, (Boolean)=>TMultiPart)] = _
    private val idWriter = new IDWriter
    private val converters:Array[Seq[IPartConverter]] = Array.fill(4096)(Seq())
    private val containers:HashMap[String, ModContainer] = new HashMap()
    
    /**
     * The state of the registry. 0 = no parts, 1 = registering, 2 = registered
     */
    private var state:Int = 0
    
    /**
     * Register a part factory with an array of types it is capable of instantiating. Must be called before postInit
     */
    def registerParts(partFactory:IPartFactory, types:Array[String])
    {
        registerParts(partFactory.createPart _, types:_*)
    }
    
    /**
     * Scala function version of registerParts
     */
    def registerParts(partFactory:(String, Boolean)=>TMultiPart, types:String*)
    {
        if(loaded)
            throw new IllegalStateException("Parts must be registered in the init methods.")
        state=1
        
        val container = Loader.instance.activeModContainer
        if(container == null)
            throw new IllegalStateException("Parts must be registered during the initialization phase of a mod container")
        
        types.foreach{s => 
            if(typeMap.contains(s))
                throw new IllegalStateException("Part with id "+s+" is already registered.")

            typeMap.put(s, (c:Boolean) => partFactory(s, c))
            containers.put(s, container)
        }
    }
    
    /**
     * Register a part converter instance
     */
    def registerConverter(c:IPartConverter)
    {
        for(i <- 0 until 4096)
            if(c.canConvert(i))
                converters(i) = converters(i):+c
    }
    
    private[multipart] def beforeServerStart()
    {
        idMap = typeMap.toList.sortBy(_._1).toArray
        idWriter.setMax(idMap.length)
        nameMap.clear()
        for(i <- 0 until idMap.length)
            nameMap.put(idMap(i)._1, i)
    }
    
    private[multipart] def writeIDMap(packet:PacketCustom)
    {
        packet.writeInt(idMap.length)
        idMap.foreach(e => packet.writeString(e._1))
    }
    
    private[multipart] def readIDMap(packet:PacketCustom):Seq[String] =
    {
        val k = packet.readInt()
        idWriter.setMax(k)
        idMap = new Array(k)
        nameMap.clear()
        val missing = ListBuffer[String]()
        for(i <- 0 until k)
        {
            val s = packet.readString()
            val v = typeMap.get(s)
            if(v.isEmpty)
                missing+=s
            else {
                idMap(i) = (s, v.get)
                nameMap.put(s, i)
            }
        }
        return missing
    }
    
    /**
     * Return true if any multiparts have been registered
     */
    private[multipart] def required = state > 0
    
    /**
     * Return true if no more parts can be registered
     */
    def loaded = state == 2
    
    private[multipart] def postInit(){state = 2}
    
    /**
     * Writes the id of part to data
     */
    def writePartID(data:MCDataOutput, part:TMultiPart)
    {
        idWriter.write(data, nameMap.get(part.getType).get)
    }
    
    /**
     * Uses instantiators to creat a new part from the id read from data
     */
    def readPart(data:MCDataInput) = idMap(idWriter.read(data))._2(true)
    
    /**
     * Uses instantiators to create a new part with specified identifier on side
     */
    def createPart(name:String, client:Boolean):TMultiPart = 
    {
        val part = typeMap.get(name)
        if(part.isDefined)
            return part.get(client)
        else
        {
            System.err.println("Missing mapping for part with ID: "+name)
            return null
        }
    }
    
    /**
     * Calls converters to create a multipart version of the block at pos
     */
    def convertBlock(world:World, pos:BlockCoord, id:Int):TMultiPart =
    {
        converters(id).foreach{c =>
            val ret = c.convert(world, pos)
            if(ret != null)
                return ret
        }
        return null
    }
    
    def getModContainer(name:String) = containers(name)
}
