package codechicken.multipart

import net.minecraft.tileentity.TileEntity
import scala.collection.immutable.Map
import net.minecraft.world.World
import codechicken.lib.vec.BlockCoord
import codechicken.multipart.handler.MultipartProxy
import codechicken.lib.packet.PacketCustom
import net.minecraft.network.packet.Packet53BlockChange
import codechicken.multipart.asm.IMultipartFactory
import codechicken.multipart.asm.ASMMixinFactory

/**
 * This class manages the dynamic construction and allocation of container TileMultipart instances.
 * 
 * Classes that extend TileMultipart, adding tile centric logic, optimisations or interfaces, can be registered to a marker interface on a part instance.
 * When a part is added to the tile that implements the certain marker interface, the container tile will be replaced with a class that includes the functionality from the corresponding mixin class.
 * 
 * Classes are generated in a similar fashion to the way scala traits are compiled. To see the output, simply enable the config option and look in the asm/multipart folder of you .minecraft directory.
 * 
 * There are several mixin traits that come with the API included in the scalatraits package. TPartialOcclusionTile is defined as class instead of trait to give an example for Java programmers.
 */
object MultipartGenerator
{
    private var tileTraitMap:Map[Class[_], Set[String]] = Map()
    private var interfaceTraitMap_c:Map[String, String] = Map()
    private var interfaceTraitMap_s:Map[String, String] = Map()
    private var partTraitMap_c:Map[Class[_], Seq[String]] = Map()
    private var partTraitMap_s:Map[Class[_], Seq[String]] = Map()
    
    var factory:IMultipartFactory = ASMMixinFactory
    
    def partTraitMap(client:Boolean) = if(client) partTraitMap_c else partTraitMap_s
    
    def interfaceTraitMap(client:Boolean) = if(client) partTraitMap_c else interfaceTraitMap_s
    
    def traitsForPart(part:TMultiPart, client:Boolean):Seq[String] = 
    {
        var ret = partTraitMap(client).getOrElse(part.getClass, null)
        if(ret == null)
        {
            def heirachy(clazz:Class[_]):Seq[Class[_]] =
            {
                var superClasses:Seq[Class[_]] = clazz.getInterfaces.flatMap(c => heirachy(c)):+clazz
                if(clazz.getSuperclass != null)
                    superClasses = superClasses++heirachy(clazz.getSuperclass)
                return superClasses
            }
            
            val interfaceTraitMap = if(client) interfaceTraitMap_c else interfaceTraitMap_s
            ret = heirachy(part.getClass).flatMap(c => interfaceTraitMap.get(c.getName)).distinct
            if(client)
                partTraitMap_c = partTraitMap_c+(part.getClass -> ret)
            else
                partTraitMap_s = partTraitMap_s+(part.getClass -> ret)
        }
        return ret
    }
    
    /**
     * Check if part adds any new interfaces to tile, if so, replace tile with a new copy and call tile.addPart(part)
     * returns true if tile was replaced
     */
    private[multipart] def addPart(world:World, pos:BlockCoord, part:TMultiPart):TileMultipart =
    {
        val (tile, converted) = TileMultipart.getOrConvertTile2(world, pos)
        var partTraits = traitsForPart(part, world.isRemote)
        var ntile = tile
        if(ntile != null)
        {
            if(converted)//perform client conversion
            {
                ntile.partList(0).invalidateConvertedTile()
                world.setBlock(pos.x, pos.y, pos.z, MultipartProxy.block.blockID, 0, 0)
                silentAddTile(world, pos, ntile)
                PacketCustom.sendToChunk(new Packet53BlockChange(pos.x, pos.y, pos.z, world), world, pos.x>>4, pos.z>>4)
                ntile.partList(0).onConverted()
                ntile.writeAddPart(ntile.partList(0))
            }
            
            val tileTraits = tileTraitMap(tile.getClass)
            partTraits = partTraits.filter(!tileTraits(_))
            if(!partTraits.isEmpty)
            {
                ntile = factory.generateTile(partTraits++tileTraits, world.isRemote)
                tile.setValid(false)
                silentAddTile(world, pos, ntile)
                ntile.from(tile)
            }
        }
        else
        {
            world.setBlock(pos.x, pos.y, pos.z, MultipartProxy.block.blockID, 0, 0)
            ntile = factory.generateTile(partTraits, world.isRemote)
            silentAddTile(world, pos, ntile)
        }
        ntile.addPart_impl(part)
        return ntile
    }
    
    /**
     * Adds a tile entity to the world without notifying neighbor blocks or adding it to the tick list
     */
    def silentAddTile(world:World, pos:BlockCoord, tile:TileEntity)
    {
    	val chunk = world.getChunkFromBlockCoords(pos.x, pos.z)
    	if(chunk != null)
    		chunk.setChunkBlockTileEntity(pos.x & 15, pos.y, pos.z & 15, tile)
    }
    
    /**
     * Check if tile satisfies all the interfaces required by parts. If not, return a new generated copy of tile
     */
    private[multipart] def generateCompositeTile(tile:TileEntity, parts:Seq[TMultiPart], client:Boolean):TileMultipart = 
    {
        var partTraits = parts.flatMap(traitsForPart(_, client)).distinct
        if(tile != null && tile.isInstanceOf[TileMultipart])
        {
            var tileTraits = tileTraitMap(tile.getClass)
            if(partTraits.forall(tileTraits(_)) && partTraits.size == tileTraits.size)//equal contents
                return tile.asInstanceOf[TileMultipart]
            
        }
        return factory.generateTile(partTraits, client)
    }
    
    /**
     * Check if there are any redundant interfaces on tile, if so, replace tile with new copy
     */
    private[multipart] def partRemoved(tile:TileMultipart, part:TMultiPart):TileMultipart = 
    {
        val client = tile.worldObj.isRemote
        var partTraits = tile.partList.flatMap(traitsForPart(_, client))
        var testSet = partTraits.toSet
        if(!traitsForPart(part, client).forall(testSet(_)))
        {
            val ntile = factory.generateTile(testSet.toSeq, client)
            tile.setValid(false)
            silentAddTile(tile.worldObj, new BlockCoord(tile), ntile)
            ntile.from(tile)
            ntile.notifyTileChange()
            return ntile
        }
        return tile
    }
    
    /**
     * register s_trait to be applied to tiles containing parts implementing s_interface
     */
    def registerTrait(s_interface:String, s_trait:String):Unit = registerTrait(s_interface, s_trait, s_trait)
    
    /**
     * register traits to be applied to tiles containing parts implementing s_interface
     * s_trait for server worlds (may be null)
     * c_trait for client worlds (may be null)
     */
    def registerTrait(s_interface:String, c_trait:String, s_trait:String)
    {
        if(c_trait != null)
        {
            if(interfaceTraitMap_c.contains(s_interface))
                System.err.println("Trait already registered for "+s_interface)
            else
            {
                interfaceTraitMap_c = interfaceTraitMap_c+(s_interface->c_trait)
                factory.registerTrait(s_interface, c_trait, true)
            }
        }
        if(s_trait != null)
        {
            if(interfaceTraitMap_s.contains(s_interface))
                System.err.println("Trait already registered for "+s_interface)
            else
            {
                interfaceTraitMap_s = interfaceTraitMap_s+(s_interface->s_trait)
                factory.registerTrait(s_interface, s_trait, false)
            }
        }
    }
    
    def registerPassThroughInterface(s_interface:String):Unit = registerPassThroughInterface(s_interface, true, true)
    
    /**
     * A passthrough interface, is an interface to be implemented on the container tile instance, for which all calls are passed directly to the single implementing part.
     * Registering a passthrough interface is equivalent to defining a mixin class as follows.
     *  1. field 'impl' which contains the reference to the corresponding part
     *  2. occlusionTest is overriden to prevent more than one part with s_interface existing in the block space
     *  3. implementing s_interface and passing all calls directly to the part instance.
     *  
     *  This allows compatibility with APIs that expect interfaces on the tile entity.
     */
    def registerPassThroughInterface(s_interface:String, client:Boolean, server:Boolean)
    {
        val tType = factory.generatePassThroughTrait(s_interface)
        if(tType == null)
            return
        
        if(client)
        {
            if(interfaceTraitMap_c.contains(s_interface))
                System.err.println("Trait already registered for "+s_interface)
            else
                interfaceTraitMap_c = interfaceTraitMap_c+(s_interface->tType)
        }
        if(server)
        {
            if(interfaceTraitMap_s.contains(s_interface))
                System.err.println("Trait already registered for "+s_interface)
            else
                interfaceTraitMap_s = interfaceTraitMap_s+(s_interface->tType)
        }
    }
    
    private[multipart] def registerTileClass(clazz:Class[_ <: TileEntity], traits:Set[String])
    {
        tileTraitMap=tileTraitMap+(clazz->traits)
        MultipartProxy.onTileClassBuilt(clazz)
    }
}