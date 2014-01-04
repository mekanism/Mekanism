package codechicken.multipart

import codechicken.lib.render.CCRenderState
import codechicken.lib.vec.Vector3
import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler
import cpw.mods.fml.client.registry.RenderingRegistry
import net.minecraft.block.Block
import net.minecraft.client.renderer.RenderBlocks
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer
import net.minecraft.tileentity.TileEntity
import net.minecraft.world.IBlockAccess
import net.minecraft.client.Minecraft
import cpw.mods.fml.relauncher.SideOnly
import cpw.mods.fml.relauncher.Side
import codechicken.lib.raytracer.ExtendedMOP
import codechicken.lib.lighting.LazyLightMatrix

/**
 * Internal class for rendering callbacks. Should be moved to the handler package
 */
@SideOnly(Side.CLIENT)
object MultipartRenderer extends TileEntitySpecialRenderer with ISimpleBlockRenderingHandler
{
    TileMultipart.renderID = RenderingRegistry.getNextAvailableRenderId
    private val olm = new LazyLightMatrix
    
    var pass:Int = 0
    
    override def renderTileEntityAt(t:TileEntity, x:Double, y:Double, z:Double, f:Float)
    {
        val tmpart = t.asInstanceOf[TileMultipartClient]
        if(tmpart.partList.isEmpty)
            return

        CCRenderState.reset()
        CCRenderState.pullLightmap()
        CCRenderState.useNormals(true)
        
        val pos = new Vector3(x, y, z)
        tmpart.renderDynamic(pos, f, pass)
    }
    
    override def getRenderId = TileMultipart.renderID
    
    override def renderWorldBlock(world:IBlockAccess, x:Int, y:Int, z:Int, block:Block, modelId:Int, renderer:RenderBlocks):Boolean =
    {
        val t = world.getBlockTileEntity(x, y, z)
        if(!t.isInstanceOf[TileMultipartClient])
            return false

        val tmpart = t.asInstanceOf[TileMultipartClient]
        if(tmpart.partList.isEmpty)
            return false
        
        if(renderer.hasOverrideBlockTexture)
        {
            val hit = Minecraft.getMinecraft.objectMouseOver
            if(hit != null && hit.blockX == x && hit.blockY == y && hit.blockZ == z && ExtendedMOP.getData(hit).isInstanceOf[(_, _)])
            {
                val hitInfo:(Int, _) = ExtendedMOP.getData(hit)
                if(hitInfo._1.isInstanceOf[Int] && hitInfo._1 >= 0 && hitInfo._1 < tmpart.partList.size)
                    tmpart.partList(hitInfo._1).drawBreaking(renderer)
            }
            return false
        }
        
        CCRenderState.reset()
        CCRenderState.useModelColours(true)
        val pos = new Vector3(x, y, z)
        olm.setPos(world, x, y, z)
        tmpart.renderStatic(pos, olm, pass)
        return true
    }
    
    override def renderInventoryBlock(block:Block, meta:Int, modelId:Int, renderer:RenderBlocks) {}
    
    override def shouldRender3DInInventory = false
}
