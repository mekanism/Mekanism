package mekanism.common.lib.frequency;

import com.mojang.serialization.Codec;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import mekanism.api.SerializationConstants;
import mekanism.api.security.SecurityMode;
import mekanism.common.content.entangloporter.InventoryFrequency;
import mekanism.common.content.qio.QIOFrequency;
import mekanism.common.content.teleporter.TeleporterFrequency;
import mekanism.common.lib.security.SecurityFrequency;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import org.jetbrains.annotations.Nullable;

public class FrequencyTypes {
    private static final Map<String, FrequencyType<?>> registryMap = new HashMap<>();
    private static final List<FrequencyLookup<?>> tickableLookups = new ArrayList<>();

    public static final FrequencyType<TeleporterFrequency> TELEPORTER = register("Teleporter",
          (key, uuid, securityMode) -> new TeleporterFrequency((String) key, uuid, securityMode),
          TeleporterFrequency.CODEC,
          TeleporterFrequency.STREAM_CODEC,
          FrequencyController.Type.PUBLIC_PRIVATE_TRUSTED,
          IdentitySerializer.NAME,
          false);
    public static final FrequencyType<InventoryFrequency> INVENTORY = register("Inventory",
          (key, uuid, securityMode) -> new InventoryFrequency((String) key, uuid, securityMode),
          InventoryFrequency.CODEC,
          InventoryFrequency.STREAM_CODEC,
          FrequencyController.Type.PUBLIC_PRIVATE_TRUSTED,
          IdentitySerializer.NAME,
          false);
    public static final FrequencyType<SecurityFrequency> SECURITY = register("Security",
          (key, uuid, securityMode) -> new SecurityFrequency(uuid, securityMode),
          SecurityFrequency.CODEC,
          SecurityFrequency.STREAM_CODEC,
          FrequencyController.Type.PUBLIC_ONLY,
          IdentitySerializer.UUID,
          false);
    public static final FrequencyType<QIOFrequency> QIO = register("QIO",
          (key, uuid, securityMode) -> new QIOFrequency((String) key, uuid, securityMode),
          QIOFrequency.CODEC,
          QIOFrequency.STREAM_CODEC,
          FrequencyController.Type.PUBLIC_PRIVATE_TRUSTED,
          IdentitySerializer.NAME,
          true);

    public static void init() {
    }

    private static <FREQ extends Frequency> FrequencyType<FREQ> register(String name, FrequencyConstructor<FREQ> creationFunction, Codec<FREQ> codec,
          StreamCodec<? super RegistryFriendlyByteBuf, FREQ> streamCodec, FrequencyController.Type managerType, IdentitySerializer identitySerializer, boolean needsTick) {
        FrequencyType<FREQ> type = new FrequencyType<>(name, creationFunction, codec, streamCodec, managerType, identitySerializer, needsTick);
        registryMap.put(name, type);
        return type;
    }

    public static <FREQ extends Frequency> FrequencyType<FREQ> load(CompoundTag tag) {
        return (FrequencyType<FREQ>) registryMap.get(tag.getString(SerializationConstants.TYPE));
    }

    @Nullable
    public static FrequencyType<?> byName(String name) {
        return registryMap.get(name);
    }

    public static Collection<FrequencyType<?>> allRegistered() {
        return Collections.unmodifiableCollection(registryMap.values());
    }

    public static void registerTickable(FrequencyLookup<?> lookup) {
        if (lookup.getFrequencyType().needsTick()) {
            tickableLookups.add(lookup);
        }
    }

    public static void tick(boolean tickingNormally) {
        for (FrequencyLookup<?> lookup : tickableLookups) {
            lookup.tickSelf(tickingNormally);
        }
    }

    public static void reset() {
        tickableLookups.clear();
        for (FrequencyType<?> type : FrequencyTypes.allRegistered()) {
            type.getController().clear();
        }
    }

    @FunctionalInterface
    public interface FrequencyConstructor<FREQ extends Frequency> {

        FREQ create(Object key, UUID owner, SecurityMode securityMode);
    }
}
