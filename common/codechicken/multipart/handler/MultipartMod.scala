package codechicken.multipart.handler

import cpw.mods.fml.common.network.NetworkMod
import cpw.mods.fml.common.Mod
import cpw.mods.fml.common.event.FMLPostInitializationEvent
import cpw.mods.fml.common.event.FMLPreInitializationEvent
import cpw.mods.fml.common.event.FMLInitializationEvent
import cpw.mods.fml.common.Mod.EventHandler
import cpw.mods.fml.common.event.FMLServerAboutToStartEvent
import codechicken.multipart.MultiPartRegistry
import codechicken.lib.packet.PacketCustom.CustomTinyPacketHandler

@Mod(modid = "ForgeMultipart", acceptedMinecraftVersions = "[1.6.4]", 
        modLanguage="scala")
@NetworkMod(clientSideRequired = true, serverSideRequired = true, tinyPacketHandler=classOf[CustomTinyPacketHandler])
object MultipartMod
{
    @EventHandler
    def preInit(event:FMLPreInitializationEvent)
    {
        MultipartProxy.preInit(event.getModConfigurationDirectory)
    }
    
    @EventHandler
    def init(event:FMLInitializationEvent)
    {
        MultipartProxy.init()
    }
    
    @EventHandler
    def postInit(event:FMLPostInitializationEvent)
    {
        if(MultiPartRegistry.required)
        {
            MultiPartRegistry.postInit()
            MultipartProxy.postInit()
        }
    }
    
    @EventHandler
    def beforeServerStart(event:FMLServerAboutToStartEvent)
    {
        MultiPartRegistry.beforeServerStart()
    }
}