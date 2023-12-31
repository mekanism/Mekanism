package mekanism.common.network.to_client;

import java.util.function.BooleanSupplier;
import mekanism.client.render.RenderTickHandler;
import mekanism.common.Mekanism;
import mekanism.common.config.MekanismConfig;
import mekanism.common.lib.effect.BoltEffect;
import mekanism.common.lib.effect.BoltEffect.BoltRenderInfo;
import mekanism.common.lib.effect.BoltEffect.SpawnFunction;
import mekanism.common.network.IMekanismPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;
import org.jetbrains.annotations.NotNull;

public record PacketLightningRender(LightningPreset preset, int renderer, Vec3 start, Vec3 end, int segments) implements IMekanismPacket<PlayPayloadContext> {

    public static final ResourceLocation ID = Mekanism.rl("render_bolt");

    public PacketLightningRender(FriendlyByteBuf buffer) {
        this(buffer.readEnum(LightningPreset.class), buffer.readVarInt(), buffer.readVec3(), buffer.readVec3(), buffer.readVarInt());
    }

    @NotNull
    @Override
    public ResourceLocation id() {
        return ID;
    }

    @Override
    public void handle(PlayPayloadContext context) {
        if (preset.shouldAdd.getAsBoolean()) {
            RenderTickHandler.renderBolt(renderer, preset.boltCreator.create(start, end, segments));
        }
    }

    @Override
    public void write(@NotNull FriendlyByteBuf buffer) {
        buffer.writeEnum(preset);
        buffer.writeVarInt(renderer);
        buffer.writeVec3(start);
        buffer.writeVec3(end);
        buffer.writeVarInt(segments);
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

        private final BooleanSupplier shouldAdd;
        private final BoltCreator boltCreator;

        LightningPreset(BooleanSupplier shouldAdd, BoltCreator boltCreator) {
            this.shouldAdd = shouldAdd;
            this.boltCreator = boltCreator;
        }
    }
}