package mekanism.common.lib.security;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.annotation.Nullable;
import mekanism.api.NBTConstants;
import mekanism.common.lib.HashList;
import mekanism.common.lib.frequency.Frequency;
import mekanism.common.lib.frequency.FrequencyType;
import mekanism.common.network.BasePacketHandler;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.NBTUtils;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.common.util.Constants.NBT;

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
        super(FrequencyType.SECURITY);
    }

    @Override
    public UUID getKey() {
        return getOwner();
    }

    @Override
    public void write(CompoundNBT nbtTags) {
        super.write(nbtTags);
        nbtTags.putBoolean(NBTConstants.OVERRIDE, override);
        nbtTags.putInt(NBTConstants.SECURITY_MODE, securityMode.ordinal());
        if (!trusted.isEmpty()) {
            ListNBT trustedList = new ListNBT();
            for (UUID uuid : trusted) {
                trustedList.add(NBTUtil.func_240626_a_(uuid));
            }
            nbtTags.put(NBTConstants.TRUSTED, trustedList);
        }
    }

    @Override
    protected void read(CompoundNBT nbtTags) {
        super.read(nbtTags);
        override = nbtTags.getBoolean(NBTConstants.OVERRIDE);
        NBTUtils.setEnumIfPresent(nbtTags, NBTConstants.SECURITY_MODE, SecurityMode::byIndexStatic, mode -> securityMode = mode);
        if (nbtTags.contains(NBTConstants.TRUSTED, NBT.TAG_LIST)) {
            ListNBT trustedList = nbtTags.getList(NBTConstants.TRUSTED, NBT.TAG_INT_ARRAY);
            for (INBT trusted : trustedList) {
                UUID uuid = NBTUtil.readUniqueId(trusted);
                addTrusted(uuid, MekanismUtils.getLastKnownUsername(uuid));
            }
        }
    }

    @Override
    public void write(PacketBuffer buffer) {
        super.write(buffer);
        buffer.writeBoolean(override);
        buffer.writeEnumValue(securityMode);
        buffer.writeVarInt(trustedCache.size());
        trustedCache.forEach(buffer::writeString);
    }

    @Override
    protected void read(PacketBuffer dataStream) {
        super.read(dataStream);
        override = dataStream.readBoolean();
        securityMode = dataStream.readEnumValue(SecurityMode.class);
        trustedCache = new ArrayList<>();
        int count = dataStream.readVarInt();
        for (int i = 0; i < count; i++) {
            trustedCache.add(BasePacketHandler.readString(dataStream));
        }
    }

    @Override
    public int getSyncHash() {
        int code = super.getSyncHash();
        code = 31 * code + (override ? 1 : 0);
        code = 31 * code + (securityMode != null ? securityMode.ordinal() : 0);
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

    public void removeTrusted(int index) {
        if (index >= 0 && index < trusted.size()) {
            trusted.remove(index);
        }
        if (index >= 0 && index < trustedCache.size()) {
            trustedCache.remove(index);
        }
        trustedCacheHash = trustedCache.hashCode();
    }
}