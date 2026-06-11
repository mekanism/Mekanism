package mekanism.common.tile.component;

import java.util.List;
import java.util.UUID;
import mekanism.api.SerializationConstants;
import mekanism.api.security.IBlockSecurityUtils;
import mekanism.api.security.SecurityMode;
import mekanism.common.integration.computer.annotation.ComputerMethod;
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.inventory.container.sync.SyncableEnum;
import mekanism.common.lib.frequency.Frequency.FrequencyIdentity;
import mekanism.common.lib.frequency.FrequencyTypes;
import mekanism.common.lib.security.SecurityFrequency;
import mekanism.common.registries.MekanismDataComponents;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.NBTUtils;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

public class TileComponentSecurity implements ITileComponent {

    /// TileEntity implementing this component.
    public final TileEntityMekanism tile;

    @Nullable
    private UUID ownerUUID;
    @Nullable
    private String ownerName;

    private SecurityMode securityMode = SecurityMode.PUBLIC;

    public TileComponentSecurity(TileEntityMekanism tile) {
        this.tile = tile;
        tile.addComponent(this);
        tile.getFrequencyComponent().track(FrequencyTypes.SECURITY, true, false, true);
    }

    @Nullable
    public SecurityFrequency getFrequency() {
        return tile.getFrequency(FrequencyTypes.SECURITY);
    }

    @Nullable
    @ComputerMethod
    public UUID getOwnerUUID() {
        return ownerUUID;
    }

    public void setOwnerUUID(@Nullable UUID uuid) {
        ownerUUID = uuid;
        if (ownerUUID == null) {
            tile.getFrequencyComponent().unsetFrequency(FrequencyTypes.SECURITY);
        } else {
            tile.setFrequency(FrequencyTypes.SECURITY, new FrequencyIdentity(ownerUUID, SecurityMode.PUBLIC, ownerUUID), ownerUUID);
        }
    }

    @Nullable
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

    @Override
    public String getComponentKey() {
        return SerializationConstants.COMPONENT_SECURITY;
    }

    @Override
    public void applyImplicitComponents(DataComponentGetter input) {
        securityMode = input.getOrDefault(MekanismDataComponents.SECURITY, securityMode);
        UUID owner = input.get(MekanismDataComponents.OWNER);
        if (owner != null) {
            setOwnerUUID(owner);
        }
    }

    @Override
    public void addRemapEntries(List<DataComponentType<?>> remapEntries) {
        if (ownerUUID == null) {
            remapEntries.add(MekanismDataComponents.OWNER.get());
        }
    }

    @Override
    public void collectImplicitComponents(DataComponentMap.Builder builder) {
        builder.set(MekanismDataComponents.SECURITY, securityMode);
        if (ownerUUID != null) {
            builder.set(MekanismDataComponents.OWNER, ownerUUID);
        }
    }

    @Override
    public void deserialize(ValueInput securityInput) {
        NBTUtils.setEnumIfPresent(securityInput, SerializationConstants.SECURITY_MODE, SecurityMode.BY_ID, mode -> securityMode = mode);
        //Note: We can just set the owner uuid directly as the frequency data should be set already from the frequency component
        // Or if it was cleared due to changing owner data as an item, the block place should update it properly
        //TODO: If this ends up causing issues anywhere we may want to consider ensuring the frequency gets set if it is missing
        securityInput.read(SerializationConstants.OWNER_UUID, UUIDUtil.CODEC).ifPresent(uuid -> ownerUUID = uuid);
    }

    @Override
    public void serialize(ValueOutput securityOutput) {
        if (securityMode != SecurityMode.PUBLIC) {
            NBTUtils.writeEnum(securityOutput, SerializationConstants.SECURITY_MODE, securityMode);
        }
        securityOutput.storeNullable(SerializationConstants.OWNER_UUID, UUIDUtil.CODEC, ownerUUID);
    }

    @Override
    public void trackForMainContainer(MekanismContainer container) {
        container.track(SyncableEnum.create(SecurityMode.BY_ID, SecurityMode.PUBLIC, this::getMode, this::setMode));
    }

    @Override
    public void addToUpdateTag(ValueOutput output) {
        if (ownerUUID != null) {
            output.store(SerializationConstants.OWNER_UUID, UUIDUtil.CODEC, ownerUUID);
            output.putString(SerializationConstants.OWNER_NAME, MekanismUtils.getLastKnownUsername(ownerUUID));
        }
    }

    @Override
    public void readFromUpdateTag(ValueInput input) {
        input.read(SerializationConstants.OWNER_UUID, UUIDUtil.CODEC).ifPresent(uuid -> ownerUUID = uuid);
        ownerName = input.getString(SerializationConstants.OWNER_NAME).orElse(ownerName);
    }

    //Computer related methods
    @ComputerMethod(nameOverride = "getSecurityMode")
    SecurityMode getComputerSecurityMode() {
        //Get the effective security mode
        if (tile.getLevel() == null) {
            //If we don't have a level yet do our best effort to return a usable value
            // though given this is just for computer access we should theoretically always have a level
            return getMode();
        }
        return IBlockSecurityUtils.INSTANCE.getSecurityMode(tile.getLevel(), tile.getBlockPos(), tile);
    }
    //End computer related methods
}