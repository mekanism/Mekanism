package mekanism.common.network;

import java.util.Set;
import java.util.function.Supplier;
import mekanism.api.MekanismAPI;
import mekanism.common.Mekanism;
import net.minecraft.block.Block;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent.Context;

public class PacketBoxBlacklist {

    public static PacketBoxBlacklist decode(PacketBuffer buf) {
        Set<Block> boxIgnore = MekanismAPI.getBoxIgnore();
        buf.writeInt(boxIgnore.size());
        for (Block info : boxIgnore) {
            buf.writeInt(Block.getIdFromBlock(info));
        }
        Set<String> boxModIgnore = MekanismAPI.getBoxModIgnore();
        buf.writeInt(boxModIgnore.size());
        for (String modid : boxModIgnore) {
            buf.writeString(modid);
        }
        return new PacketBoxBlacklist();
    }

    public static void encode(PacketBoxBlacklist pkt, PacketBuffer buf) {
        MekanismAPI.getBoxIgnore().clear();
        int amount = buf.readInt();
        for (int i = 0; i < amount; i++) {
            MekanismAPI.addBoxBlacklist(Block.getBlockById(buf.readInt()));
        }
        int amountMods = buf.readInt();
        for (int i = 0; i < amountMods; i++) {
            MekanismAPI.addBoxBlacklistMod(buf.readString());
        }
        Mekanism.logger.info("Received Cardboard Box blacklist entries from server (" + amount + " explicit blocks, " + amountMods + " mod wildcards)");
    }

    public static void handle(PacketBoxBlacklist message, Supplier<Context> context) {
    }
}