package mekanism.common.tile.component;

import java.util.UUID;
import mekanism.api.MekanismAPI;
import mekanism.api.NBTConstants;
import mekanism.api.security.SecurityMode;
import mekanism.common.integration.computer.annotation.ComputerMethod;
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.inventory.container.sync.SyncableEnum;
import mekanism.common.lib.frequency.Frequency.FrequencyIdentity;
import mekanism.common.lib.frequency.FrequencyType;
import mekanism.common.lib.security.SecurityFrequency;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.NBTUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;

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

    @ComputerMethod
    public UUID getOwnerUUID() {
        return ownerUUID;
    }

    public void setOwnerUUID(UUID uuid) {
        tile.getFrequencyComponent().setFrequency(FrequencyType.SECURITY, null);
        ownerUUID = uuid;
    }

    @ComputerMethod
    public String getOwnerName() {
        return ownerName;
    }

    public SecurityMode getMode() {
        return securityMode;
    }

    public void setMode(SecurityMode mode) {
        if (securityMode != mode) {
            SecurityMode old = securityMode;
            securityMode = mode;
            tile.onSecurityChanged(old, securityMode);
            if (!tile.isRemote()) {
                tile.markForSave();
            }
        }
    }

    public void tickServer() {
        if (getFrequency() == null && ownerUUID != null) {
            tile.setFrequency(FrequencyType.SECURITY, new FrequencyIdentity(ownerUUID, true), ownerUUID);
        }
    }

    @Override
    public void read(CompoundTag nbtTags) {
        if (nbtTags.contains(NBTConstants.COMPONENT_SECURITY, Tag.TAG_COMPOUND)) {
            CompoundTag securityNBT = nbtTags.getCompound(NBTConstants.COMPONENT_SECURITY);
            NBTUtils.setEnumIfPresent(securityNBT, NBTConstants.SECURITY_MODE, SecurityMode::byIndexStatic, mode -> securityMode = mode);
            NBTUtils.setUUIDIfPresent(securityNBT, NBTConstants.OWNER_UUID, uuid -> ownerUUID = uuid);
        }
    }

    @Override
    public void write(CompoundTag nbtTags) {
        CompoundTag securityNBT = new CompoundTag();
        NBTUtils.writeEnum(securityNBT, NBTConstants.SECURITY_MODE, securityMode);
        if (ownerUUID != null) {
            securityNBT.putUUID(NBTConstants.OWNER_UUID, ownerUUID);
        }
        nbtTags.put(NBTConstants.COMPONENT_SECURITY, securityNBT);
    }

    @Override
    public void trackForMainContainer(MekanismContainer container) {
        container.track(SyncableEnum.create(SecurityMode::byIndexStatic, SecurityMode.PUBLIC, this::getMode, this::setMode));
    }

    @Override
    public void addToUpdateTag(CompoundTag updateTag) {
        if (ownerUUID != null) {
            updateTag.putUUID(NBTConstants.OWNER_UUID, ownerUUID);
            updateTag.putString(NBTConstants.OWNER_NAME, MekanismUtils.getLastKnownUsername(ownerUUID));
        }
    }

    @Override
    public void readFromUpdateTag(CompoundTag updateTag) {
        NBTUtils.setUUIDIfPresent(updateTag, NBTConstants.OWNER_UUID, uuid -> ownerUUID = uuid);
        NBTUtils.setStringIfPresent(updateTag, NBTConstants.OWNER_NAME, name -> ownerName = name);
    }

    //Computer related methods
    @ComputerMethod(nameOverride = "getSecurityMode")
    private SecurityMode getComputerSecurityMode() {
        //Get the effective security mode
        return MekanismAPI.getSecurityUtils().getSecurityMode(tile, tile.isRemote());
    }
    //End computer related methods
}