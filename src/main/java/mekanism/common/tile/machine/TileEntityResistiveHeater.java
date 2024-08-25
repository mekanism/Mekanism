package mekanism.common.tile.machine;

import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.IContentsListener;
import mekanism.api.RelativeSide;
import mekanism.api.SerializationConstants;
import mekanism.api.heat.HeatAPI.HeatTransfer;
import mekanism.common.capabilities.energy.MachineEnergyContainer;
import mekanism.common.capabilities.energy.ResistiveHeaterEnergyContainer;
import mekanism.common.capabilities.heat.BasicHeatCapacitor;
import mekanism.common.capabilities.heat.CachedAmbientTemperature;
import mekanism.common.capabilities.holder.energy.EnergyContainerHelper;
import mekanism.common.capabilities.holder.energy.IEnergyContainerHolder;
import mekanism.common.capabilities.holder.heat.HeatCapacitorHelper;
import mekanism.common.capabilities.holder.heat.IHeatCapacitorHolder;
import mekanism.common.capabilities.holder.slot.IInventorySlotHolder;
import mekanism.common.capabilities.holder.slot.InventorySlotHelper;
import mekanism.common.config.MekanismConfig;
import mekanism.common.integration.computer.ComputerException;
import mekanism.common.integration.computer.SpecialComputerMethodWrapper.ComputerHeatCapacitorWrapper;
import mekanism.common.integration.computer.SpecialComputerMethodWrapper.ComputerIInventorySlotWrapper;
import mekanism.common.integration.computer.annotation.ComputerMethod;
import mekanism.common.integration.computer.annotation.WrappingComputerMethod;
import mekanism.common.integration.computer.computercraft.ComputerConstants;
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.inventory.container.sync.SyncableDouble;
import mekanism.common.inventory.container.sync.SyncableLong;
import mekanism.common.inventory.slot.EnergyInventorySlot;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.registries.MekanismDataComponents;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.util.NBTUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

public class TileEntityResistiveHeater extends TileEntityMekanism {

    public static final double HEAT_CAPACITY = 100;
    public static final double INVERSE_CONDUCTION_COEFFICIENT = 5;
    public static final double INVERSE_INSULATION_COEFFICIENT = 10;
    //TODO: Eventually make this into a config at some point?
    public static final long BASE_USAGE = 100L;

    private float soundScale = 1;
    private double lastEnvironmentLoss;
    private double lastTransferLoss;
    private long clientEnergyUsed = 0;

    private ResistiveHeaterEnergyContainer energyContainer;
    @WrappingComputerMethod(wrapper = ComputerHeatCapacitorWrapper.class, methodNames = "getTemperature", docPlaceholder = "heater")
    BasicHeatCapacitor heatCapacitor;
    @WrappingComputerMethod(wrapper = ComputerIInventorySlotWrapper.class, methodNames = "getEnergyItem", docPlaceholder = "energy slot")
    EnergyInventorySlot energySlot;

    public TileEntityResistiveHeater(BlockPos pos, BlockState state) {
        super(MekanismBlocks.RESISTIVE_HEATER, pos, state);
    }

    @NotNull
    @Override
    protected IEnergyContainerHolder getInitialEnergyContainers(IContentsListener listener) {
        EnergyContainerHelper builder = EnergyContainerHelper.forSide(facingSupplier);
        builder.addContainer(energyContainer = ResistiveHeaterEnergyContainer.input(this, listener), RelativeSide.LEFT, RelativeSide.RIGHT);
        return builder.build();
    }

    @NotNull
    @Override
    protected IHeatCapacitorHolder getInitialHeatCapacitors(IContentsListener listener, CachedAmbientTemperature ambientTemperature) {
        HeatCapacitorHelper builder = HeatCapacitorHelper.forSide(facingSupplier);
        builder.addCapacitor(heatCapacitor = BasicHeatCapacitor.create(HEAT_CAPACITY, INVERSE_CONDUCTION_COEFFICIENT, INVERSE_INSULATION_COEFFICIENT, ambientTemperature, listener));
        return builder.build();
    }

    @NotNull
    @Override
    protected IInventorySlotHolder getInitialInventory(IContentsListener listener) {
        InventorySlotHelper builder = InventorySlotHelper.forSide(facingSupplier);
        builder.addSlot(energySlot = EnergyInventorySlot.fillOrConvert(energyContainer, this::getLevel, listener, 15, 35));
        return builder.build();
    }

    @Override
    protected boolean onUpdateServer() {
        boolean sendUpdatePacket = super.onUpdateServer();
        energySlot.fillContainerOrConvert();
        long toUse = 0;
        if (canFunction()) {
            toUse = energyContainer.extract(energyContainer.getEnergyPerTick(), Action.SIMULATE, AutomationType.INTERNAL);
            if (toUse > 0L) {
                heatCapacitor.handleHeat(toUse * MekanismConfig.general.resistiveHeaterEfficiency.get());
                energyContainer.extract(toUse, Action.EXECUTE, AutomationType.INTERNAL);
            }
        }
        setActive(toUse > 0L);
        clientEnergyUsed = toUse;
        HeatTransfer transfer = simulate();
        lastEnvironmentLoss = transfer.environmentTransfer();
        lastTransferLoss = transfer.adjacentTransfer();
        float newSoundScale = (float) (toUse / 100_000D);
        if (Math.abs(newSoundScale - soundScale) > 0.01) {
            soundScale = newSoundScale;
            sendUpdatePacket = true;
        }
        return sendUpdatePacket;
    }

    @ComputerMethod
    public long getEnergyUsed() {
        return clientEnergyUsed;
    }

    @ComputerMethod(nameOverride = "getTransferLoss")
    public double getLastTransferLoss() {
        return lastTransferLoss;
    }

    @ComputerMethod(nameOverride = "getEnvironmentalLoss")
    public double getLastEnvironmentLoss() {
        return lastEnvironmentLoss;
    }

    public void setEnergyUsageFromPacket(long floatingLong) {
        energyContainer.updateEnergyUsage(floatingLong);
        markForSave();
    }

    @Override
    public float getVolume() {
        return Mth.sqrt(soundScale);
    }

    public MachineEnergyContainer<TileEntityResistiveHeater> getEnergyContainer() {
        return energyContainer;
    }

    @Override
    public CompoundTag getConfigurationData(HolderLookup.Provider provider, Player player) {
        CompoundTag data = super.getConfigurationData(provider, player);
        data.putLong(SerializationConstants.ENERGY_USAGE, energyContainer.getEnergyPerTick());
        return data;
    }

    @Override
    public void setConfigurationData(HolderLookup.Provider provider, Player player, CompoundTag data) {
        super.setConfigurationData(provider, player, data);
        NBTUtils.setLegacyEnergyIfPresent(data, SerializationConstants.ENERGY_USAGE, energyContainer::updateEnergyUsage);
    }

    @Override
    public void addContainerTrackers(MekanismContainer container) {
        super.addContainerTrackers(container);
        container.track(SyncableDouble.create(this::getLastTransferLoss, value -> lastTransferLoss = value));
        container.track(SyncableDouble.create(this::getLastEnvironmentLoss, value -> lastEnvironmentLoss = value));
        container.track(SyncableLong.create(this::getEnergyUsed, value -> clientEnergyUsed = value));
    }

    @NotNull
    @Override
    public CompoundTag getReducedUpdateTag(@NotNull HolderLookup.Provider provider) {
        CompoundTag updateTag = super.getReducedUpdateTag(provider);
        updateTag.putFloat(SerializationConstants.SOUND_SCALE, soundScale);
        return updateTag;
    }

    @Override
    public void handleUpdateTag(@NotNull CompoundTag tag, @NotNull HolderLookup.Provider provider) {
        super.handleUpdateTag(tag, provider);
        NBTUtils.setFloatIfPresent(tag, SerializationConstants.SOUND_SCALE, value -> soundScale = value);
    }

    @Override
    protected void collectImplicitComponents(@NotNull DataComponentMap.Builder builder) {
        //Note: We copy the energy usage before handling super, in case it is necessary in order to set the proper value on the item
        builder.set(MekanismDataComponents.ENERGY_USAGE, energyContainer.getEnergyPerTick());
        super.collectImplicitComponents(builder);
    }

    @Override
    protected void applyImplicitComponents(@NotNull BlockEntity.DataComponentInput input) {
        //Apply the usage before processing the stored data as it changes the buffer of the energy container
        energyContainer.updateEnergyUsage(input.getOrDefault(MekanismDataComponents.ENERGY_USAGE, BASE_USAGE));
        super.applyImplicitComponents(input);
    }

    //Methods relating to IComputerTile
    @ComputerMethod(methodDescription = ComputerConstants.DESCRIPTION_GET_ENERGY_USAGE)
    long getEnergyUsage() {
        return energyContainer.getEnergyPerTick();
    }

    @ComputerMethod(requiresPublicSecurity = true)
    void setEnergyUsage(long usage) throws ComputerException {
        validateSecurityIsPublic();
        setEnergyUsageFromPacket(usage);
    }
    //End methods IComputerTile
}
