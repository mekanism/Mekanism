package mekanism.common.network.to_client.security;

import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import mekanism.api.security.SecurityMode;
import mekanism.client.MekanismClient;
import mekanism.common.Mekanism;
import mekanism.common.lib.frequency.FrequencyType;
import mekanism.common.lib.security.SecurityData;
import mekanism.common.lib.security.SecurityFrequency;
import mekanism.common.network.IMekanismPacket;
import mekanism.common.network.PacketUtils;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

public record PacketBatchSecurityUpdate(Map<UUID, SecurityData> securityMap, Map<UUID, String> uuidMap) implements IMekanismPacket {

    public static final CustomPacketPayload.Type<PacketBatchSecurityUpdate> TYPE = new CustomPacketPayload.Type<>(Mekanism.rl("batch_security"));
    public static final StreamCodec<ByteBuf, PacketBatchSecurityUpdate> STREAM_CODEC = StreamCodec.composite(
          ByteBufCodecs.map(HashMap::new, UUIDUtil.STREAM_CODEC, SecurityData.STREAM_CODEC), PacketBatchSecurityUpdate::securityMap,
          ByteBufCodecs.map(HashMap::new, UUIDUtil.STREAM_CODEC, ByteBufCodecs.stringUtf8(PacketUtils.LAST_USERNAME_LENGTH)), PacketBatchSecurityUpdate::uuidMap,
          PacketBatchSecurityUpdate::new
    );

    public PacketBatchSecurityUpdate() {
        this(new Object2ObjectOpenHashMap<>(), new Object2ObjectOpenHashMap<>());
        List<SecurityFrequency> frequencies = new ArrayList<>(FrequencyType.SECURITY.getManager(null, SecurityMode.PUBLIC).getFrequencies());
        for (SecurityFrequency frequency : frequencies) {
            UUID owner = frequency.getOwner();
            //In theory no owner should be null but just in case handle it anyway
            if (owner != null) {
                securityMap.put(owner, new SecurityData(frequency));
                uuidMap.put(owner, frequency.getOwnerName());
            }
        }
    }

    @NotNull
    @Override
    public CustomPacketPayload.Type<PacketBatchSecurityUpdate> type() {
        return TYPE;
    }

    @Override
    public void handle(IPayloadContext context) {
        MekanismClient.clientSecurityMap.clear();
        MekanismClient.clientSecurityMap.putAll(securityMap);
        MekanismClient.clientUUIDMap.putAll(uuidMap);
    }
}