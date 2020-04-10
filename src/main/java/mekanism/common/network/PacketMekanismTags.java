package mekanism.common.network;

import java.util.function.Supplier;
import mekanism.api.chemical.gas.GasTags;
import mekanism.api.chemical.infuse.InfuseTypeTags;
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
            GasTags.setCollection(message.tags.getGases());
            InfuseTypeTags.setCollection(message.tags.getInfuseTypes());
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