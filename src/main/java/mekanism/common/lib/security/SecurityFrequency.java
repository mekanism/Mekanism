package mekanism.common.lib.security;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import mekanism.api.SerializationConstants;
import mekanism.api.security.SecurityMode;
import mekanism.common.lib.collection.HashList;
import mekanism.common.lib.frequency.Frequency;
import mekanism.common.lib.frequency.FrequencyType;
import mekanism.common.network.PacketUtils;
import mekanism.common.util.MekanismUtils;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import org.jetbrains.annotations.Nullable;

public class SecurityFrequency extends Frequency {

    public static final String SECURITY = "Security";

    public static final Codec<SecurityFrequency> CODEC = RecordCodecBuilder.create(instance -> instance.group(
          UUIDUtil.CODEC.optionalFieldOf(SerializationConstants.OWNER_UUID).forGetter(freq -> Optional.ofNullable(freq.getOwner())),
          SecurityMode.CODEC.fieldOf(SerializationConstants.SECURITY_MODE).forGetter(SecurityFrequency::getSecurity),
          Codec.BOOL.fieldOf(SerializationConstants.OVERRIDE).forGetter(SecurityFrequency::isOverridden),
          UUIDUtil.CODEC.listOf().optionalFieldOf(SerializationConstants.TRUSTED).forGetter(freq -> freq.trusted.isEmpty() ? Optional.empty() : Optional.of(freq.trusted.elements()))
    ).apply(instance, (owner, securityMode, override, trustedCache) -> {
        SecurityFrequency frequency = new SecurityFrequency(owner.orElse(null), securityMode);
        frequency.override = override;
        for (UUID trusted : trustedCache.orElse(Collections.emptyList())) {
            frequency.addTrustedRaw(trusted, MekanismUtils.getLastKnownUsername(trusted));
        }
        return frequency;
    }));
    public static final StreamCodec<ByteBuf, SecurityFrequency> STREAM_CODEC = StreamCodec.composite(
          ByteBufCodecs.optional(UUIDUtil.STREAM_CODEC), freq -> Optional.ofNullable(freq.getOwner()),
          ByteBufCodecs.stringUtf8(PacketUtils.LAST_USERNAME_LENGTH), SecurityFrequency::getOwnerName,
          SecurityMode.STREAM_CODEC, SecurityFrequency::getSecurity,
          ByteBufCodecs.BOOL, SecurityFrequency::isOverridden,
          ByteBufCodecs.stringUtf8(PacketUtils.LAST_USERNAME_LENGTH).<HashList<String>>apply(buf -> ByteBufCodecs.collection(HashList::new, buf)), freq -> freq.trustedCache,
          (owner, ownerName, securityMode, override, trustedCache) -> {
              SecurityFrequency frequency = new SecurityFrequency(owner.orElse(null), ownerName, securityMode);
              frequency.override = override;
              frequency.trustedCache = trustedCache;
              return frequency;
          }
    );

    private boolean override = false;

    private final HashList<UUID> trusted = new HashList<>();
    private HashList<String> trustedCache = new HashList<>();
    private int trustedCacheHash;

    /**
     * @param uuid Should only be null if we have incomplete data that we are loading
     */
    public SecurityFrequency(@Nullable UUID uuid, SecurityMode securityMode) {
        super(FrequencyType.SECURITY, SECURITY, uuid, securityMode);
    }

    private SecurityFrequency(@Nullable UUID owner, String ownerName, SecurityMode securityMode) {
        super(FrequencyType.SECURITY, SECURITY, owner, ownerName, securityMode);
    }

    @Override
    public UUID getKey() {
        return getOwner();
    }

    @Override
    public int getSyncHash() {
        int code = super.getSyncHash();
        code = 31 * code + (override ? 1 : 0);
        //We skip including the security in the normal hashcode, but we do need to include it in the sync hashcode
        code = 31 * code + getSecurity().ordinal();
        code = 31 * code + trustedCacheHash;
        return code;
    }

    public void setOverridden(boolean override) {
        if (this.override != override) {
            this.override = override;
            this.dirty = true;
        }
    }

    @Override
    public FrequencyIdentity getIdentity() {
        return new FrequencyIdentity(getKey(), SecurityMode.PUBLIC, getOwner());
    }

    public boolean isOverridden() {
        return override;
    }

    public boolean isTrusted(UUID subject) {
        return trusted.contains(subject);
    }

    public List<String> getTrustedUsernameCache() {
        return trustedCache.elements();
    }

    public void addTrusted(UUID uuid, String name) {
        if (!trusted.contains(uuid)) {
            addTrustedRaw(uuid, name);
            this.dirty = true;
        }
    }

    private void addTrustedRaw(UUID uuid, String name) {
        trusted.add(uuid);
        trustedCache.add(name);
        trustedCacheHash = trustedCache.hashCode();
    }

    @Nullable
    public UUID removeTrusted(int index) {
        UUID uuid = null;
        if (index >= 0 && index < trusted.size()) {
            uuid = trusted.remove(index);
            this.dirty = true;
        }
        if (index >= 0 && index < trustedCache.size()) {
            trustedCache.remove(index);
            trustedCacheHash = trustedCache.hashCode();
        }
        return uuid;
    }
}