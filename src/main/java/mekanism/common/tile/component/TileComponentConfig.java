package mekanism.common.tile.component;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.NBTConstants;
import mekanism.api.RelativeSide;
import mekanism.api.inventory.IInventorySlot;
import mekanism.api.transmitters.TransmissionType;
import mekanism.common.base.ITileComponent;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.integration.energy.EnergyCompatUtils;
import mekanism.common.inventory.container.ITrackableContainer;
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.inventory.container.sync.SyncableBoolean;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.component.config.ConfigInfo;
import mekanism.common.tile.component.config.DataType;
import mekanism.common.tile.component.config.slot.BaseSlotInfo;
import mekanism.common.tile.component.config.slot.ISlotInfo;
import mekanism.common.tile.component.config.slot.InventorySlotInfo;
import mekanism.common.util.EnumUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.NBTUtils;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.items.CapabilityItemHandler;

public class TileComponentConfig implements ITileComponent, ITrackableContainer {

    public TileEntityMekanism tile;
    private Map<TransmissionType, ConfigInfo> configInfo = new EnumMap<>(TransmissionType.class);
    //TODO: See if we can come up with a way of not needing this. The issue is we want this to be sorted, but getting the keyset of configInfo doesn't work for us
    private List<TransmissionType> transmissionTypes = new ArrayList<>();

    public TileComponentConfig(TileEntityMekanism tile, TransmissionType... types) {
        this.tile = tile;
        for (TransmissionType type : types) {
            addSupported(type);
        }
        tile.addComponent(this);
    }

    public void sideChanged(TransmissionType transmissionType, RelativeSide side) {
        //TODO: Instead of getDirection this should use ISideConfiguration#getOrientation
        Direction direction = side.getDirection(tile.getDirection());
        switch (transmissionType) {
            case ENERGY:
                tile.invalidateCapabilities(EnergyCompatUtils.getEnabledEnergyCapabilities(), direction);
                break;
            case FLUID:
                tile.invalidateCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, direction);
                break;
            case GAS:
                tile.invalidateCapability(Capabilities.GAS_HANDLER_CAPABILITY, direction);
                break;
            case ITEM:
                tile.invalidateCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, direction);
                break;
            case HEAT:
                tile.invalidateCapability(Capabilities.HEAT_HANDLER_CAPABILITY, direction);
                break;
        }
        tile.sendUpdatePacket();
        tile.markDirty(false);
        //TODO - V10: We can probably remove the extra neighbor update notification
        //Notify the neighbor on that side our state changed
        MekanismUtils.notifyNeighborOfChange(tile.getWorld(), direction, tile.getPos());
    }

    private RelativeSide getSide(Direction direction) {
        //TODO: Instead of getDirection this should use ISideConfiguration#getOrientation
        return RelativeSide.fromDirections(tile.getDirection(), direction);
    }

    public List<TransmissionType> getTransmissions() {
        return transmissionTypes;
    }

    public void addSupported(TransmissionType type) {
        if (!configInfo.containsKey(type)) {
            //TODO: ISideConfiguration#getOrientation?
            configInfo.put(type, new ConfigInfo(() -> tile.getDirection()));
            transmissionTypes.add(type);
        }
    }

    public boolean isCapabilityDisabled(@Nonnull Capability<?> capability, Direction side) {
        TransmissionType type = null;
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            type = TransmissionType.ITEM;
        } else if (capability == Capabilities.GAS_HANDLER_CAPABILITY) {
            type = TransmissionType.GAS;
        } else if (capability == Capabilities.HEAT_HANDLER_CAPABILITY) {
            type = TransmissionType.HEAT;
        } else if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
            type = TransmissionType.FLUID;
        } else if (EnergyCompatUtils.isEnergyCapability(capability)) {
            type = TransmissionType.ENERGY;
        }
        if (type != null) {
            ConfigInfo info = getConfig(type);
            if (info != null && side != null) {
                //If we support this config type and we have a side so are not the read only "internal" check
                ISlotInfo slotInfo = info.getSlotInfo(getSide(side));
                //Return that it is disabled:
                // If we don't know how to handle the data type that is on that side config (such as for NONE)
                // or the slot is not enabled then return that it is disabled
                return slotInfo == null || !slotInfo.isEnabled();
            }
        }
        return false;
    }

    @Nullable
    public ConfigInfo getConfig(TransmissionType type) {
        return configInfo.get(type);
    }

    public ConfigInfo setupInputConfig(TransmissionType type, BaseSlotInfo slotInfo) {
        ConfigInfo config = getConfig(type);
        if (config != null) {
            config.addSlotInfo(DataType.INPUT, slotInfo);
            config.fill(DataType.INPUT);
            config.setCanEject(false);
        }
        return config;
    }

    public ConfigInfo setupOutputConfig(TransmissionType type, BaseSlotInfo slotInfo, RelativeSide... sides) {
        ConfigInfo config = getConfig(type);
        if (config != null) {
            config.addSlotInfo(DataType.OUTPUT, slotInfo);
            config.setDataType(DataType.OUTPUT, sides);
            config.setEjecting(true);
        }
        return config;
    }

    public ConfigInfo setupIOConfig(TransmissionType type, BaseSlotInfo inputInfo, BaseSlotInfo outputInfo, RelativeSide outputSide) {
        ConfigInfo gasConfig = getConfig(type);
        if (gasConfig != null) {
            gasConfig.addSlotInfo(DataType.INPUT, inputInfo);
            gasConfig.addSlotInfo(DataType.OUTPUT, outputInfo);
            gasConfig.fill(DataType.INPUT);
            gasConfig.setDataType(DataType.OUTPUT, outputSide);
        }
        return gasConfig;
    }

    public ConfigInfo setupItemIOConfig(IInventorySlot inputSlot, IInventorySlot outputSlot, IInventorySlot energySlot) {
        ConfigInfo itemConfig = getConfig(TransmissionType.ITEM);
        if (itemConfig != null) {
            itemConfig.addSlotInfo(DataType.INPUT, new InventorySlotInfo(true, false, inputSlot));
            itemConfig.addSlotInfo(DataType.OUTPUT, new InventorySlotInfo(false, true, outputSlot));
            itemConfig.addSlotInfo(DataType.ENERGY, new InventorySlotInfo(true, true, energySlot));
            //Set default config directions
            itemConfig.setDefaults();
        }
        return itemConfig;
    }

    public ConfigInfo setupItemIOExtraConfig(IInventorySlot inputSlot, IInventorySlot outputSlot, IInventorySlot extraSlot, IInventorySlot energySlot) {
        ConfigInfo itemConfig = getConfig(TransmissionType.ITEM);
        if (itemConfig != null) {
            itemConfig.addSlotInfo(DataType.INPUT, new InventorySlotInfo(true, false, inputSlot));
            itemConfig.addSlotInfo(DataType.OUTPUT, new InventorySlotInfo(false, true, outputSlot));
            itemConfig.addSlotInfo(DataType.EXTRA, new InventorySlotInfo(true, true, extraSlot));
            itemConfig.addSlotInfo(DataType.ENERGY, new InventorySlotInfo(true, true, energySlot));
            //Set default config directions
            itemConfig.setDefaults();
        }
        return itemConfig;
    }

    @Nullable
    public DataType getDataType(TransmissionType type, RelativeSide side) {
        ConfigInfo info = getConfig(type);
        if (info == null) {
            return null;
        }
        return info.getDataType(side);
    }

    //TODO: Use relative side where possible?
    @Nullable
    public ISlotInfo getSlotInfo(TransmissionType type, Direction direction) {
        if (direction == null) {
            return null;
        }
        ConfigInfo info = getConfig(type);
        if (info == null) {
            return null;
        }
        return info.getSlotInfo(getSide(direction));
    }

    public boolean supports(TransmissionType type) {
        return configInfo.containsKey(type);
    }

    @Override
    public void tick() {
    }

    @Override
    public void read(CompoundNBT nbtTags) {
        if (nbtTags.contains(NBTConstants.COMPONENT_CONFIG, NBT.TAG_COMPOUND)) {
            CompoundNBT configNBT = nbtTags.getCompound(NBTConstants.COMPONENT_CONFIG);
            for (Entry<TransmissionType, ConfigInfo> entry : configInfo.entrySet()) {
                TransmissionType type = entry.getKey();
                ConfigInfo info = entry.getValue();
                info.setEjecting(configNBT.getBoolean(NBTConstants.EJECT + type.ordinal()));
                CompoundNBT sideConfig = configNBT.getCompound(NBTConstants.CONFIG + type.ordinal());
                for (RelativeSide side : EnumUtils.SIDES) {
                    NBTUtils.setEnumIfPresent(sideConfig, NBTConstants.SIDE + side.ordinal(), DataType::byIndexStatic, dataType -> info.setDataType(dataType, side));
                }
            }
        }
    }

    @Override
    public void write(CompoundNBT nbtTags) {
        CompoundNBT configNBT = new CompoundNBT();
        for (Entry<TransmissionType, ConfigInfo> entry : configInfo.entrySet()) {
            TransmissionType type = entry.getKey();
            ConfigInfo info = entry.getValue();
            configNBT.putBoolean(NBTConstants.EJECT + type.ordinal(), info.isEjecting());
            CompoundNBT sideConfig = new CompoundNBT();
            for (RelativeSide side : EnumUtils.SIDES) {
                sideConfig.putInt(NBTConstants.SIDE + side.ordinal(), info.getDataType(side).ordinal());
            }
            configNBT.put(NBTConstants.CONFIG + type.ordinal(), sideConfig);
        }
        nbtTags.put(NBTConstants.COMPONENT_CONFIG, configNBT);
    }

    @Override
    public void invalidate() {
    }

    @Override
    public void trackForMainContainer(MekanismContainer container) {
    }

    @Override
    public void addToUpdateTag(CompoundNBT updateTag) {
        //Note: This is slightly different from read and write as we don't bother syncing the ejecting status
        CompoundNBT configNBT = new CompoundNBT();
        for (Entry<TransmissionType, ConfigInfo> entry : configInfo.entrySet()) {
            TransmissionType type = entry.getKey();
            ConfigInfo info = entry.getValue();
            CompoundNBT sideConfig = new CompoundNBT();
            for (RelativeSide side : EnumUtils.SIDES) {
                sideConfig.putInt(NBTConstants.SIDE + side.ordinal(), info.getDataType(side).ordinal());
            }
            configNBT.put(NBTConstants.CONFIG + type.ordinal(), sideConfig);
        }
        updateTag.put(NBTConstants.COMPONENT_CONFIG, configNBT);
    }

    @Override
    public void readFromUpdateTag(CompoundNBT updateTag) {
        if (updateTag.contains(NBTConstants.COMPONENT_CONFIG, NBT.TAG_COMPOUND)) {
            CompoundNBT configNBT = updateTag.getCompound(NBTConstants.COMPONENT_CONFIG);
            for (Entry<TransmissionType, ConfigInfo> entry : configInfo.entrySet()) {
                TransmissionType type = entry.getKey();
                ConfigInfo info = entry.getValue();
                CompoundNBT sideConfig = configNBT.getCompound(NBTConstants.CONFIG + type.ordinal());
                for (RelativeSide side : EnumUtils.SIDES) {
                    NBTUtils.setEnumIfPresent(sideConfig, NBTConstants.SIDE + side.ordinal(), DataType::byIndexStatic, dataType -> info.setDataType(dataType, side));
                }
            }
        }
    }

    @Override
    public void addContainerTrackers(MekanismContainer container) {
        List<TransmissionType> transmissions = getTransmissions();
        for (TransmissionType transmission : transmissions) {
            ConfigInfo info = configInfo.get(transmission);
            container.track(SyncableBoolean.create(info::isEjecting, info::setEjecting));
        }
    }
}