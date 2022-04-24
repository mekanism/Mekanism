package mekanism.common.network.to_client;

import mekanism.common.network.IMekanismPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

public class PacketStepHeightSync implements IMekanismPacket {

    private final float stepHeight;

    public PacketStepHeightSync(float stepHeight) {
        this.stepHeight = stepHeight;
    }

    @Override
    public void handle(NetworkEvent.Context context) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player != null) {
            player.maxUpStep = stepHeight;
        }
    }

    @Override
    public void encode(FriendlyByteBuf buffer) {
        buffer.writeFloat(stepHeight);
    }

    public static PacketStepHeightSync decode(FriendlyByteBuf buffer) {
        return new PacketStepHeightSync(buffer.readFloat());
    }
}