package mekanism.common.network.to_client.radiation;

import mekanism.common.Mekanism;
import mekanism.common.lib.radiation.RadiationManager;
import mekanism.common.lib.radiation.RadiationManager.LevelAndMaxMagnitude;
import mekanism.common.network.IMekanismPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;
import org.jetbrains.annotations.NotNull;

public record PacketEnvironmentalRadiationData(double radiation, double maxMagnitude) implements IMekanismPacket<PlayPayloadContext> {

    public static final ResourceLocation ID = Mekanism.rl("environmental_radiation");

    public PacketEnvironmentalRadiationData(FriendlyByteBuf buffer) {
        this(buffer.readDouble(), buffer.readDouble());
    }

    public PacketEnvironmentalRadiationData(LevelAndMaxMagnitude levelAndMaxMagnitude) {
        this(levelAndMaxMagnitude.level(), levelAndMaxMagnitude.maxMagnitude());
    }

    @NotNull
    @Override
    public ResourceLocation id() {
        return ID;
    }

    @Override
    public void handle(PlayPayloadContext context) {
        RadiationManager.get().setClientEnvironmentalRadiation(radiation, maxMagnitude);
    }

    @Override
    public void write(@NotNull FriendlyByteBuf buffer) {
        buffer.writeDouble(radiation);
        buffer.writeDouble(maxMagnitude);
    }
}
