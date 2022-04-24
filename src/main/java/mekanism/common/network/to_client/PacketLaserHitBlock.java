package mekanism.common.network.to_client;

import mekanism.common.network.IMekanismPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.network.NetworkEvent;

public class PacketLaserHitBlock implements IMekanismPacket {

    private final BlockHitResult result;

    public PacketLaserHitBlock(BlockHitResult result) {
        this.result = result;
    }

    @Override
    public void handle(NetworkEvent.Context context) {
        if (Minecraft.getInstance().level != null) {
            Minecraft.getInstance().particleEngine.addBlockHitEffects(result.getBlockPos(), result);
        }
    }

    @Override
    public void encode(FriendlyByteBuf buffer) {
        buffer.writeBlockHitResult(result);
    }

    public static PacketLaserHitBlock decode(FriendlyByteBuf buffer) {
        return new PacketLaserHitBlock(buffer.readBlockHitResult());
    }
}