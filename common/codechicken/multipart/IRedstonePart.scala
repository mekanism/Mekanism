package codechicken.multipart

import net.minecraft.world.World
import net.minecraft.block.Block
import net.minecraft.world.IBlockAccess
import net.minecraft.util.Direction
import codechicken.lib.vec.Rotation._

/**
 * Interface for parts with redstone interaction
 * 
 * Marker interface for TRedstoneTile. This means that if a part is an instance of IRedstonePart, the container tile may be cast to TRedstoneTile
 */
trait IRedstonePart
{
    /**
     * Returns the strong (indirect, through blocks) signal being emitted by this part to the specified side
     */
    def strongPowerLevel(side:Int):Int
    /**
     * Returns the weak (direct) signal being emitted by this part to the specified side
     */
    def weakPowerLevel(side:Int):Int
    /**
     * Returns true if this part can connect to redstone on the specified side. Blocking parts like covers will be handled by RedstoneInteractions
     */
    def canConnectRedstone(side:Int):Boolean
}

/**
 * For parts like wires that adhere to a specific face, reduces redstone connections to the specific edge between two faces.
 * Should be implemented on parts implementing TFacePart
 */
trait IFaceRedstonePart extends IRedstonePart
{
    /**
     * Return the face to which this redstone part is attached
     */
    def getFace:Int
}

/**
 * For parts that want to define their own connection masks (like center-center parts)
 */
trait IMaskedRedstonePart extends IRedstonePart
{
    /**
     * Returns the redstone connection mask for this part on side. See IRedstoneConnector for mask definition
     */
    def getConnectionMask(side:Int):Int
}

/**
 * Interface for tile entities which split their redstone connections into a mask for each side (edges and center)
 * 
 * All connection masks are a 5 bit map. 
 * The lowest 4 bits correspond to the connection toward the face specified Rotation.rotateSide(side&6, b) where b is the bit index from lowest to highest.
 * Bit 5 corresponds to a connection opposite side.
 */
trait IRedstoneConnector
{
    /**
     * Returns the redstone connection mask for this tile on side.
     */
    def getConnectionMask(side:Int):Int
    /**
     * Returns the weak power level provided by this tile on side through mask
     */
    def weakPowerLevel(side:Int, mask:Int):Int
}

/**
 * Internal interface for TileMultipart instances hosting IRedstonePart
 */
trait IRedstoneTile extends IRedstoneConnector
{
    /**
     * Returns a mask of spaces through which a wire could connect on side
     */
    def openConnections(side:Int):Int
}

/**
 * Block version of IRedstoneConnector
 */
trait IRedstoneConnectorBlock
{
    def getConnectionMask(world:IBlockAccess, x:Int, y:Int, z:Int, side:Int):Int
    def weakPowerLevel(world:IBlockAccess, x:Int, y:Int, z:Int, side:Int, mask:Int):Int
}

/**
 * static helper class for calculating various things about redstone.
 * Indirect power (also known as strong power) is not handled here, just use world.getIndirectPowerTo
 * Masks are defined in IRedstoneConnector
 */
object RedstoneInteractions
{
    import net.minecraft.util.Facing._
    
    val vanillaSideMap = Array(-2, -1, 0, 2, 3, 1)
    val sideVanillaMap = Array(1, 2, 5, 3, 4)
    
    /**
     * Get the direct power to p on side
     */
    def getPowerTo(p:TMultiPart, side:Int):Int =
    {
        val tile = p.tile
        return getPowerTo(tile.worldObj, tile.xCoord, tile.yCoord, tile.zCoord, side,
                tile.asInstanceOf[IRedstoneTile].openConnections(side)&connectionMask(p, side))
    }
    
    /**
     * Get the direct power level to space (x, y, z) on side with mask
     */
    def getPowerTo(world:World, x:Int, y:Int, z:Int, side:Int, mask:Int):Int =
        getPower(world, x+offsetsXForSide(side), y+offsetsYForSide(side), z+offsetsZForSide(side), side^1, mask)
    
    /**
     * Get the direct power level provided by space (x, y, z) on side with mask
     */
    def getPower(world:World, x:Int, y:Int, z:Int, side:Int, mask:Int):Int =
    {
        val tile = world.getBlockTileEntity(x, y, z)
        if(tile.isInstanceOf[IRedstoneConnector])
            return tile.asInstanceOf[IRedstoneConnector].weakPowerLevel(side, mask)
        
        val block = Block.blocksList(world.getBlockId(x, y, z))
        if(block == null)
            return 0
        
        if(block.isInstanceOf[IRedstoneConnectorBlock])
            return block.asInstanceOf[IRedstoneConnectorBlock].weakPowerLevel(world, x, y, z, side, mask)
        
        val vmask = vanillaConnectionMask(block, world, x, y, z, side, true)
        if((vmask&mask) > 0)
        {
            var m = world.getIndirectPowerLevelTo(x, y, z, side^1)
            if(m < 15 && block == Block.redstoneWire)
                m = Math.max(m, world.getBlockMetadata(x, y, z))//painful vanilla kludge
            return m
        }
        return 0
    }
    
    def vanillaToSide(vside:Int) = sideVanillaMap(vside+1)
    
    /**
     * Get the connection mask of the block on side of (x, y, z).
     * @param power, whether the connection mask is for signal transfer or visual connection. (some blocks accept power without visual connection)
     */
    def otherConnectionMask(world:IBlockAccess, x:Int, y:Int, z:Int, side:Int, power:Boolean):Int =
        getConnectionMask(world, x+offsetsXForSide(side), y+offsetsYForSide(side), z+offsetsZForSide(side), side^1, power)
    
    /**
     * Get the connection mask of part on side
     */
    def connectionMask(p:TMultiPart, side:Int):Int =
    {
        if(p.isInstanceOf[IRedstonePart] && p.asInstanceOf[IRedstonePart].canConnectRedstone(side))
        {
            if(p.isInstanceOf[IFaceRedstonePart])
            {
                val fside = p.asInstanceOf[IFaceRedstonePart].getFace
                if((side&6) == (fside&6))
                    return 0x10
                
                return 1<<rotationTo(side&6, fside)
            }
            else if(p.isInstanceOf[IMaskedRedstonePart])
            {
                return p.asInstanceOf[IMaskedRedstonePart].getConnectionMask(side)
            }
            return 0x1F
        }
        return 0
    }
    
    /**
     * @param power If true, don't test canConnectRedstone on blocks, just get a power transmission mask rather than a visual connection
     */
    def getConnectionMask(world:IBlockAccess, x:Int, y:Int, z:Int, side:Int, power:Boolean):Int =
    {
        val tile = world.getBlockTileEntity(x, y, z)
        if(tile.isInstanceOf[IRedstoneConnector])
            return tile.asInstanceOf[IRedstoneConnector].getConnectionMask(side)
        
        val block = Block.blocksList(world.getBlockId(x, y, z))
        if(block == null)
            return 0
        
        if(block.isInstanceOf[IRedstoneConnectorBlock])
            return block.asInstanceOf[IRedstoneConnectorBlock].getConnectionMask(world, x, y, z, side)
        
        return vanillaConnectionMask(block, world, x, y, z, side, power)
    }
    
    /**
     * Returns the connection mask for a vanilla block
     */
    def vanillaConnectionMask(block:Block, world:IBlockAccess, x:Int, y:Int, z:Int, side:Int, power:Boolean):Int =
    {
        if(block == Block.torchRedstoneActive || block == Block.torchRedstoneIdle || block == Block.lever || block == Block.stoneButton || block == Block.woodenButton)
            return 0x1F
        
        if(side == 0)//vanilla doesn't handle side 0
        {
            if(power)
                return 0x1F
            
            return 0
        }
        
        if(block == Block.redstoneWire)
        {
            if(side != 1)
                return 4
            return 0x1F
        }
        
        if(block == Block.redstoneComparatorActive || block == Block.redstoneComparatorIdle)
        {
            if(side != 1)
                return 4
            return 0
        }
        
        val vside = vanillaSideMap(side)
        if(block == Block.redstoneRepeaterActive || block == Block.redstoneRepeaterIdle)//stupid minecraft hardcodes
        {
             val meta = world.getBlockMetadata(x, y, z)
            if(vside == (meta & 3) || vside == Direction.rotateOpposite(meta & 3))
                 return 4
             return 0
        }
        
        if(power || block.canConnectRedstone(world, x, y, z, vside))//some blocks accept power without visualising connections
            return 0x1F
        
        return 0
    }
}