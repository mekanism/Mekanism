package mekanism.common.tile.component;

import java.util.UUID;
import mekanism.api.NBTConstants;
import mekanism.common.config.MekanismConfig;
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.inventory.container.sync.SyncableEnum;
import mekanism.common.lib.frequency.Frequency.FrequencyIdentity;
import mekanism.common.lib.frequency.FrequencyType;
import mekanism.common.lib.security.SecurityFrequency;
import mekanism.common.lib.security.SecurityMode;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.NBTUtils;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.common.util.Constants.NBT;

public class TileComponentSecurity implements ITileComponent {

    /**
     * TileEntity implementing this component.
     */
    public final TileEntityMekanism tile;

    private UUID ownerUUID;
    private String ownerName;

    private SecurityMode securityMode = SecurityMode.PUBLIC;

    public TileComponentSecurity(TileEntityMekanism tile) {
        this.tile = tile;
        tile.addComponent(this);
        tile.getFrequencyComponent().track(FrequencyType.SECURITY, true, false, true);
    }

    public SecurityFrequency getFrequency() {
        return tile.getFrequency(FrequencyType.SECURITY);
    }

    public UUID getOwnerUUID() {
        return ownerUUID;
    }

    public void setOwnerUUID(UUID uuid) {
        tile.getFrequencyComponent().setFrequency(FrequencyType.SECURITY, null);
        ownerUUID = uuid;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public SecurityMode getMode() {
        if (MekanismConfig.general.allowProtection.get()) {
            return securityMode;
        }
        return SecurityMode.PUBLIC;
    }

    public void setMode(SecurityMode mode) {
        if (securityMode != mode) {
            SecurityMode old = securityMode;
            securityMode = mode;
            tile.markDirty(false);
            tile.onSecurityChanged(old, securityMode);
        }
    }

    @Override
    public void tick() {
        if (!tile.isRemote()) {
            if (getFrequency() == null && ownerUUID != null) {
                tile.getFrequencyComponent().setFrequencyFromData(FrequencyType.SECURITY, new FrequencyIdentity(ownerUUID, true));
            }
        }
    }

    @Override
    public void read(CompoundNBT nbtTags) {
        if (nbtTags.contains(NBTConstants.COMPONENT_SECURITY, NBT.TAG_COMPOUND)) {
            CompoundNBT securityNBT = nbtTags.getCompound(NBTConstants.COMPONENT_SECURITY);
            NBTUtils.setEnumIfPresent(securityNBT, NBTConstants.SECURITY_MODE, SecurityMode::byIndexStatic, mode -> securityMode = mode);
            NBTUtils.setUUIDIfPresent(securityNBT, NBTConstants.OWNER_UUID, uuid -> ownerUUID = uuid);
        }
    }

    @Override
    public void write(CompoundNBT nbtTags) {
        CompoundNBT securityNBT = new CompoundNBT();
        securityNBT.putInt(NBTConstants.SECURITY_MODE, securityMode.ordinal());
        if (ownerUUID != null) {
            securityNBT.putUniqueId(NBTConstants.OWNER_UUID, ownerUUID);
        }
        nbtTags.put(NBTConstants.COMPONENT_SECURITY, securityNBT);
    }

    @Override
    public void trackForMainContainer(MekanismContainer container) {
        container.track(SyncableEnum.create(SecurityMode::byIndexStatic, SecurityMode.PUBLIC, this::getMode, this::setMode));
    }

    @Override
    public void addToUpdateTag(CompoundNBT updateTag) {
        if (ownerUUID != null) {
            updateTag.putUniqueId(NBTConstants.OWNER_UUID, ownerUUID);
            updateTag.putString(NBTConstants.OWNER_NAME, MekanismUtils.getLastKnownUsername(ownerUUID));
        }
    }

    @Override
    public void readFromUpdateTag(CompoundNBT updateTag) {
        NBTUtils.setUUIDIfPresent(updateTag, NBTConstants.OWNER_UUID, uuid -> ownerUUID = uuid);
        NBTUtils.setStringIfPresent(updateTag, NBTConstants.OWNER_NAME, uuid -> ownerName = uuid);
    }
}