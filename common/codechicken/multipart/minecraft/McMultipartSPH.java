package codechicken.multipart.minecraft;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.NetServerHandler;
import net.minecraft.world.World;
import codechicken.lib.packet.PacketCustom;
import codechicken.lib.packet.PacketCustom.IServerPacketHandler;

public class McMultipartSPH implements IServerPacketHandler
{
    public static Object channel = MinecraftMultipartMod.instance;
    
    @Override
    public void handlePacket(PacketCustom packet, NetServerHandler nethandler, EntityPlayerMP sender)
    {
        switch(packet.getType())
        {
            case 1:
                EventHandler.place(sender, sender.worldObj);
            break;
        }
    }

    public static void spawnBurnoutSmoke(World world, int x, int y, int z)
    {
        new PacketCustom(channel, 1).writeCoord(x, y, z).sendToChunk(world, x>>4, z>>4);
    }
}
