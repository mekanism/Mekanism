package mekanism.common.security;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import mekanism.api.NBTConstants;
import mekanism.common.HashList;
import mekanism.common.frequency.Frequency;
import mekanism.common.security.ISecurityTile.SecurityMode;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.NBTUtils;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraftforge.common.util.Constants.NBT;

public class SecurityFrequency extends Frequency {

    public static final String SECURITY = "Security";

    public boolean override;

    public HashList<UUID> trusted;
    public List<String> trustedCache;

    public SecurityMode securityMode;

    public SecurityFrequency(UUID uuid) {
        super(SECURITY, uuid);
        trusted = new HashList<>();
        trustedCache = new ArrayList<>();
        securityMode = SecurityMode.PUBLIC;
    }

    public SecurityFrequency(CompoundNBT nbtTags, boolean fromUpdate) {
        super(nbtTags, fromUpdate);
    }

    @Override
    protected void readFromUpdateTag(CompoundNBT updateTag) {
        super.readFromUpdateTag(updateTag);
        securityMode = SecurityMode.PUBLIC;
        trustedCache = new ArrayList<>();
        trusted = new HashList<>();
    }

    @Override
    public void write(CompoundNBT nbtTags) {
        super.write(nbtTags);
        nbtTags.putBoolean(NBTConstants.OVERRIDE, override);
        nbtTags.putInt(NBTConstants.SECURITY_MODE, securityMode.ordinal());
        if (!trusted.isEmpty()) {
            ListNBT trustedList = new ListNBT();
            for (UUID uuid : trusted) {
                trustedList.add(NBTUtil.writeUniqueId(uuid));
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
            ListNBT trustedList = nbtTags.getList(NBTConstants.TRUSTED, NBT.TAG_COMPOUND);
            for (int i = 0; i < trustedList.size(); i++) {
                UUID uuid = NBTUtil.readUniqueId(trustedList.getCompound(i));
                addTrusted(uuid, MekanismUtils.getLastKnownUsername(uuid));
            }
        }
    }

    public void addTrusted(UUID uuid, String name) {
        trusted.add(uuid);
        trustedCache.add(name);
    }

    public void removeTrusted(int index) {
        if (index >= 0 && index < trusted.size()) {
            trusted.remove(index);
        }
        if (index >= 0 && index < trustedCache.size()) {
            trustedCache.remove(index);
        }
    }
}