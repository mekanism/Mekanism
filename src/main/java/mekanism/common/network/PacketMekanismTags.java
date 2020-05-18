package mekanism.common.network;

import java.util.function.Supplier;
import mekanism.common.tags.MekanismTagManager;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent.Context;

public class PacketMekanismTags {

    private final MekanismTagManager tags;

    public PacketMekanismTags(MekanismTagManager tags) {
        this.tags = tags;
    }

    public static void handle(PacketMekanismTags message, Supplier<Context> context) {
        context.get().enqueueWork(message.tags::setCollections);
        context.get().setPacketHandled(true);
    }

    public static void encode(PacketMekanismTags pkt, PacketBuffer buf) {
        pkt.tags.write(buf);
    }

    public static PacketMekanismTags decode(PacketBuffer buf) {
        return new PacketMekanismTags(MekanismTagManager.read(buf));
    }
}