package mekanism.common.lib.frequency;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.DecoderException;
import java.util.Objects;
import java.util.UUID;
import mekanism.api.security.SecurityMode;
import mekanism.common.lib.frequency.Frequency.FrequencyIdentity;
import mekanism.common.lib.frequency.FrequencyController.Type;
import mekanism.common.lib.frequency.FrequencyTypes.FrequencyConstructor;
import mekanism.common.lib.security.SecurityUtils;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.neoforged.neoforge.network.codec.NeoForgeStreamCodecs;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

public class FrequencyType<FREQ extends Frequency> {

    //TODO - 26.1 - investigate no usages
    public static final Codec<FrequencyType<?>> CODEC = Codec.stringResolver(FrequencyType::getName, FrequencyTypes::byName);
    public static final StreamCodec<ByteBuf, FrequencyType<?>> STREAM_CODEC = NeoForgeStreamCodecs.lazy(() -> ByteBufCodecs.stringUtf8(255).map(
          name -> {
              FrequencyType<?> type = FrequencyTypes.byName(name);
              if (type == null) {
                  throw new DecoderException("Unable to find frequency type for name: " + name);
              }
              return type;
          }, FrequencyType::getName
    ));

    private final String name;
    private final FrequencyConstructor<FREQ> creationFunction;
    private final Codec<FREQ> codec;
    private final StreamCodec<? super RegistryFriendlyByteBuf, FREQ> streamCodec;
    private final IdentitySerializer identitySerializer;
    private final FrequencyController.Type controllerType;
    private final boolean needsTick;

    public FrequencyType(String name, FrequencyConstructor<FREQ> creationFunction, Codec<FREQ> codec, StreamCodec<? super RegistryFriendlyByteBuf, FREQ> streamCodec,
          FrequencyController.Type controllerType, IdentitySerializer identitySerializer, boolean needsTick) {
        this.name = name;
        this.creationFunction = creationFunction;
        this.codec = codec;
        this.streamCodec = streamCodec;
        this.controllerType = controllerType;
        this.identitySerializer = identitySerializer;
        this.needsTick = needsTick;
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

    public Type getControllerType() {
        return controllerType;
    }

    @Nullable
    public FREQ create(FrequencyIdentity identity) {
        FREQ frequency = create(identity.key(), identity.ownerUUID(), identity.securityMode());
        frequency.setValid(false);
        return frequency;
    }

    public FREQ create(Object key, UUID ownerUUID, SecurityMode securityMode) {
        return creationFunction.create(key, ownerUUID, securityMode);
    }

    public FREQ create(RegistryFriendlyByteBuf buffer) {
        return streamCodec.decode(buffer);
    }

    public FrequencyController<FREQ> getController() {
        return FrequencyControllerManager.getController(this);
    }

    public FrequencyLookup<FREQ> getLookup(@Nullable UUID owner, SecurityMode securityMode) {
        return switch (securityMode) {
            case PUBLIC -> getController().getPublicLookup();
            case PRIVATE -> getController().getPrivateLookup(owner);
            case TRUSTED -> getController().getTrustedLookup(owner);
        };
    }

    @Nullable
    @Contract("null -> null")
    public FrequencyLookup<FREQ> getFrequencyLookup(@Nullable FREQ freq) {
        if (freq == null) {
            return null;
        }
        FrequencyController<FREQ> controller = getController();
        if (freq.getType() == FrequencyTypes.SECURITY) {
            //Frequency#getSecurity means something slightly different for security frequencies. They are always public
            return controller.getPublicLookup();
        }
        return switch (freq.getSecurity()) {
            case PUBLIC -> controller.getPublicLookup();
            case PRIVATE -> controller.getPrivateLookup(freq.getOwner());
            case TRUSTED -> controller.getTrustedLookup(freq.getOwner());
        };
    }

    public FrequencyLookup<FREQ> getLookup(FrequencyIdentity identity, UUID owner) {
        return switch (identity.securityMode()) {
            case PUBLIC -> getController().getPublicLookup();
            case PRIVATE -> getController().getPrivateLookup(owner);
            case TRUSTED -> getController().getTrustedLookup(owner);
        };
    }

    @Nullable
    public FREQ getFrequency(FrequencyIdentity identity, UUID owner) {
        FrequencyLookup<FREQ> lookup;
        if (!Objects.equals(identity.ownerUUID(), owner) && SecurityUtils.get().isTrusted(identity.securityMode(), identity.ownerUUID(), owner)) {
            lookup = getLookup(identity, identity.ownerUUID());
        } else {
            lookup = getLookup(identity, owner);
        }
        return lookup.getFrequency(identity.key());
    }

    public IdentitySerializer getIdentitySerializer() {
        return identitySerializer;
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

    public boolean needsTick() {
        return needsTick;
    }
}