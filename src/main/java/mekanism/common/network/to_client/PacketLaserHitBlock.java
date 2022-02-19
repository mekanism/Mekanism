package mekanism.common.network.to_client;

import mekanism.common.network.IMekanismPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraftforge.fml.network.NetworkEvent;

public class PacketLaserHitBlock implements IMekanismPacket {

    private final BlockRayTraceResult result;

    public PacketLaserHitBlock(BlockRayTraceResult result) {
        this.result = result;
    }

    @Override
    public void handle(NetworkEvent.Context context) {
        if (Minecraft.getInstance().level != null) {
            Minecraft.getInstance().particleEngine.addBlockHitEffects(result.getBlockPos(), result);
        }
    }

    @Override
    public void encode(PacketBuffer buffer) {
        buffer.writeBlockHitResult(result);
    }

    public static PacketLaserHitBlock decode(PacketBuffer buffer) {
        return new PacketLaserHitBlock(buffer.readBlockHitResult());
    }
}