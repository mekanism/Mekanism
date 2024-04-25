package mekanism.common.lib.security;

import java.util.List;
import java.util.UUID;
import mekanism.api.NBTConstants;
import mekanism.api.security.SecurityMode;
import mekanism.common.lib.collection.HashList;
import mekanism.common.lib.frequency.Frequency;
import mekanism.common.lib.frequency.FrequencyType;
import mekanism.common.network.PacketUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.NBTUtils;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import org.jetbrains.annotations.Nullable;

public class SecurityFrequency extends Frequency {

    public static final String SECURITY = "Security";

    private boolean override = false;

    private final HashList<UUID> trusted = new HashList<>();
    private HashList<String> trustedCache = new HashList<>();
    private int trustedCacheHash;

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
    public void write(HolderLookup.Provider provider, CompoundTag nbtTags) {
        super.write(provider, nbtTags);
        nbtTags.putBoolean(NBTConstants.OVERRIDE, override);
        if (!trusted.isEmpty()) {
            ListTag trustedList = new ListTag();
            for (UUID uuid : trusted) {
                trustedList.add(NbtUtils.createUUID(uuid));
            }
            nbtTags.put(NBTConstants.TRUSTED, trustedList);
        }
    }

    @Override
    protected void read(HolderLookup.Provider provider, CompoundTag nbtTags) {
        super.read(provider, nbtTags);
        NBTUtils.setBooleanIfPresent(nbtTags, NBTConstants.OVERRIDE, value -> override = value);
        if (nbtTags.contains(NBTConstants.TRUSTED, Tag.TAG_LIST)) {
            ListTag trustedList = nbtTags.getList(NBTConstants.TRUSTED, Tag.TAG_INT_ARRAY);
            for (Tag trusted : trustedList) {
                UUID uuid = NbtUtils.loadUUID(trusted);
                addTrustedRaw(uuid, MekanismUtils.getLastKnownUsername(uuid));
            }
        }
    }

    @Override
    public void write(RegistryFriendlyByteBuf buffer) {
        super.write(buffer);
        buffer.writeBoolean(override);
        buffer.writeCollection(trustedCache, (buf, name) -> buf.writeUtf(name, PacketUtils.LAST_USERNAME_LENGTH));
    }

    @Override
    protected void read(RegistryFriendlyByteBuf dataStream) {
        super.read(dataStream);
        override = dataStream.readBoolean();
        trustedCache = dataStream.readCollection(HashList::new, buf -> buf.readUtf(PacketUtils.LAST_USERNAME_LENGTH));
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