package mekanism.common.network.to_client;

import io.netty.buffer.ByteBuf;
import java.util.function.BooleanSupplier;
import java.util.function.IntFunction;
import mekanism.client.render.RenderTickHandler;
import mekanism.common.Mekanism;
import mekanism.common.config.MekanismConfig;
import mekanism.common.lib.effect.BoltEffect;
import mekanism.common.lib.effect.BoltEffect.BoltRenderInfo;
import mekanism.common.lib.effect.BoltEffect.SpawnFunction;
import mekanism.common.network.IMekanismPacket;
import mekanism.common.network.PacketUtils;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.util.ByIdMap;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

public record PacketLightningRender(LightningPreset preset, int renderer, Vec3 start, Vec3 end, int segments) implements IMekanismPacket {

    public static final CustomPacketPayload.Type<PacketLightningRender> TYPE = new CustomPacketPayload.Type<>(Mekanism.rl("render_bolt"));
    public static final StreamCodec<ByteBuf, PacketLightningRender> STREAM_CODEC = StreamCodec.composite(
          LightningPreset.STREAM_CODEC, PacketLightningRender::preset,
          ByteBufCodecs.VAR_INT, PacketLightningRender::renderer,
          PacketUtils.VEC3_STREAM_CODEC, PacketLightningRender::start,
          PacketUtils.VEC3_STREAM_CODEC, PacketLightningRender::end,
          ByteBufCodecs.VAR_INT, PacketLightningRender::segments,
          PacketLightningRender::new
    );

    @NotNull
    @Override
    public CustomPacketPayload.Type<PacketLightningRender> type() {
        return TYPE;
    }

    @Override
    public void handle(IPayloadContext context) {
        if (preset.shouldAdd.getAsBoolean()) {
            RenderTickHandler.renderBolt(renderer, preset.boltCreator.create(start, end, segments));
        }
    }

    @FunctionalInterface
    public interface BoltCreator {

        BoltEffect create(Vec3 start, Vec3 end, int segments);
    }

    public enum LightningPreset {
        MAGNETIC_ATTRACTION(MekanismConfig.client.renderMagneticAttractionParticles, (start, end, segments) ->
              new BoltEffect(BoltRenderInfo.ELECTRICITY, start, end, segments).size(0.04F).lifespan(8).spawn(SpawnFunction.noise(8, 4))),
        TOOL_AOE(MekanismConfig.client.renderToolAOEParticles, (start, end, segments) ->
              new BoltEffect(BoltRenderInfo.ELECTRICITY, start, end, segments).size(0.015F).lifespan(12).spawn(SpawnFunction.NO_DELAY));

        public static final IntFunction<LightningPreset> BY_ID = ByIdMap.continuous(LightningPreset::ordinal, values(), ByIdMap.OutOfBoundsStrategy.WRAP);
        public static final StreamCodec<ByteBuf, LightningPreset> STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, LightningPreset::ordinal);

        private final BooleanSupplier shouldAdd;
        private final BoltCreator boltCreator;

        LightningPreset(BooleanSupplier shouldAdd, BoltCreator boltCreator) {
            this.shouldAdd = shouldAdd;
            this.boltCreator = boltCreator;
        }
    }
}