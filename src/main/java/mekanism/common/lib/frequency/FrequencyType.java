package mekanism.common.lib.frequency;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.DecoderException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import mekanism.api.SerializationConstants;
import mekanism.api.security.SecurityMode;
import mekanism.common.Mekanism;
import mekanism.common.content.entangloporter.InventoryFrequency;
import mekanism.common.content.qio.QIOFrequency;
import mekanism.common.content.teleporter.TeleporterFrequency;
import mekanism.common.lib.frequency.Frequency.FrequencyIdentity;
import mekanism.common.lib.security.SecurityFrequency;
import mekanism.common.lib.security.SecurityUtils;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
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
          (key, uuid, securityMode) -> new TeleporterFrequency((String) key, uuid, securityMode),
          TeleporterFrequency.CODEC,
          TeleporterFrequency.STREAM_CODEC,
          FrequencyManagerWrapper.Type.PUBLIC_PRIVATE_TRUSTED,
          IdentitySerializer.NAME);
    public static final FrequencyType<InventoryFrequency> INVENTORY = register("Inventory",
          (key, uuid, securityMode) -> new InventoryFrequency((String) key, uuid, securityMode),
          InventoryFrequency.CODEC,
          InventoryFrequency.STREAM_CODEC,
          FrequencyManagerWrapper.Type.PUBLIC_PRIVATE_TRUSTED,
          IdentitySerializer.NAME);
    public static final FrequencyType<SecurityFrequency> SECURITY = register("Security",
          (key, uuid, securityMode) -> new SecurityFrequency(uuid, securityMode),
          SecurityFrequency.CODEC,
          SecurityFrequency.STREAM_CODEC,
          FrequencyManagerWrapper.Type.PUBLIC_ONLY,
          IdentitySerializer.UUID);
    public static final FrequencyType<QIOFrequency> QIO = register("QIO",
          (key, uuid, securityMode) -> new QIOFrequency((String) key, uuid, securityMode),
          QIOFrequency.CODEC,
          QIOFrequency.STREAM_CODEC,
          FrequencyManagerWrapper.Type.PUBLIC_PRIVATE_TRUSTED,
          IdentitySerializer.NAME);

    public static void init() {
    }

    private final String name;
    private final FrequencyConstructor<FREQ> creationFunction;
    private final Codec<FREQ> codec;
    private final StreamCodec<? super RegistryFriendlyByteBuf, FREQ> streamCodec;
    private final IdentitySerializer identitySerializer;
    private final FrequencyManagerWrapper<FREQ> managerWrapper;

    private static <FREQ extends Frequency> FrequencyType<FREQ> register(String name, FrequencyConstructor<FREQ> creationFunction, Codec<FREQ> codec,
          StreamCodec<? super RegistryFriendlyByteBuf, FREQ> streamCodec, FrequencyManagerWrapper.Type managerType, IdentitySerializer identitySerializer) {
        FrequencyType<FREQ> type = new FrequencyType<>(name, creationFunction, codec, streamCodec, managerType, identitySerializer);
        registryMap.put(name, type);
        maxNameLength = Math.max(maxNameLength, name.length());
        return type;
    }

    private FrequencyType(String name, FrequencyConstructor<FREQ> creationFunction,  Codec<FREQ> codec, StreamCodec<? super RegistryFriendlyByteBuf, FREQ> streamCodec,
          FrequencyManagerWrapper.Type managerType, IdentitySerializer identitySerializer) {
        this.name = name;
        this.creationFunction = creationFunction;
        this.codec = codec;
        this.streamCodec = streamCodec;
        this.managerWrapper = FrequencyManagerWrapper.create(this, managerType);
        this.identitySerializer = identitySerializer;
    }

    public String getName() {
        return name;
    }

    public StreamCodec<? super RegistryFriendlyByteBuf, FREQ> streamCodec() {
        return streamCodec;
    }

    public Codec<FREQ> codec() {
        return codec;
    }

    @Nullable
    public FREQ create(HolderLookup.Provider provider, CompoundTag identityTag) {
        DataResult<FrequencyIdentity> parsedIdentity = getIdentitySerializer().codec().parse(provider.createSerializationContext(NbtOps.INSTANCE), identityTag);
        if (parsedIdentity.isSuccess()) {
            FrequencyIdentity identity = parsedIdentity.getOrThrow();
            FREQ frequency = create(identity.key(), identity.ownerUUID(), identity.securityMode());
            frequency.setValid(false);
            return frequency;
        }
        parsedIdentity.ifError(error -> Mekanism.logger.warn("Failed to deserialize frequency identity: {}", error.message()));
        return null;
    }

    public FREQ create(Object key, UUID ownerUUID, SecurityMode securityMode) {
        return creationFunction.create(key, ownerUUID, securityMode);
    }

    public FREQ create(RegistryFriendlyByteBuf buffer) {
        return streamCodec.decode(buffer);
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
        FrequencyManagerWrapper<FREQ> manager = getManagerWrapper();
        if (freq.getType() == SECURITY) {
            //Frequency#getSecurity means something slightly different for security frequencies. They are always public
            return manager.getPublicManager();
        }
        return switch (freq.getSecurity()) {
            case PUBLIC -> manager.getPublicManager();
            case PRIVATE -> manager.getPrivateManager(freq.getOwner());
            case TRUSTED -> manager.getTrustedManager(freq.getOwner());
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
        return (FrequencyType<FREQ>) registryMap.get(tag.getString(SerializationConstants.TYPE));
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

    @FunctionalInterface
    private interface FrequencyConstructor<FREQ extends Frequency> {

        FREQ create(Object key, UUID owner, SecurityMode securityMode);
    }
}