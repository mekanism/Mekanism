package codechicken.multipart.handler

import codechicken.multipart.BlockMultipart
import cpw.mods.fml.client.registry.RenderingRegistry
import cpw.mods.fml.client.registry.ClientRegistry
import net.minecraft.tileentity.TileEntity
import codechicken.lib.config.ConfigFile
import java.io.File
import codechicken.multipart.handler.MultipartProxy._
import codechicken.multipart.MultipartRenderer
import net.minecraftforge.common.MinecraftForge
import codechicken.multipart.MultipartGenerator
import cpw.mods.fml.relauncher.SideOnly
import cpw.mods.fml.relauncher.Side
import codechicken.lib.packet.PacketCustom
import cpw.mods.fml.client.registry.KeyBindingRegistry
import codechicken.multipart.ControlKeyHandler
import cpw.mods.fml.common.network.NetworkRegistry
import cpw.mods.fml.common.registry.TickRegistry
import net.minecraft.block.Block
import net.minecraft.world.ChunkCoordIntPair
import codechicken.lib.vec.BlockCoord
import codechicken.lib.world.WorldExtensionManager
import codechicken.multipart.TickScheduler

class MultipartProxy_serverImpl
{
    def preInit(cfgdir:File)
    {
        config = new ConfigFile(new File(cfgdir, "multipart.cfg"))
            .setComment("Multipart API config file")
        
        MultipartGenerator.registerTrait("codechicken.multipart.TSlottedPart", "codechicken.multipart.scalatraits.TSlottedTile")
        MultipartGenerator.registerTrait("net.minecraftforge.fluids.IFluidHandler", "codechicken.multipart.scalatraits.TFluidHandlerTile")
        MultipartGenerator.registerTrait("codechicken.multipart.JPartialOcclusion", "codechicken.multipart.scalatraits.TPartialOcclusionTile")
        MultipartGenerator.registerTrait("codechicken.multipart.IRedstonePart", "codechicken.multipart.scalatraits.TRedstoneTile")
        MultipartGenerator.registerTrait("codechicken.multipart.IRandomDisplayTick", "codechicken.multipart.scalatraits.TRandomDisplayTickTile", null)
        MultipartGenerator.registerTrait("codechicken.multipart.INeighborTileChange", null, "codechicken.multipart.scalatraits.TTileChangeTile")
        
        MultipartSaveLoad.hookLoader()
    }
    
    def init()
    {
        block = new BlockMultipart(config.getTag("block.id").getIntValue(getFreeBlockID(1281)))
        block.setUnlocalizedName("ccmultipart")
    }
    
    def postInit()
    {
        MinecraftForge.EVENT_BUS.register(MultipartEventHandler)
        PacketCustom.assignHandler(MultipartSPH.channel, MultipartSPH)
        NetworkRegistry.instance.registerConnectionHandler(MultipartEventHandler)
        TickRegistry.registerTickHandler(MultipartEventHandler, Side.SERVER)
        
        WorldExtensionManager.registerWorldExtension(TickScheduler)
    }
    
    def getFreeBlockID(preferred:Int):Int =
    {
        for(i <- (preferred until 4096) ++ (preferred-1 until 255))
            if(Block.blocksList(i) == null)
                return i
        throw new RuntimeException("Out of Block IDs")
    }
    
    def onTileClassBuilt(t:Class[_ <: TileEntity])
    {
        MultipartSaveLoad.registerTileClass(t)   
    }
}

class MultipartProxy_clientImpl extends MultipartProxy_serverImpl
{
    @SideOnly(Side.CLIENT)
    override def postInit()
    {
        super.postInit()
        RenderingRegistry.registerBlockHandler(MultipartRenderer)
        PacketCustom.assignHandler(MultipartCPH.channel, MultipartCPH)
        PacketCustom.assignHandler(MultipartCPH.registryChannel, 1, 127, MultipartCPH)
        KeyBindingRegistry.registerKeyBinding(ControlKeyHandler)
    }
    
    @SideOnly(Side.CLIENT)
    override def onTileClassBuilt(t:Class[_ <: TileEntity])
    {
        super.onTileClassBuilt(t)
        ClientRegistry.bindTileEntitySpecialRenderer(t, MultipartRenderer)
    }
}

object MultipartProxy extends MultipartProxy_clientImpl
{
    var block:BlockMultipart = _
    var config:ConfigFile = _
    
    def indexInChunk(cc:ChunkCoordIntPair, i:Int) = new BlockCoord(cc.chunkXPos<<4|i&0xF, (i>>8)&0xFF, cc.chunkZPos<<4|(i&0xF0)>>4)
    def indexInChunk(pos:BlockCoord) = pos.x&0xF|pos.y<<8|(pos.z&0xF)<<4
}