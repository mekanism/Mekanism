package mekanism.common.tile.component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import mekanism.api.SerializationConstants;
import mekanism.api.RelativeSide;
import mekanism.api.chemical.IChemicalTank;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.fluid.IExtendedFluidTank;
import mekanism.api.heat.IHeatCapacitor;
import mekanism.api.inventory.IInventorySlot;
import mekanism.common.attachments.component.AttachedSideConfig;
import mekanism.common.attachments.component.AttachedSideConfig.LightConfigInfo;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.integration.computer.ComputerException;
import mekanism.common.integration.computer.annotation.ComputerMethod;
import mekanism.common.integration.energy.EnergyCompatUtils;
import mekanism.common.inventory.container.MekanismContainer.ISpecificContainerTracker;
import mekanism.common.inventory.container.sync.ISyncableData;
import mekanism.common.inventory.container.sync.SyncableBoolean;
import mekanism.common.lib.transmitter.TransmissionType;
import mekanism.common.registries.MekanismDataComponents;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.component.config.ConfigInfo;
import mekanism.common.tile.component.config.DataType;
import mekanism.common.tile.component.config.IPersistentConfigInfo;
import mekanism.common.tile.component.config.slot.BaseSlotInfo;
import mekanism.common.tile.component.config.slot.ChemicalSlotInfo;
import mekanism.common.tile.component.config.slot.EnergySlotInfo;
import mekanism.common.tile.component.config.slot.FluidSlotInfo;
import mekanism.common.tile.component.config.slot.HeatSlotInfo;
import mekanism.common.tile.component.config.slot.ISlotInfo;
import mekanism.common.tile.component.config.slot.InventorySlotInfo;
import mekanism.common.util.EnumUtils;
import mekanism.common.util.NBTUtils;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.capabilities.BlockCapability;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TileComponentConfig implements ITileComponent, ISpecificContainerTracker {

    public static final String LEGACY_ITEM_EJECT_KEY = SerializationConstants.EJECT + TransmissionType.ITEM.getLegacyOrdinal();
    public static final String LEGACY_ITEM_CONFIG_KEY = SerializationConstants.CONFIG + TransmissionType.ITEM.getLegacyOrdinal();
    public final TileEntityMekanism tile;
    private final Map<TransmissionType, ConfigInfo> configInfo = new EnumMap<>(TransmissionType.class);
    private final Map<TransmissionType, List<Consumer<Direction>>> configChangeListeners = new EnumMap<>(TransmissionType.class);
    //TODO: See if we can come up with a way of not needing this. The issue is we want this to be sorted, but getting the keySet of configInfo doesn't work for us
    private final List<TransmissionType> transmissionTypes = new ArrayList<>();

    public TileComponentConfig(TileEntityMekanism tile, Set<TransmissionType> types) {
        this.tile = tile;
        for (TransmissionType type : types) {
            configInfo.put(type, new ConfigInfo());
            transmissionTypes.add(type);
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
    }

    private void sideChangedBasic(TransmissionType transmissionType, Direction direction) {
        switch (transmissionType) {
            case ENERGY -> tile.invalidateCapabilities(EnergyCompatUtils.getLoadedEnergyCapabilities(), direction);
            case FLUID -> tile.invalidateCapability(Capabilities.FLUID.block(), direction);
            case CHEMICAL -> tile.invalidateCapability(Capabilities.CHEMICAL.block(), direction);
            case ITEM -> tile.invalidateCapability(Capabilities.ITEM.block(), direction);
            case HEAT -> tile.invalidateCapability(Capabilities.HEAT, direction);
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

    public boolean isCapabilityDisabled(@NotNull BlockCapability<?, @Nullable Direction> capability, @Nullable Direction side) {
        TransmissionType type = null;
        if (Capabilities.ITEM.is(capability)) {
            type = TransmissionType.ITEM;
        } else if (Capabilities.CHEMICAL.is(capability)) {
            type = TransmissionType.CHEMICAL;
        } else if (capability == Capabilities.HEAT) {
            type = TransmissionType.HEAT;
        } else if (Capabilities.FLUID.is(capability)) {
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

    public void addDisabledSides(@NotNull RelativeSide... sides) {
        for (ConfigInfo config : configInfo.values()) {
            config.addDisabledSides(sides);
        }
    }

    public ConfigInfo setupInputConfig(TransmissionType type, Object container) {
        ConfigInfo config = getConfig(type);
        if (config != null) {
            config.addSlotInfo(DataType.INPUT, createInfo(type, true, false, container));
            config.setCanEject(false);
        }
        return config;
    }

    public ConfigInfo setupOutputConfig(TransmissionType type, Object container, RelativeSide... sides) {
        ConfigInfo config = getConfig(type);
        if (config != null) {
            config.addSlotInfo(DataType.OUTPUT, createInfo(type, false, true, container));
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
    public String getComponentKey() {
        return SerializationConstants.COMPONENT_CONFIG;
    }

    @Override
    public void applyImplicitComponents(@NotNull BlockEntity.DataComponentInput input) {
        AttachedSideConfig sideConfig = input.get(MekanismDataComponents.SIDE_CONFIG);
        if (sideConfig != null) {
            for (Entry<TransmissionType, LightConfigInfo> entry : sideConfig.configInfo().entrySet()) {
                TransmissionType type = entry.getKey();
                ConfigInfo info = configInfo.get(type);
                if (info != null) {
                    LightConfigInfo lightInfo = entry.getValue();
                    info.setEjecting(lightInfo.isEjecting());
                    for (Map.Entry<RelativeSide, DataType> sideEntry : lightInfo.sideConfig().entrySet()) {
                        RelativeSide side = sideEntry.getKey();
                        if (info.setDataType(sideEntry.getValue(), side)) {
                            if (tile.hasLevel()) {//If we aren't already loaded yet don't do any updates
                                Direction direction = side.getDirection(tile.getDirection());
                                sideChangedBasic(type, direction);
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    public void collectImplicitComponents(DataComponentMap.Builder builder) {
        builder.set(MekanismDataComponents.SIDE_CONFIG, AttachedSideConfig.create(configInfo));
    }

    @Override
    public void deserialize(CompoundTag configNBT, HolderLookup.Provider provider) {
        read(configNBT, configInfo, (type, side) -> {
            if (tile.hasLevel()) {//If we aren't already loaded yet don't do any updates
                Direction direction = side.getDirection(tile.getDirection());
                sideChangedBasic(type, direction);
            }
        });
    }

    public static void read(CompoundTag configNBT, Map<TransmissionType, ConfigInfo> configInfo) {
        read(configNBT, configInfo, (type, side) -> {
        });
    }

    public static void read(CompoundTag configNBT, Map<TransmissionType, ConfigInfo> configInfo, BiConsumer<TransmissionType, RelativeSide> onChange) {
        //TODO -  1.22 remove backcompat - check for old ITEM ordinal, switch to legacy ordinals if found
        boolean isLegacyData = configNBT.contains(LEGACY_ITEM_CONFIG_KEY) || configNBT.contains(LEGACY_ITEM_EJECT_KEY);
        for (Entry<TransmissionType, ConfigInfo> entry : configInfo.entrySet()) {
            TransmissionType type = entry.getKey();
            ConfigInfo info = entry.getValue();
            int ordinalToUse = isLegacyData ? type.getLegacyOrdinal() : type.ordinal();
            NBTUtils.setBooleanIfPresent(configNBT, SerializationConstants.EJECT + ordinalToUse, info::setEjecting);
            String configKey = SerializationConstants.CONFIG + ordinalToUse;
            if (configNBT.contains(configKey, Tag.TAG_INT_ARRAY)) {
                readConfigSides(configNBT, onChange, configKey, info, type);
            } else if (isLegacyData && type == TransmissionType.CHEMICAL) {
                //fallback to try load other types in case a machine didn't have GAS
                for (int legacyOrdinal = TransmissionType.CHEMICAL.getLegacyOrdinal() + 1; legacyOrdinal < TransmissionType.ITEM.getLegacyOrdinal(); legacyOrdinal++) {
                    configKey = SerializationConstants.CONFIG + legacyOrdinal;
                    if (configNBT.contains(configKey, Tag.TAG_INT_ARRAY)) {
                        readConfigSides(configNBT, onChange, configKey, info, type);
                        break;
                    }
                }
            }
        }
    }

    private static void readConfigSides(CompoundTag configNBT, BiConsumer<TransmissionType, RelativeSide> onChange, String configKey, ConfigInfo info, TransmissionType type) {
        int[] sideData = configNBT.getIntArray(configKey);
        for (int i = 0; i < sideData.length && i < EnumUtils.SIDES.length; i++) {
            RelativeSide side = EnumUtils.SIDES[i];
            if (info.setDataType(DataType.BY_ID.apply(sideData[i]), side)) {
                onChange.accept(type, side);
            }
        }
    }

    @Override
    public CompoundTag serialize(HolderLookup.Provider provider) {
        return write(configInfo, true);
    }

    public static CompoundTag write(Map<TransmissionType, ? extends IPersistentConfigInfo> configInfo, boolean full) {
        CompoundTag configNBT = new CompoundTag();
        for (Entry<TransmissionType, ? extends IPersistentConfigInfo> entry : configInfo.entrySet()) {
            TransmissionType type = entry.getKey();
            IPersistentConfigInfo info = entry.getValue();
            if (full) {
                configNBT.putBoolean(SerializationConstants.EJECT + type.ordinal(), info.isEjecting());
            }
            int[] sideData = new int[EnumUtils.SIDES.length];
            for (int i = 0; i < EnumUtils.SIDES.length; i++) {
                sideData[i] = info.getDataType(EnumUtils.SIDES[i]).ordinal();
            }
            configNBT.putIntArray(SerializationConstants.CONFIG + type.ordinal(), sideData);
        }
        return configNBT;
    }

    /**
     * @implNote This is slightly different from read and write as we don't bother syncing the ejecting status. We can skip syncing the ejecting status as the client only
     * needs that information when in the gui see {@link #getSpecificSyncableData()} for where we sync ejecting status while in GUIs.
     */
    @Override
    public void addToUpdateTag(CompoundTag updateTag) {
        CompoundTag configNBT = write(configInfo, false);
        if (!configNBT.isEmpty()) {
            updateTag.put(getComponentKey(), configNBT);
        }
    }

    @Override
    public void readFromUpdateTag(CompoundTag updateTag) {
        NBTUtils.setCompoundIfPresent(updateTag, getComponentKey(), configNBT -> read(configNBT, configInfo));
    }

    @Override
    public List<ISyncableData> getSpecificSyncableData() {
        List<ISyncableData> list = new ArrayList<>();
        for (TransmissionType transmission : getTransmissions()) {
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
            case CHEMICAL -> new ChemicalSlotInfo(input, output, (List<IChemicalTank>) containers);
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
    boolean canEject(TransmissionType type) throws ComputerException {
        validateSupportedTransmissionType(type);
        return configInfo.get(type).canEject();
    }

    @ComputerMethod
    boolean isEjecting(TransmissionType type) throws ComputerException {
        validateSupportedTransmissionType(type);
        return configInfo.get(type).isEjecting();
    }

    @ComputerMethod(requiresPublicSecurity = true)
    void setEjecting(TransmissionType type, boolean ejecting) throws ComputerException {
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

    @ComputerMethod(requiresPublicSecurity = true)
    Set<DataType> getSupportedModes(TransmissionType type) throws ComputerException {
        validateSupportedTransmissionType(type);
        return configInfo.get(type).getSupportedDataTypes();
    }

    @ComputerMethod(requiresPublicSecurity = true)
    DataType getMode(TransmissionType type, RelativeSide side) throws ComputerException {
        validateSupportedTransmissionType(type);
        return configInfo.get(type).getDataType(side);
    }

    @ComputerMethod(requiresPublicSecurity = true)
    void setMode(TransmissionType type, RelativeSide side, DataType mode) throws ComputerException {
        tile.validateSecurityIsPublic();
        validateSupportedTransmissionType(type);
        ConfigInfo config = configInfo.get(type);
        if (!config.supports(mode)) {
            throw new ComputerException("This machine does not support mode '%s' for transmission type '%s'.", mode, type);
        }
        DataType currentMode = config.getDataType(side);
        if (mode != currentMode) {
            config.setDataType(mode, side);
            sideChanged(type, side);
        }
    }

    @ComputerMethod(requiresPublicSecurity = true)
    void incrementMode(TransmissionType type, RelativeSide side) throws ComputerException {
        tile.validateSecurityIsPublic();
        validateSupportedTransmissionType(type);
        ConfigInfo configInfo = this.configInfo.get(type);
        if (configInfo.getDataType(side) != configInfo.incrementDataType(side)) {
            sideChanged(type, side);
        }
    }

    @ComputerMethod(requiresPublicSecurity = true)
    void decrementMode(TransmissionType type, RelativeSide side) throws ComputerException {
        tile.validateSecurityIsPublic();
        validateSupportedTransmissionType(type);
        ConfigInfo configInfo = this.configInfo.get(type);
        if (configInfo.getDataType(side) != configInfo.decrementDataType(side)) {
            sideChanged(type, side);
        }
    }
    //End computer related methods
}
