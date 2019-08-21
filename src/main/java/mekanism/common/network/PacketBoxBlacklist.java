package mekanism.common.network;

import java.util.Set;
import java.util.function.Supplier;
import mekanism.api.MekanismAPI;
import mekanism.common.Mekanism;
import net.minecraft.block.Block;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent.Context;
import net.minecraftforge.registries.ForgeRegistries;

public class PacketBoxBlacklist {

    //TODO: Actually store the data in the Packet, so that it doesn't get handled until handle is called

    public static void handle(PacketBoxBlacklist message, Supplier<Context> context) {
        context.get().setPacketHandled(true);
    }

    public static void encode(PacketBoxBlacklist pkt, PacketBuffer buf) {
        Set<Block> boxIgnore = MekanismAPI.getBoxIgnore();
        buf.writeInt(boxIgnore.size());
        for (Block info : boxIgnore) {
            buf.writeResourceLocation(info.getRegistryName());
        }
        Set<String> boxModIgnore = MekanismAPI.getBoxModIgnore();
        buf.writeInt(boxModIgnore.size());
        for (String modid : boxModIgnore) {
            buf.writeString(modid);
        }
    }

    public static PacketBoxBlacklist decode(PacketBuffer buf) {
        //TODO: This is wrong
        MekanismAPI.getBoxIgnore().clear();
        int amount = buf.readInt();
        for (int i = 0; i < amount; i++) {
            MekanismAPI.addBoxBlacklist(ForgeRegistries.BLOCKS.getValue(buf.readResourceLocation()));
        }
        int amountMods = buf.readInt();
        for (int i = 0; i < amountMods; i++) {
            MekanismAPI.addBoxBlacklistMod(buf.readString());
        }
        Mekanism.logger.info("Received Cardboard Box blacklist entries from server (" + amount + " explicit blocks, " + amountMods + " mod wildcards)");
        return new PacketBoxBlacklist();
    }
}