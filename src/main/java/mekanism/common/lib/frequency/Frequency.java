package mekanism.common.lib.frequency;

import java.util.Objects;
import java.util.UUID;
import mekanism.api.IFrequency;
import mekanism.api.NBTConstants;
import mekanism.api.security.SecurityMode;
import mekanism.common.network.PacketUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.NBTUtils;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class Frequency implements IFrequency {

    protected boolean dirty;
    private boolean removed;
    private String name;

    @Nullable
    private UUID ownerUUID;
    private String ownerName;

    private boolean valid = true;
    private SecurityMode securityMode = SecurityMode.PUBLIC;

    private final FrequencyType<?> frequencyType;

    /**
     * @param uuid Should only be null if we have incomplete data that we are loading
     */
    public Frequency(FrequencyType<?> frequencyType, String name, @Nullable UUID uuid) {
        this(frequencyType);
        this.name = name;
        setOwnerUUID(uuid);
    }

    public Frequency(FrequencyType<?> frequencyType) {
        this.frequencyType = frequencyType;
    }

    /**
     * @return {@code true} if persistent data was changed and the frequency needs to be saved.
     */
    public boolean tick() {
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

    public void writeComponentData(CompoundTag nbtTags) {
        nbtTags.putString(NBTConstants.NAME, name);
        if (ownerUUID != null) {
            nbtTags.putUUID(NBTConstants.OWNER_UUID, ownerUUID);
        }
        NBTUtils.writeEnum(nbtTags, NBTConstants.SECURITY_MODE, securityMode);
    }

    public void write(HolderLookup.Provider provider, CompoundTag nbtTags) {
        writeComponentData(nbtTags);
    }

    protected void read(HolderLookup.Provider provider, CompoundTag nbtTags) {
        name = nbtTags.getString(NBTConstants.NAME);
        NBTUtils.setUUIDIfPresent(nbtTags, NBTConstants.OWNER_UUID, this::setOwnerUUID);
        NBTUtils.setEnumIfPresent(nbtTags, NBTConstants.SECURITY_MODE, SecurityMode.BY_ID, mode -> securityMode = mode);
    }

    private void setOwnerUUID(@Nullable UUID uuid) {
        ownerUUID = uuid;
        //Look up the username of the owner so that we can sync it (and more importantly have it set in single player when network connections don't serialize and deserialize)
        ownerName = MekanismUtils.getLastKnownUsername(ownerUUID);
    }

    public void write(RegistryFriendlyByteBuf buffer) {
        buffer.writeUtf(name);
        buffer.writeNullable(ownerUUID, (buf, uuid) -> buf.writeUUID(uuid));
        buffer.writeUtf(ownerName, PacketUtils.LAST_USERNAME_LENGTH);
        buffer.writeEnum(securityMode);
    }

    protected void read(RegistryFriendlyByteBuf dataStream) {
        name = dataStream.readUtf();
        ownerUUID = dataStream.readNullable(buf -> buf.readUUID());
        ownerName = dataStream.readUtf(PacketUtils.LAST_USERNAME_LENGTH);
        securityMode = dataStream.readEnum(SecurityMode.class);
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
}
