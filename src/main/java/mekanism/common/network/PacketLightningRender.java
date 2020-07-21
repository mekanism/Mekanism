package mekanism.common.network;

import java.util.function.Supplier;
import mekanism.client.render.RenderTickHandler;
import mekanism.common.lib.effect.BoltEffect;
import mekanism.common.lib.effect.BoltEffect.BoltRenderInfo;
import mekanism.common.lib.effect.BoltEffect.SpawnFunction;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.fml.network.NetworkEvent.Context;

public class PacketLightningRender {

    private final LightningPreset preset;
    private final Vector3d start;
    private final Vector3d end;
    private final int renderer;
    private final int segments;

    public PacketLightningRender(LightningPreset preset, int renderer, Vector3d start, Vector3d end, int segments) {
        this.preset = preset;
        this.renderer = renderer;
        this.start = start;
        this.end = end;
        this.segments = segments;
    }

    public static void handle(PacketLightningRender message, Supplier<Context> context) {
        context.get().enqueueWork(() -> RenderTickHandler.renderBolt(message.renderer, message.preset.boltCreator.create(message.start, message.end, message.segments)));
        context.get().setPacketHandled(true);
    }

    public static void encode(PacketLightningRender pkt, PacketBuffer buf) {
        buf.writeEnumValue(pkt.preset);
        buf.writeVarInt(pkt.renderer);
        BasePacketHandler.writeVector3d(buf, pkt.start);
        BasePacketHandler.writeVector3d(buf, pkt.end);
        buf.writeVarInt(pkt.segments);
    }

    public static PacketLightningRender decode(PacketBuffer buf) {
        LightningPreset preset = buf.readEnumValue(LightningPreset.class);
        int renderer = buf.readVarInt();
        Vector3d start = BasePacketHandler.readVector3d(buf);
        Vector3d end = BasePacketHandler.readVector3d(buf);
        int segments = buf.readVarInt();
        return new PacketLightningRender(preset, renderer, start, end, segments);
    }

    @FunctionalInterface
    public interface BoltCreator {

        BoltEffect create(Vector3d start, Vector3d end, int segments);
    }

    public enum LightningPreset {
        MAGNETIC_ATTRACTION((start, end, segments) -> new BoltEffect(BoltRenderInfo.ELECTRICITY, start, end, segments).size(0.04F).lifespan(8).spawn(SpawnFunction.noise(8, 4))),
        TOOL_AOE((start, end, segments) -> new BoltEffect(BoltRenderInfo.ELECTRICITY, start, end, segments).size(0.015F).lifespan(12).spawn(SpawnFunction.NO_DELAY));

        private final BoltCreator boltCreator;

        LightningPreset(BoltCreator boltCreator) {
            this.boltCreator = boltCreator;
        }
    }
}