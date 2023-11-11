package mekanism.common.network.to_client.security;

import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.ArrayList;
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
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.ConfigurationPayloadContext;
import org.jetbrains.annotations.NotNull;

public record PacketBatchSecurityUpdate(Map<UUID, SecurityData> securityMap, Map<UUID, String> uuidMap) implements IMekanismPacket<ConfigurationPayloadContext> {

    public static final ResourceLocation ID = Mekanism.rl("batch_security");

    public PacketBatchSecurityUpdate(FriendlyByteBuf buffer) {
        this(PacketUtils.readMultipleMaps(buffer,
              FriendlyByteBuf::readUUID,
              SecurityData::read,
              buf -> buf.readUtf(PacketUtils.LAST_USERNAME_LENGTH)
        ));
    }

    private PacketBatchSecurityUpdate(Pair<Map<UUID, SecurityData>, Map<UUID, String>> maps) {
        this(maps.getFirst(), maps.getSecond());
    }

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
    public ResourceLocation id() {
        return ID;
    }

    @Override
    public void handle(ConfigurationPayloadContext context) {
        MekanismClient.clientSecurityMap.clear();
        MekanismClient.clientSecurityMap.putAll(securityMap);
        MekanismClient.clientUUIDMap.putAll(uuidMap);
    }

    @Override
    public void write(@NotNull FriendlyByteBuf buffer) {
        PacketUtils.writeMultipleMaps(buffer, securityMap, uuidMap,
              FriendlyByteBuf::writeUUID,
              (buf, securityData) -> securityData.write(buf),
              (buf, name) -> buf.writeUtf(name, PacketUtils.LAST_USERNAME_LENGTH)
        );
    }
}