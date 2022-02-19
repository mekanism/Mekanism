package mekanism.common.network.to_client;

import mekanism.common.network.IMekanismPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public class PacketStepHeightSync implements IMekanismPacket {

    private final float stepHeight;

    public PacketStepHeightSync(float stepHeight) {
        this.stepHeight = stepHeight;
    }

    @Override
    public void handle(NetworkEvent.Context context) {
        ClientPlayerEntity player = Minecraft.getInstance().player;
        if (player != null) {
            player.maxUpStep = stepHeight;
        }
    }

    @Override
    public void encode(PacketBuffer buffer) {
        buffer.writeFloat(stepHeight);
    }

    public static PacketStepHeightSync decode(PacketBuffer buffer) {
        return new PacketStepHeightSync(buffer.readFloat());
    }
}