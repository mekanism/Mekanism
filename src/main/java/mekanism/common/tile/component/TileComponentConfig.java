package mekanism.common.tile.component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Consumer;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.NBTConstants;
import mekanism.api.RelativeSide;
import mekanism.api.chemical.gas.IGasTank;
import mekanism.api.chemical.infuse.IInfusionTank;
import mekanism.api.chemical.pigment.IPigmentTank;
import mekanism.api.chemical.slurry.ISlurryTank;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.fluid.IExtendedFluidTank;
import mekanism.api.heat.IHeatCapacitor;
import mekanism.api.inventory.IInventorySlot;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.integration.computer.ComputerException;
import mekanism.common.integration.computer.annotation.ComputerMethod;
import mekanism.common.integration.energy.EnergyCompatUtils;
import mekanism.common.inventory.container.MekanismContainer.ISpecificContainerTracker;
import mekanism.common.inventory.container.sync.ISyncableData;
import mekanism.common.inventory.container.sync.SyncableBoolean;
import mekanism.common.lib.transmitter.TransmissionType;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.component.config.ConfigInfo;
import mekanism.common.tile.component.config.DataType;
import mekanism.common.tile.component.config.slot.BaseSlotInfo;
import mekanism.common.tile.component.config.slot.ChemicalSlotInfo.GasSlotInfo;
import mekanism.common.tile.component.config.slot.ChemicalSlotInfo.InfusionSlotInfo;
import mekanism.common.tile.component.config.slot.ChemicalSlotInfo.PigmentSlotInfo;
import mekanism.common.tile.component.config.slot.ChemicalSlotInfo.SlurrySlotInfo;
import mekanism.common.tile.component.config.slot.EnergySlotInfo;
import mekanism.common.tile.component.config.slot.FluidSlotInfo;
import mekanism.common.tile.component.config.slot.HeatSlotInfo;
import mekanism.common.tile.component.config.slot.ISlotInfo;
import mekanism.common.tile.component.config.slot.InventorySlotInfo;
import mekanism.common.util.EnumUtils;
import mekanism.common.util.NBTUtils;
import mekanism.common.util.WorldUtils;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.items.CapabilityItemHandler;

public class TileComponentConfig implements ITileComponent, ISpecificContainerTracker {

    public final TileEntityMekanism tile;
    private final Map<TransmissionType, ConfigInfo> configInfo = new EnumMap<>(TransmissionType.class);
    private final Map<TransmissionType, List<Consumer<Direction>>> configChangeListeners = new EnumMap<>(TransmissionType.class);
    //TODO: See if we can come up with a way of not needing this. The issue is we want this to be sorted, but getting the keySet of configInfo doesn't work for us
    private final List<TransmissionType> transmissionTypes = new ArrayList<>();

    public TileComponentConfig(TileEntityMekanism tile, TransmissionType... types) {
        this.tile = tile;
        for (TransmissionType type : types) {
            addSupported(type);
        }
        tile.addComponent(this);
    }

    public void addConfigChangeListener(TransmissionType transmissionType, Consumer<Direction> listener) {
        //Note: We set the initial capacity to one as currently the only place that really uses this is ConfigHolders
        // and each tile should really only have one holder per transmission type, but we have this as a list for
        // expandability and in case any of the tiles end up needing to make use of this
        configChangeListeners.computeIfAbsent(transmissionType, type -> new ArrayList<>(1)).add(listener);
    }

    public void sideChanged(TransmissionType transmissionType, RelativeSide side) {
        Direction direction = side.getDirection(tile.getDirection());
        sideChangedBasic(transmissionType, direction);
        tile.sendUpdatePacket();
        //Notify the neighbor on that side our state changed
        WorldUtils.notifyNeighborOfChange(tile.getLevel(), direction, tile.getBlockPos());
    }

    private void sideChangedBasic(TransmissionType transmissionType, Direction direction) {
        switch (transmissionType) {
            case ENERGY -> tile.invalidateCapabilities(EnergyCompatUtils.getEnabledEnergyCapabilities(), direction);
            case FLUID -> tile.invalidateCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, direction);
            case GAS -> tile.invalidateCapability(Capabilities.GAS_HANDLER_CAPABILITY, direction);
            case INFUSION -> tile.invalidateCapability(Capabilities.INFUSION_HANDLER_CAPABILITY, direction);
            case PIGMENT -> tile.invalidateCapability(Capabilities.PIGMENT_HANDLER_CAPABILITY, direction);
            case SLURRY -> tile.invalidateCapability(Capabilities.SLURRY_HANDLER_CAPABILITY, direction);
            case ITEM -> tile.invalidateCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, direction);
            case HEAT -> tile.invalidateCapability(Capabilities.HEAT_HANDLER_CAPABILITY, direction);
        }
        tile.markForSave();
        //And invalidate any "listeners" we may have that the side changed for a specific transmission type
        for (Consumer<Direction> listener : configChangeListeners.getOrDefault(transmissionType, Collections.emptyList())) {
            listener.accept(direction);
        }
    }

    private RelativeSide getSide(Direction direction) {
        return RelativeSide.fromDirections(tile.getDirection(), direction);
    }

    @ComputerMethod(nameOverride = "getConfigurableTypes")
    public List<TransmissionType> getTransmissions() {
        return transmissionTypes;
    }

    public void addSupported(TransmissionType type) {
        if (!configInfo.containsKey(type)) {
            configInfo.put(type, new ConfigInfo(tile::getDirection));
            transmissionTypes.add(type);
        }
    }

    public boolean isCapabilityDisabled(@Nonnull Capability<?> capability, Direction side) {
        TransmissionType type = null;
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            type = TransmissionType.ITEM;
        } else if (capability == Capabilities.GAS_HANDLER_CAPABILITY) {
            type = TransmissionType.GAS;
        } else if (capability == Capabilities.INFUSION_HANDLER_CAPABILITY) {
            type = TransmissionType.INFUSION;
        } else if (capability == Capabilities.PIGMENT_HANDLER_CAPABILITY) {
            type = TransmissionType.PIGMENT;
        } else if (capability == Capabilities.SLURRY_HANDLER_CAPABILITY) {
            type = TransmissionType.SLURRY;
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
                //If we support this config type, and we have a side so are not the read only "internal" check
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

    public void addDisabledSides(@Nonnull RelativeSide... sides) {
        for (ConfigInfo config : configInfo.values()) {
            config.addDisabledSides(sides);
        }
    }

    public ConfigInfo setupInputConfig(TransmissionType type, Object container) {
        ConfigInfo config = getConfig(type);
        if (config != null) {
            config.addSlotInfo(DataType.INPUT, createInfo(type, true, false, container));
            config.fill(DataType.INPUT);
            config.setCanEject(false);
        }
        return config;
    }

    public ConfigInfo setupOutputConfig(TransmissionType type, Object container, RelativeSide... sides) {
        ConfigInfo config = getConfig(type);
        if (config != null) {
            config.addSlotInfo(DataType.OUTPUT, createInfo(type, false, true, container));
            config.setDataType(DataType.OUTPUT, sides);
            config.setEjecting(true);
        }
        return config;
    }

    public ConfigInfo setupIOConfig(TransmissionType type, Object inputInfo, Object outputInfo, RelativeSide outputSide) {
        return setupIOConfig(type, inputInfo, outputInfo, outputSide, false);
    }

    public ConfigInfo setupIOConfig(TransmissionType type, Object inputContainer, Object outputContainer, RelativeSide outputSide, boolean alwaysAllow) {
        return setupIOConfig(type, inputContainer, outputContainer, outputSide, alwaysAllow, alwaysAllow);
    }

    public ConfigInfo setupIOConfig(TransmissionType type, Object inputContainer, Object outputContainer, RelativeSide outputSide, boolean alwaysAllowInput,
          boolean alwaysAllowOutput) {
        ConfigInfo config = getConfig(type);
        if (config != null) {
            config.addSlotInfo(DataType.INPUT, createInfo(type, true, alwaysAllowOutput, inputContainer));
            config.addSlotInfo(DataType.OUTPUT, createInfo(type, alwaysAllowInput, true, outputContainer));
            config.addSlotInfo(DataType.INPUT_OUTPUT, createInfo(type, true, true, List.of(inputContainer, outputContainer)));
            config.fill(DataType.INPUT);
            config.setDataType(DataType.OUTPUT, outputSide);
        }
        return config;
    }

    public ConfigInfo setupIOConfig(TransmissionType type, Object info, RelativeSide outputSide) {
        return setupIOConfig(type, info, outputSide, false);
    }

    public ConfigInfo setupIOConfig(TransmissionType type, Object info, RelativeSide outputSide, boolean alwaysAllow) {
        ConfigInfo config = getConfig(type);
        if (config != null) {
            config.addSlotInfo(DataType.INPUT, createInfo(type, true, alwaysAllow, info));
            config.addSlotInfo(DataType.OUTPUT, createInfo(type, alwaysAllow, true, info));
            config.addSlotInfo(DataType.INPUT_OUTPUT, createInfo(type, true, true, info));
            config.fill(DataType.INPUT);
            config.setDataType(DataType.OUTPUT, outputSide);
        }
        return config;
    }

    public ConfigInfo setupItemIOConfig(IInventorySlot inputSlot, IInventorySlot outputSlot, IInventorySlot energySlot) {
        return setupItemIOConfig(Collections.singletonList(inputSlot), Collections.singletonList(outputSlot), energySlot, false);
    }

    public ConfigInfo setupItemIOConfig(List<IInventorySlot> inputSlots, List<IInventorySlot> outputSlots, IInventorySlot energySlot, boolean alwaysAllow) {
        ConfigInfo itemConfig = getConfig(TransmissionType.ITEM);
        if (itemConfig != null) {
            itemConfig.addSlotInfo(DataType.INPUT, new InventorySlotInfo(true, alwaysAllow, inputSlots));
            itemConfig.addSlotInfo(DataType.OUTPUT, new InventorySlotInfo(alwaysAllow, true, outputSlots));
            List<IInventorySlot> ioSlots = new ArrayList<>(inputSlots);
            ioSlots.addAll(outputSlots);
            itemConfig.addSlotInfo(DataType.INPUT_OUTPUT, new InventorySlotInfo(true, true, ioSlots));
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
            itemConfig.addSlotInfo(DataType.INPUT_OUTPUT, new InventorySlotInfo(true, true, inputSlot, outputSlot));
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
    public void read(CompoundTag nbtTags) {
        NBTUtils.setCompoundIfPresent(nbtTags, NBTConstants.COMPONENT_CONFIG, configNBT -> {
            Set<Direction> directionsToUpdate = EnumSet.noneOf(Direction.class);
            configInfo.forEach((type, info) -> {
                info.setEjecting(configNBT.getBoolean(NBTConstants.EJECT + type.ordinal()));
                CompoundTag sideConfig = configNBT.getCompound(NBTConstants.CONFIG + type.ordinal());
                for (RelativeSide side : EnumUtils.SIDES) {
                    NBTUtils.setEnumIfPresent(sideConfig, NBTConstants.SIDE + side.ordinal(), DataType::byIndexStatic, dataType -> {
                        if (info.getDataType(side) != dataType) {
                            info.setDataType(dataType, side);
                            if (tile.hasLevel()) {//If we aren't already loaded yet don't do any updates
                                Direction direction = side.getDirection(tile.getDirection());
                                sideChangedBasic(type, direction);
                                directionsToUpdate.add(direction);
                            }
                        }
                    });
                }
            });
            WorldUtils.notifyNeighborsOfChange(tile.getLevel(), tile.getBlockPos(), directionsToUpdate);
        });
    }

    @Override
    public void write(CompoundTag nbtTags) {
        CompoundTag configNBT = new CompoundTag();
        for (Entry<TransmissionType, ConfigInfo> entry : configInfo.entrySet()) {
            TransmissionType type = entry.getKey();
            ConfigInfo info = entry.getValue();
            configNBT.putBoolean(NBTConstants.EJECT + type.ordinal(), info.isEjecting());
            CompoundTag sideConfig = new CompoundTag();
            for (RelativeSide side : EnumUtils.SIDES) {
                NBTUtils.writeEnum(sideConfig, NBTConstants.SIDE + side.ordinal(), info.getDataType(side));
            }
            configNBT.put(NBTConstants.CONFIG + type.ordinal(), sideConfig);
        }
        nbtTags.put(NBTConstants.COMPONENT_CONFIG, configNBT);
    }

    @Override
    public void addToUpdateTag(CompoundTag updateTag) {
        //Note: This is slightly different from read and write as we don't bother syncing the ejecting status
        CompoundTag configNBT = new CompoundTag();
        for (Entry<TransmissionType, ConfigInfo> entry : configInfo.entrySet()) {
            TransmissionType type = entry.getKey();
            ConfigInfo info = entry.getValue();
            CompoundTag sideConfig = new CompoundTag();
            for (RelativeSide side : EnumUtils.SIDES) {
                NBTUtils.writeEnum(sideConfig, NBTConstants.SIDE + side.ordinal(), info.getDataType(side));
            }
            configNBT.put(NBTConstants.CONFIG + type.ordinal(), sideConfig);
        }
        updateTag.put(NBTConstants.COMPONENT_CONFIG, configNBT);
    }

    @Override
    public void readFromUpdateTag(CompoundTag updateTag) {
        if (updateTag.contains(NBTConstants.COMPONENT_CONFIG, Tag.TAG_COMPOUND)) {
            CompoundTag configNBT = updateTag.getCompound(NBTConstants.COMPONENT_CONFIG);
            for (Entry<TransmissionType, ConfigInfo> entry : configInfo.entrySet()) {
                TransmissionType type = entry.getKey();
                ConfigInfo info = entry.getValue();
                CompoundTag sideConfig = configNBT.getCompound(NBTConstants.CONFIG + type.ordinal());
                for (RelativeSide side : EnumUtils.SIDES) {
                    NBTUtils.setEnumIfPresent(sideConfig, NBTConstants.SIDE + side.ordinal(), DataType::byIndexStatic, dataType -> info.setDataType(dataType, side));
                }
            }
        }
    }

    @Override
    public List<ISyncableData> getSpecificSyncableData() {
        List<ISyncableData> list = new ArrayList<>();
        List<TransmissionType> transmissions = getTransmissions();
        for (TransmissionType transmission : transmissions) {
            ConfigInfo info = configInfo.get(transmission);
            list.add(SyncableBoolean.create(info::isEjecting, info::setEjecting));
        }
        return list;
    }

    public static BaseSlotInfo createInfo(TransmissionType type, boolean input, boolean output, Object... containers) {
        return createInfo(type, input, output, List.of(containers));
    }

    @SuppressWarnings("unchecked")
    public static BaseSlotInfo createInfo(TransmissionType type, boolean input, boolean output, List<?> containers) {
        return switch (type) {
            case ITEM -> new InventorySlotInfo(input, output, (List<IInventorySlot>) containers);
            case FLUID -> new FluidSlotInfo(input, output, (List<IExtendedFluidTank>) containers);
            case GAS -> new GasSlotInfo(input, output, (List<IGasTank>) containers);
            case INFUSION -> new InfusionSlotInfo(input, output, (List<IInfusionTank>) containers);
            case PIGMENT -> new PigmentSlotInfo(input, output, (List<IPigmentTank>) containers);
            case SLURRY -> new SlurrySlotInfo(input, output, (List<ISlurryTank>) containers);
            case ENERGY -> new EnergySlotInfo(input, output, (List<IEnergyContainer>) containers);
            case HEAT -> new HeatSlotInfo(input, output, (List<IHeatCapacitor>) containers);
        };
    }

    //Computer related methods
    private void validateSupportedTransmissionType(TransmissionType type) throws ComputerException {
        if (!supports(type)) {
            throw new ComputerException("This machine does not support configuring transmission type '%s'.", type);
        }
    }

    @ComputerMethod
    private boolean canEject(TransmissionType type) throws ComputerException {
        validateSupportedTransmissionType(type);
        return configInfo.get(type).canEject();
    }

    @ComputerMethod
    private boolean isEjecting(TransmissionType type) throws ComputerException {
        validateSupportedTransmissionType(type);
        return configInfo.get(type).isEjecting();
    }

    @ComputerMethod
    private void setEjecting(TransmissionType type, boolean ejecting) throws ComputerException {
        tile.validateSecurityIsPublic();
        validateSupportedTransmissionType(type);
        ConfigInfo config = configInfo.get(type);
        if (!config.canEject()) {
            throw new ComputerException("This machine does not support auto-ejecting for transmission type '%s'.", type);
        }
        if (config.isEjecting() != ejecting) {
            config.setEjecting(ejecting);
            tile.markForSave();
        }
    }

    @ComputerMethod
    private Set<DataType> getSupportedModes(TransmissionType type) throws ComputerException {
        validateSupportedTransmissionType(type);
        return configInfo.get(type).getSupportedDataTypes();
    }

    @ComputerMethod
    private DataType getMode(TransmissionType type, RelativeSide side) throws ComputerException {
        validateSupportedTransmissionType(type);
        return configInfo.get(type).getDataType(side);
    }

    @ComputerMethod
    private void setMode(TransmissionType type, RelativeSide side, DataType mode) throws ComputerException {
        tile.validateSecurityIsPublic();
        validateSupportedTransmissionType(type);
        ConfigInfo config = configInfo.get(type);
        if (!config.getSupportedDataTypes().contains(mode)) {
            throw new ComputerException("This machine does not support mode '%s' for transmission type '%s'.", mode, type);
        }
        DataType currentMode = config.getDataType(side);
        if (mode != currentMode) {
            config.setDataType(mode, side);
            sideChanged(type, side);
        }
    }

    @ComputerMethod
    private void incrementMode(TransmissionType type, RelativeSide side) throws ComputerException {
        tile.validateSecurityIsPublic();
        validateSupportedTransmissionType(type);
        ConfigInfo configInfo = this.configInfo.get(type);
        if (configInfo.getDataType(side) != configInfo.incrementDataType(side)) {
            sideChanged(type, side);
        }
    }

    @ComputerMethod
    private void decrementMode(TransmissionType type, RelativeSide side) throws ComputerException {
        tile.validateSecurityIsPublic();
        validateSupportedTransmissionType(type);
        ConfigInfo configInfo = this.configInfo.get(type);
        if (configInfo.getDataType(side) != configInfo.decrementDataType(side)) {
            sideChanged(type, side);
        }
    }
    //End computer related methods
}