package mekanism.common.network;

import java.util.function.Supplier;
import mekanism.api.gas.GasTags;
import mekanism.common.tags.MekanismTagManager;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent.Context;

public class PacketMekanismTags {

    private MekanismTagManager tags;

    public PacketMekanismTags(MekanismTagManager tags) {
        this.tags = tags;
    }

    public static void handle(PacketMekanismTags message, Supplier<Context> context) {
        context.get().enqueueWork(() -> {
            //TODO: 1.14, Only do it if not local
            //if (!netManager.isLocalChannel()) {
                GasTags.setCollection(message.tags.getGases());
            //}
        });
        context.get().setPacketHandled(true);
    }

    public static void encode(PacketMekanismTags pkt, PacketBuffer buf) {
        pkt.tags.write(buf);
    }

    public static PacketMekanismTags decode(PacketBuffer buf) {
        return new PacketMekanismTags(MekanismTagManager.read(buf));
    }
}