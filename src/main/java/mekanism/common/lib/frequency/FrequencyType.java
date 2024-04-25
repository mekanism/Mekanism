package mekanism.common.lib.frequency;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.DecoderException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.BiFunction;
import java.util.function.Supplier;
import mekanism.api.NBTConstants;
import mekanism.api.security.SecurityMode;
import mekanism.common.content.entangloporter.InventoryFrequency;
import mekanism.common.content.qio.QIOFrequency;
import mekanism.common.content.teleporter.TeleporterFrequency;
import mekanism.common.lib.frequency.Frequency.FrequencyIdentity;
import mekanism.common.lib.security.SecurityFrequency;
import mekanism.common.lib.security.SecurityUtils;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.neoforged.neoforge.network.codec.NeoForgeStreamCodecs;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

public class FrequencyType<FREQ extends Frequency> {

    private static final Map<String, FrequencyType<?>> registryMap = new HashMap<>();
    private static int maxNameLength = 0;

    public static final Codec<FrequencyType<?>> CODEC = Codec.stringResolver(FrequencyType::getName, registryMap::get);
    //Note: This is lazy so that we ensure we don't call it until after maxNameLength has been set
    public static final StreamCodec<ByteBuf, FrequencyType<?>> STREAM_CODEC = NeoForgeStreamCodecs.lazy(() -> ByteBufCodecs.stringUtf8(maxNameLength).map(
          name -> {
              FrequencyType<?> type = registryMap.get(name);
              if (type == null) {
                  throw new DecoderException("Unable to find frequency type for name: " + name);
              }
              return type;
          }, FrequencyType::getName
    ));

    public static final FrequencyType<TeleporterFrequency> TELEPORTER = register("Teleporter",
          (key, uuid) -> new TeleporterFrequency((String) key, uuid),
          TeleporterFrequency::new,
          FrequencyManagerWrapper.Type.PUBLIC_PRIVATE_TRUSTED,
          IdentitySerializer.NAME);
    public static final FrequencyType<InventoryFrequency> INVENTORY = register("Inventory",
          (key, uuid) -> new InventoryFrequency((String) key, uuid),
          InventoryFrequency::new,
          FrequencyManagerWrapper.Type.PUBLIC_PRIVATE_TRUSTED,
          IdentitySerializer.NAME);
    public static final FrequencyType<SecurityFrequency> SECURITY = register("Security",
          (key, uuid) -> new SecurityFrequency(uuid),
          SecurityFrequency::new,
          FrequencyManagerWrapper.Type.PUBLIC_ONLY,
          IdentitySerializer.UUID);
    public static final FrequencyType<QIOFrequency> QIO = register("QIO",
          (key, uuid) -> new QIOFrequency((String) key, uuid),
          QIOFrequency::new,
          FrequencyManagerWrapper.Type.PUBLIC_PRIVATE_TRUSTED,
          IdentitySerializer.NAME);

    public static void init() {
    }

    private final String name;
    private final BiFunction<Object, UUID, FREQ> creationFunction;
    private final Supplier<FREQ> baseCreationFunction;
    private final IdentitySerializer identitySerializer;
    private final FrequencyManagerWrapper<FREQ> managerWrapper;

    private static <FREQ extends Frequency> FrequencyType<FREQ> register(String name, BiFunction<Object, UUID, FREQ> creationFunction,
          Supplier<FREQ> baseCreationFunction, FrequencyManagerWrapper.Type managerType, IdentitySerializer identitySerializer) {
        FrequencyType<FREQ> type = new FrequencyType<>(name, creationFunction, baseCreationFunction, managerType, identitySerializer);
        registryMap.put(name, type);
        maxNameLength = Math.max(maxNameLength, name.length());
        return type;
    }

    private FrequencyType(String name, BiFunction<Object, UUID, FREQ> creationFunction, Supplier<FREQ> baseCreationFunction,
          FrequencyManagerWrapper.Type managerType, IdentitySerializer identitySerializer) {
        this.name = name;
        this.creationFunction = creationFunction;
        this.baseCreationFunction = baseCreationFunction;
        this.managerWrapper = FrequencyManagerWrapper.create(this, managerType);
        this.identitySerializer = identitySerializer;
    }

    public String getName() {
        return name;
    }

    public FREQ create(HolderLookup.Provider provider, CompoundTag tag) {
        FREQ freq = baseCreationFunction.get();
        freq.read(provider, tag);
        return freq;
    }

    public FREQ create(Object key, UUID ownerUUID) {
        return creationFunction.apply(key, ownerUUID);
    }

    public FREQ create(RegistryFriendlyByteBuf packet) {
        FREQ freq = baseCreationFunction.get();
        freq.read(packet);
        return freq;
    }

    public FrequencyManagerWrapper<FREQ> getManagerWrapper() {
        return managerWrapper;
    }

    public FrequencyManager<FREQ> getManager(@Nullable UUID owner, SecurityMode securityMode) {
        return switch (securityMode) {
            case PUBLIC -> getManagerWrapper().getPublicManager();
            case PRIVATE -> getManagerWrapper().getPrivateManager(owner);
            case TRUSTED -> getManagerWrapper().getTrustedManager(owner);
        };
    }

    @Nullable
    @Contract("null -> null")
    public FrequencyManager<FREQ> getFrequencyManager(@Nullable FREQ freq) {
        if (freq == null) {
            return null;
        }
        return switch (freq.getSecurity()) {
            case PUBLIC -> getManagerWrapper().getPublicManager();
            case PRIVATE -> getManagerWrapper().getPrivateManager(freq.getOwner());
            case TRUSTED -> getManagerWrapper().getTrustedManager(freq.getOwner());
        };
    }

    public FrequencyManager<FREQ> getManager(FrequencyIdentity identity, UUID owner) {
        return switch (identity.securityMode()) {
            case PUBLIC -> getManagerWrapper().getPublicManager();
            case PRIVATE -> getManagerWrapper().getPrivateManager(owner);
            case TRUSTED -> getManagerWrapper().getTrustedManager(owner);
        };
    }

    @Nullable
    public FREQ getFrequency(FrequencyIdentity identity, UUID owner) {
        FrequencyManager<FREQ> manager;
        if (!Objects.equals(identity.ownerUUID(), owner) && SecurityUtils.get().isTrusted(identity.securityMode(), identity.ownerUUID(), owner)) {
            manager = getManager(identity, identity.ownerUUID());
        } else {
            manager = getManager(identity, owner);
        }
        return manager.getFrequency(identity.key());
    }

    public IdentitySerializer getIdentitySerializer() {
        return identitySerializer;
    }

    public static <FREQ extends Frequency> FrequencyType<FREQ> load(CompoundTag tag) {
        return (FrequencyType<FREQ>) registryMap.get(tag.getString(NBTConstants.TYPE));
    }

    public static void clear() {
        for (FrequencyType<?> type : registryMap.values()) {
            type.managerWrapper.clear();
        }
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj) || (obj instanceof FrequencyType<?> other && Objects.equals(name, other.name));
    }
}