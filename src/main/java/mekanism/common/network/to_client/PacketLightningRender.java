package mekanism.common.network.to_client;

import java.util.function.BooleanSupplier;
import mekanism.client.render.RenderTickHandler;
import mekanism.common.config.MekanismConfig;
import mekanism.common.lib.effect.BoltEffect;
import mekanism.common.lib.effect.BoltEffect.BoltRenderInfo;
import mekanism.common.lib.effect.BoltEffect.SpawnFunction;
import mekanism.common.network.BasePacketHandler;
import mekanism.common.network.IMekanismPacket;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.fml.network.NetworkEvent;

public class PacketLightningRender implements IMekanismPacket {

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

    @Override
    public void handle(NetworkEvent.Context context) {
        if (preset.shouldAdd.getAsBoolean()) {
            RenderTickHandler.renderBolt(renderer, preset.boltCreator.create(start, end, segments));
        }
    }

    @Override
    public void encode(PacketBuffer buffer) {
        buffer.writeEnum(preset);
        buffer.writeVarInt(renderer);
        BasePacketHandler.writeVector3d(buffer, start);
        BasePacketHandler.writeVector3d(buffer, end);
        buffer.writeVarInt(segments);
    }

    public static PacketLightningRender decode(PacketBuffer buffer) {
        LightningPreset preset = buffer.readEnum(LightningPreset.class);
        int renderer = buffer.readVarInt();
        Vector3d start = BasePacketHandler.readVector3d(buffer);
        Vector3d end = BasePacketHandler.readVector3d(buffer);
        int segments = buffer.readVarInt();
        return new PacketLightningRender(preset, renderer, start, end, segments);
    }

    @FunctionalInterface
    public interface BoltCreator {

        BoltEffect create(Vector3d start, Vector3d end, int segments);
    }

    public enum LightningPreset {
        MAGNETIC_ATTRACTION(MekanismConfig.client.renderMagneticAttractionParticles, (start, end, segments) ->
              new BoltEffect(BoltRenderInfo.ELECTRICITY, start, end, segments).size(0.04F).lifespan(8).spawn(SpawnFunction.noise(8, 4))),
        TOOL_AOE(MekanismConfig.client.renderToolAOEParticles, (start, end, segments) ->
              new BoltEffect(BoltRenderInfo.ELECTRICITY, start, end, segments).size(0.015F).lifespan(12).spawn(SpawnFunction.NO_DELAY));

        private final BooleanSupplier shouldAdd;
        private final BoltCreator boltCreator;

        LightningPreset(BooleanSupplier shouldAdd, BoltCreator boltCreator) {
            this.shouldAdd = shouldAdd;
            this.boltCreator = boltCreator;
        }
    }
}