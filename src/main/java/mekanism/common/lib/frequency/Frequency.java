package mekanism.common.lib.frequency;

import com.mojang.datafixers.Products.P3;
import com.mojang.serialization.codecs.RecordCodecBuilder.Instance;
import com.mojang.serialization.codecs.RecordCodecBuilder.Mu;
import io.netty.buffer.ByteBuf;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import mekanism.api.IFrequency;
import mekanism.api.SerializationConstants;
import mekanism.api.security.SecurityMode;
import mekanism.common.network.PacketUtils;
import mekanism.common.util.MekanismUtils;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class Frequency implements IFrequency {

    protected static <FREQ extends Frequency> P3<Mu<FREQ>, String, Optional<UUID>, SecurityMode> baseCodec(Instance<FREQ> instance) {
        return instance.group(
              ExtraCodecs.NON_EMPTY_STRING.fieldOf(SerializationConstants.NAME).forGetter(Frequency::getName),
              UUIDUtil.CODEC.optionalFieldOf(SerializationConstants.OWNER_UUID).forGetter(freq -> Optional.ofNullable(freq.getOwner())),
              SecurityMode.CODEC.fieldOf(SerializationConstants.SECURITY_MODE).forGetter(Frequency::getSecurity)
        );
    }

    protected static <BUF extends ByteBuf, FREQ extends Frequency> StreamCodec<BUF, FREQ> baseStreamCodec(FrequencyConstructor<FREQ> constructor) {
        return StreamCodec.composite(
              ByteBufCodecs.STRING_UTF8, Frequency::getName,
              ByteBufCodecs.optional(UUIDUtil.STREAM_CODEC), freq -> Optional.ofNullable(freq.getOwner()),
              ByteBufCodecs.stringUtf8(PacketUtils.LAST_USERNAME_LENGTH), Frequency::getOwnerName,
              SecurityMode.STREAM_CODEC, Frequency::getSecurity,
              (name, owner, ownerName, security) -> constructor.create(name, owner.orElse(null), ownerName, security)
        );
    }

    protected boolean dirty;
    private boolean removed;
    private final String name;

    @Nullable
    private final UUID ownerUUID;
    private final String ownerName;

    private boolean valid = true;
    private SecurityMode securityMode;

    private final FrequencyType<?> frequencyType;

    /**
     * Owner username is looked up so that we can sync it (and more importantly have it
     * set in single player when network connections don't serialize and deserialize)
     *
     * @param owner Should only be null if we have incomplete data that we are loading.
     */
    protected Frequency(FrequencyType<?> frequencyType, String name, @Nullable UUID owner, SecurityMode securityMode) {
        this(frequencyType, name, owner, MekanismUtils.getLastKnownUsername(owner), securityMode);
    }

    protected Frequency(FrequencyType<?> frequencyType, String name, @Nullable UUID owner, String ownerName, SecurityMode securityMode) {
        this.frequencyType = frequencyType;
        this.name = name;
        this.ownerUUID = owner;
        this.ownerName = ownerName;
        this.securityMode = securityMode;
    }

    /**
     * @return {@code true} if persistent data was changed and the frequency needs to be saved.
     */
    public boolean tick(boolean tickingNormally) {
        return dirty;
    }

    public void onRemove() {
        removed = true;
    }

    public boolean isRemoved() {
        return removed;
    }

    /**
     * @return {@code true} if persistent data was changed by deactivating the block and the frequency needs to be saved.
     */
    public boolean onDeactivate(BlockEntity tile) {
        return false;
    }

    /**
     * @return {@code true} if persistent data was changed by updating the block and the frequency needs to be saved.
     */
    public boolean update(BlockEntity tile) {
        return false;
    }

    public FrequencyType<?> getType() {
        return frequencyType;
    }

    public Object getKey() {
        return name;
    }

    @Override
    public final SecurityMode getSecurity() {
        return securityMode;
    }

    public void setSecurityMode(SecurityMode securityMode) {
        if (this.securityMode != securityMode) {
            this.securityMode = securityMode;
            dirty = true;
        }
    }

    @Override
    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    @Override
    public String getName() {
        return name;
    }

    @Nullable
    @Override
    public UUID getOwner() {
        return ownerUUID;
    }

    public boolean ownerMatches(UUID toCheck) {
        return Objects.equals(ownerUUID, toCheck);
    }

    @NotNull
    public String getOwnerName() {
        return ownerName;
    }

    /**
     * This is the hashCode that is used for determining if a frequency is dirty. Override this if your frequency type has more things that may mean it needs to be
     * re-synced.
     */
    public int getSyncHash() {
        return hashCode();
    }

    @Override
    public int hashCode() {
        int code = 1;
        code = 31 * code + name.hashCode();
        if (ownerUUID != null) {
            code = 31 * code + ownerUUID.hashCode();
        }
        if (frequencyType != FrequencyType.SECURITY) {
            code = 31 * code + (securityMode.ordinal());
        }
        return code;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof Frequency other) {
            if (frequencyType == FrequencyType.SECURITY || securityMode == other.securityMode) {
                return ownerUUID != null && name.equals(other.name) && ownerUUID.equals(other.ownerUUID);
            }
        }
        return false;
    }

    public FrequencyIdentity getIdentity() {
        return new FrequencyIdentity(getKey(), securityMode, ownerUUID);
    }

    public record FrequencyIdentity(Object key, SecurityMode securityMode, @Nullable UUID ownerUUID) {
    }

    @FunctionalInterface
    protected interface FrequencyConstructor<FREQ extends Frequency> {

        FREQ create(String name, @Nullable UUID owner, String ownerName, SecurityMode securityMode);
    }
}
