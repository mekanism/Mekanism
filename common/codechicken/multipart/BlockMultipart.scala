package codechicken.multipart

import java.util.List
import java.lang.Iterable
import net.minecraft.block.Block
import net.minecraft.block.material.Material
import net.minecraft.entity.Entity
import net.minecraft.util.AxisAlignedBB
import net.minecraft.world.World
import net.minecraftforge.common.ForgeDirection
import net.minecraft.util.Vec3
import net.minecraft.util.MovingObjectPosition
import codechicken.lib.raytracer.RayTracer
import net.minecraft.entity.player.EntityPlayer
import java.util.Random
import java.util.ArrayList
import net.minecraft.item.ItemStack
import net.minecraft.client.particle.EffectRenderer
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.texture.IconRegister
import codechicken.lib.render.TextureUtils
import net.minecraft.world.IBlockAccess
import codechicken.lib.raytracer.ExtendedMOP
import scala.collection.JavaConversions._

object BlockMultipart
{
    def getTile(world:IBlockAccess, x:Int, y:Int, z:Int):TileMultipart = 
    {
        val tile = world.getBlockTileEntity(x, y, z)
        if(tile.isInstanceOf[TileMultipart]) 
            tile.asInstanceOf[TileMultipart] 
        else
            null
    }
    
    def getClientTile(world:IBlockAccess, x:Int, y:Int, z:Int):TileMultipartClient = 
    {
        val tile = world.getBlockTileEntity(x, y, z)
        if(tile.isInstanceOf[TileMultipartClient]) 
            tile.asInstanceOf[TileMultipartClient]
        else
            null
    }
    
    def reduceMOP(hit:MovingObjectPosition):(Int, ExtendedMOP) = {
        val ehit = hit.asInstanceOf[ExtendedMOP]
        val data:(Int, _) = ExtendedMOP.getData(hit)
        return (data._1, new ExtendedMOP(ehit, data._2, ehit.dist))
    }
    
    def drawHighlight(world:World, player:EntityPlayer, hit:MovingObjectPosition, frame:Float):Boolean =
    {
        val tile = getTile(world, hit.blockX, hit.blockY, hit.blockZ)
        if(tile == null)
            return false
        
        val (index, mop) = reduceMOP(hit)
        if(tile.partList(index).drawHighlight(mop, player, frame))
            return true
        
        tile.partList(index).collisionRayTrace(RayTracer.getStartVec(player), RayTracer.getEndVec(player))
        return false
    }
}

/**
 * Block class for all multiparts, should be internal use only.
 */
class BlockMultipart(id:Int) extends Block(id, Material.rock)
{
    import BlockMultipart._
    
    override def hasTileEntity(meta:Int = 0) = true
    
    override def isBlockSolidOnSide(world:World, x:Int, y:Int, z:Int, side:ForgeDirection):Boolean =
        getTile(world, x, y, z) match {
            case null => false
            case tile => tile.isSolid(side.ordinal())
        }

    override def onNeighborBlockChange(world:World, x:Int, y:Int, z:Int, id:Int)
    {
        val tile = getTile(world, x, y, z)
        if(tile != null) 
            tile.onNeighborBlockChange()
    }
    
    override def collisionRayTrace(world:World, x:Int, y:Int, z:Int, start:Vec3, end:Vec3):ExtendedMOP = 
        getTile(world, x, y, z) match {
            case null => null
            case tile => tile.collisionRayTrace(start, end) 
        }

    def rayTraceAll(world:World, x:Int, y:Int, z:Int, start:Vec3, end:Vec3):Iterable[ExtendedMOP] =
        getTile(world, x, y, z) match {
            case null => Seq()
            case tile => tile.rayTraceAll(start, end) 
        }

    override def removeBlockByPlayer(world:World, player:EntityPlayer, x:Int, y:Int, z:Int):Boolean =
    {
        val hit = RayTracer.retraceBlock(world, player, x, y, z)
        val tile = getTile(world, x, y, z)
        
        if(hit == null || tile == null)
        {
            dropAndDestroy(world, x, y, z)
            return true
        }
        
        val (index, mop) = reduceMOP(hit)
        if(world.isRemote)
        {
            tile.partList(index).addDestroyEffects(mop, Minecraft.getMinecraft.effectRenderer)
            return true
        }
        
        tile.harvestPart(index, mop, player)
        return world.getBlockTileEntity(x, y, z) == null
    }
    
    def dropAndDestroy(world:World, x:Int, y:Int, z:Int)
    {
        val tile = getTile(world, x, y, z)
        if(tile != null && !world.isRemote)
            tile.dropItems(getBlockDropped(world, x, y, z, 0, 0))
        
        world.setBlockToAir(x, y, z)
    }
    
    override def quantityDropped(meta:Int, fortune:Int, random:Random) = 0
    
    override def getBlockDropped(world:World, x:Int, y:Int, z:Int, meta:Int, fortune:Int):ArrayList[ItemStack] =
    {
        val ai = new ArrayList[ItemStack]()
        if(world.isRemote)
            return ai
        
        val tile = getTile(world, x, y, z)
        if(tile != null)
            tile.partList.foreach(part => part.getDrops.foreach(item => ai.add(item)))
        
        return ai
    }
    
    override def addCollisionBoxesToList(world:World, x:Int, y:Int, z:Int, ebb:AxisAlignedBB, list$:List[_], entity:Entity)
    {
        val list = list$.asInstanceOf[List[AxisAlignedBB]]
        val tile = getTile(world, x, y, z)
        if(tile != null)
            tile.partList.foreach(part => 
                part.getCollisionBoxes.foreach{c => 
                        val aabb = c.toAABB.offset(x, y, z)
                        if(aabb.intersectsWith(ebb))
                            list.add(aabb)
                })
    }
    
    override def addBlockHitEffects(world:World, hit:MovingObjectPosition, effectRenderer:EffectRenderer):Boolean =
    {
        val tile = getClientTile(world, hit.blockX, hit.blockY, hit.blockZ)
        if(tile != null) {
            val (index, mop) = reduceMOP(hit)
            tile.partList(index).addHitEffects(mop, effectRenderer)
        }
        
        return true
    }
    
    override def addBlockDestroyEffects(world:World, x:Int, y:Int, z:Int, s:Int, effectRenderer:EffectRenderer) = true
    
    override def renderAsNormalBlock() = false
    
    override def isOpaqueCube = false
    
    override def getRenderType = TileMultipart.renderID
    
    override def isAirBlock(world:World, x:Int, y:Int, z:Int):Boolean =
        getTile(world, x, y, z) match {
            case null => true
            case tile => tile.partList.isEmpty
        }

    override def isBlockReplaceable(world:World, x:Int, y:Int, z:Int) = isAirBlock(world, x, y, z)
    
    override def getRenderBlockPass = 1
    
    override def canRenderInPass(pass:Int):Boolean = 
    {
        MultipartRenderer.pass = pass
        return true
    }
    
    override def getPickBlock(hit:MovingObjectPosition, world:World, x:Int, y:Int, z:Int):ItemStack =
    {
        val tile = getTile(world, x, y, z)
        if(tile != null)
        {
            val (index, mop) = reduceMOP(hit)
            return tile.partList(index).pickItem(mop)
        }
        return null
    }
    
    override def getPlayerRelativeBlockHardness(player:EntityPlayer, world:World, x:Int, y:Int, z:Int):Float = 
    {
        val hit = RayTracer.retraceBlock(world, player, x, y, z)
        val tile = getTile(world, x, y, z)
        if(hit != null && tile != null) {
            val (index, mop) = reduceMOP(hit)
            return tile.partList(index).getStrength(mop, player)/30F
        }
        
        return 1/100F
    }
    
    /**
     * Kludge to set PROTECTED blockIcon to a blank icon 
     */
    override def registerIcons(register:IconRegister)
    {
        val icon = TextureUtils.getBlankIcon(16, register)
        setTextureName(icon.getIconName)
        super.registerIcons(register)
    }
    
    override def getLightValue(world:IBlockAccess, x:Int, y:Int, z:Int):Int = 
        getTile(world, x, y, z) match {
            case null => 0
            case tile => tile.getLightValue 
        }

    override def randomDisplayTick(world:World, x:Int, y:Int, z:Int, random:Random)
    {
        val tile = getClientTile(world, x, y, z)
        if(tile != null)
            tile.randomDisplayTick(random)
    }
    
    override def onBlockActivated(world:World, x:Int, y:Int, z:Int, player:EntityPlayer, side:Int, hitX:Float, hitY:Float, hitZ:Float):Boolean =
    {
        val hit = RayTracer.retraceBlock(world, player, x, y, z)
        if(hit == null)
            return false
        
        val tile = getTile(world, x, y, z)
        if(tile == null) 
            return false
        
        val (index, mop) = reduceMOP(hit)
        return tile.partList(index).activate(player, mop, player.getHeldItem)
    }
    
    override def onBlockClicked(world:World, x:Int, y:Int, z:Int, player:EntityPlayer)
    {
        val hit = RayTracer.retraceBlock(world, player, x, y, z)
        if(hit == null)
            return
        
        val tile = getTile(world, x, y, z)
        if(tile == null) 
            return
        
        val (index, mop) = reduceMOP(hit)
        tile.partList(index).click(player, mop, player.getHeldItem)
    }
    
    override def isProvidingStrongPower(world:IBlockAccess, x:Int, y:Int, z:Int, side:Int):Int =
        getTile(world, x, y, z) match {
            case null => 0
            case tile => tile.strongPowerLevel(side^1)
        }

    override def isProvidingWeakPower(world:IBlockAccess, x:Int, y:Int, z:Int, side:Int):Int =
        getTile(world, x, y, z) match {
            case null => 0
            case tile => tile.weakPowerLevel(side^1)
        }

    override def canConnectRedstone(world:IBlockAccess, x:Int, y:Int, z:Int, side:Int):Boolean =
        getTile(world, x, y, z) match {
            case null => false
            case tile => tile.canConnectRedstone(side)
        }

    override def onEntityCollidedWithBlock(world:World, x:Int, y:Int, z:Int, entity:Entity)
    {
        val tile = getTile(world, x, y, z)
        if(tile != null)
            tile.onEntityCollision(entity)
    }
    
    override def onNeighborTileChange(world:World, x:Int, y:Int, z:Int, tileX:Int, tileY:Int, tileZ:Int)
    {
        getTile(world, x, y, z) match {
            case null => world.setBlockToAir(x, y, z)
            case tile => tile.onNeighborTileChange(tileX, tileY, tileZ)
        }
    }

    override def getExplosionResistance(entity:Entity, world:World, x:Int, y:Int, z:Int, explosionX:Double, explosionY:Double, explosionZ:Double):Float =
        getTile(world, x, y, z) match {
            case null => 0
            case tile => tile.getExplosionResistance(entity)
        }

    override def weakTileChanges() = true
    
    override def canProvidePower = true
}