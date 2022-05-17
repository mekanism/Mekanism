package mekanism.common.lib.security;

import java.util.List;
import java.util.UUID;
import javax.annotation.Nullable;
import mekanism.api.NBTConstants;
import mekanism.api.security.SecurityMode;
import mekanism.common.lib.collection.HashList;
import mekanism.common.lib.frequency.Frequency;
import mekanism.common.lib.frequency.FrequencyType;
import mekanism.common.network.BasePacketHandler;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.NBTUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;

public class SecurityFrequency extends Frequency {

    public static final String SECURITY = "Security";

    private boolean override = false;

    private final List<UUID> trusted = new HashList<>();
    private List<String> trustedCache = new HashList<>();
    private int trustedCacheHash;

    private SecurityMode securityMode = SecurityMode.PUBLIC;

    /**
     * @param uuid Should only be null if we have incomplete data that we are loading
     */
    public SecurityFrequency(@Nullable UUID uuid) {
        super(FrequencyType.SECURITY, SECURITY, uuid);
    }

    public SecurityFrequency() {
        super(FrequencyType.SECURITY, SECURITY, null);
    }

    @Override
    public UUID getKey() {
        return getOwner();
    }

    @Override
    public void write(CompoundTag nbtTags) {
        super.write(nbtTags);
        nbtTags.putBoolean(NBTConstants.OVERRIDE, override);
        NBTUtils.writeEnum(nbtTags, NBTConstants.SECURITY_MODE, securityMode);
        if (!trusted.isEmpty()) {
            ListTag trustedList = new ListTag();
            for (UUID uuid : trusted) {
                trustedList.add(NbtUtils.createUUID(uuid));
            }
            nbtTags.put(NBTConstants.TRUSTED, trustedList);
        }
    }

    @Override
    protected void read(CompoundTag nbtTags) {
        super.read(nbtTags);
        override = nbtTags.getBoolean(NBTConstants.OVERRIDE);
        NBTUtils.setEnumIfPresent(nbtTags, NBTConstants.SECURITY_MODE, SecurityMode::byIndexStatic, mode -> securityMode = mode);
        if (nbtTags.contains(NBTConstants.TRUSTED, Tag.TAG_LIST)) {
            ListTag trustedList = nbtTags.getList(NBTConstants.TRUSTED, Tag.TAG_INT_ARRAY);
            for (Tag trusted : trustedList) {
                UUID uuid = NbtUtils.loadUUID(trusted);
                addTrusted(uuid, MekanismUtils.getLastKnownUsername(uuid));
            }
        }
    }

    @Override
    public void write(FriendlyByteBuf buffer) {
        super.write(buffer);
        buffer.writeBoolean(override);
        buffer.writeEnum(securityMode);
        buffer.writeCollection(trustedCache, FriendlyByteBuf::writeUtf);
    }

    @Override
    protected void read(FriendlyByteBuf dataStream) {
        super.read(dataStream);
        override = dataStream.readBoolean();
        securityMode = dataStream.readEnum(SecurityMode.class);
        trustedCache = dataStream.readList(BasePacketHandler::readString);
    }

    @Override
    public int getSyncHash() {
        int code = super.getSyncHash();
        code = 31 * code + (override ? 1 : 0);
        code = 31 * code + (securityMode == null ? 0 : securityMode.ordinal());
        code = 31 * code + trustedCacheHash;
        return code;
    }

    public void setOverridden(boolean override) {
        this.override = override;
    }

    public boolean isOverridden() {
        return override;
    }

    public void setSecurityMode(SecurityMode securityMode) {
        this.securityMode = securityMode;
    }

    public SecurityMode getSecurityMode() {
        return securityMode;
    }

    public List<UUID> getTrustedUUIDs() {
        return trusted;
    }

    public List<String> getTrustedUsernameCache() {
        return trustedCache;
    }

    public void addTrusted(UUID uuid, String name) {
        trusted.add(uuid);
        trustedCache.add(name);
        trustedCacheHash = trustedCache.hashCode();
    }

    @Nullable
    public UUID removeTrusted(int index) {
        UUID uuid = null;
        if (index >= 0 && index < trusted.size()) {
            uuid = trusted.remove(index);
        }
        if (index >= 0 && index < trustedCache.size()) {
            trustedCache.remove(index);
            trustedCacheHash = trustedCache.hashCode();
        }
        return uuid;
    }
}