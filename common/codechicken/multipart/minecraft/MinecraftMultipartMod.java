package codechicken.multipart.minecraft;

import net.minecraftforge.common.MinecraftForge;
import codechicken.lib.packet.PacketCustom;
import codechicken.lib.packet.PacketCustom.CustomTinyPacketHandler;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.NetworkMod;

@Mod(modid = "McMultipart", acceptedMinecraftVersions="[1.6.4]")
@NetworkMod(clientSideRequired = true, serverSideRequired = true, tinyPacketHandler=CustomTinyPacketHandler.class)
public class MinecraftMultipartMod
{
    @Instance("McMultipart")
    public static MinecraftMultipartMod instance;
    
    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
        new Content().init();
        MinecraftForge.EVENT_BUS.register(new EventHandler());
        PacketCustom.assignHandler(this, new McMultipartSPH());
        if(FMLCommonHandler.instance().getSide().isClient())
            PacketCustom.assignHandler(this, new McMultipartCPH());
            
    }
}
