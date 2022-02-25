package mekanism.common.tile.machine;

import javax.annotation.Nonnull;
import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.IContentsListener;
import mekanism.api.NBTConstants;
import mekanism.api.RelativeSide;
import mekanism.api.heat.HeatAPI.HeatTransfer;
import mekanism.api.math.FloatingLong;
import mekanism.common.capabilities.Capabilities;
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
import mekanism.common.capabilities.resolver.BasicCapabilityResolver;
import mekanism.common.config.MekanismConfig;
import mekanism.common.integration.computer.ComputerException;
import mekanism.common.integration.computer.SpecialComputerMethodWrapper.ComputerHeatCapacitorWrapper;
import mekanism.common.integration.computer.SpecialComputerMethodWrapper.ComputerIInventorySlotWrapper;
import mekanism.common.integration.computer.annotation.ComputerMethod;
import mekanism.common.integration.computer.annotation.WrappingComputerMethod;
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.inventory.container.sync.SyncableDouble;
import mekanism.common.inventory.slot.EnergyInventorySlot;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.NBTUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;

public class TileEntityResistiveHeater extends TileEntityMekanism {

    private float soundScale = 1;
    private double lastEnvironmentLoss;

    private ResistiveHeaterEnergyContainer energyContainer;
    @WrappingComputerMethod(wrapper = ComputerHeatCapacitorWrapper.class, methodNames = "getTemperature")
    private BasicHeatCapacitor heatCapacitor;
    @WrappingComputerMethod(wrapper = ComputerIInventorySlotWrapper.class, methodNames = "getEnergyItem")
    private EnergyInventorySlot energySlot;

    public TileEntityResistiveHeater(BlockPos pos, BlockState state) {
        super(MekanismBlocks.RESISTIVE_HEATER, pos, state);
        addCapabilityResolver(BasicCapabilityResolver.constant(Capabilities.CONFIG_CARD_CAPABILITY, this));
    }

    @Nonnull
    @Override
    protected IEnergyContainerHolder getInitialEnergyContainers(IContentsListener listener) {
        EnergyContainerHelper builder = EnergyContainerHelper.forSide(this::getDirection);
        builder.addContainer(energyContainer = ResistiveHeaterEnergyContainer.input(this, listener), RelativeSide.LEFT, RelativeSide.RIGHT);
        return builder.build();
    }

    @Nonnull
    @Override
    protected IHeatCapacitorHolder getInitialHeatCapacitors(IContentsListener listener, CachedAmbientTemperature ambientTemperature) {
        HeatCapacitorHelper builder = HeatCapacitorHelper.forSide(this::getDirection);
        builder.addCapacitor(heatCapacitor = BasicHeatCapacitor.create(100, 5, 100, ambientTemperature, listener));
        return builder.build();
    }

    @Nonnull
    @Override
    protected IInventorySlotHolder getInitialInventory(IContentsListener listener) {
        InventorySlotHelper builder = InventorySlotHelper.forSide(this::getDirection);
        builder.addSlot(energySlot = EnergyInventorySlot.fillOrConvert(energyContainer, this::getLevel, listener, 15, 35));
        return builder.build();
    }

    @Override
    protected void onUpdateServer() {
        super.onUpdateServer();
        energySlot.fillContainerOrConvert();
        FloatingLong toUse = FloatingLong.ZERO;
        if (MekanismUtils.canFunction(this)) {
            toUse = energyContainer.extract(energyContainer.getEnergyPerTick(), Action.SIMULATE, AutomationType.INTERNAL);
            if (!toUse.isZero()) {
                heatCapacitor.handleHeat(toUse.multiply(MekanismConfig.general.resistiveHeaterEfficiency.get()).doubleValue());
                energyContainer.extract(toUse, Action.EXECUTE, AutomationType.INTERNAL);
            }
        }
        setActive(!toUse.isZero());
        HeatTransfer transfer = simulate();
        lastEnvironmentLoss = transfer.environmentTransfer();
        float newSoundScale = toUse.divide(100_000).floatValue();
        if (Math.abs(newSoundScale - soundScale) > 0.01) {
            soundScale = newSoundScale;
            sendUpdatePacket();
        }
    }

    @ComputerMethod(nameOverride = "getEnvironmentalLoss")
    public double getLastEnvironmentLoss() {
        return lastEnvironmentLoss;
    }

    public void setEnergyUsageFromPacket(FloatingLong floatingLong) {
        energyContainer.updateEnergyUsage(floatingLong);
        markForSave();
    }

    @Override
    public float getVolume() {
        return (float) Math.sqrt(soundScale);
    }

    public MachineEnergyContainer<TileEntityResistiveHeater> getEnergyContainer() {
        return energyContainer;
    }

    @Override
    public CompoundTag getConfigurationData(Player player) {
        CompoundTag data = super.getConfigurationData(player);
        data.putString(NBTConstants.ENERGY_USAGE, energyContainer.getEnergyPerTick().toString());
        return data;
    }

    @Override
    public void setConfigurationData(Player player, CompoundTag data) {
        super.setConfigurationData(player, data);
        NBTUtils.setFloatingLongIfPresent(data, NBTConstants.ENERGY_USAGE, energyContainer::updateEnergyUsage);
    }

    @Override
    public void addContainerTrackers(MekanismContainer container) {
        super.addContainerTrackers(container);
        container.track(SyncableDouble.create(this::getLastEnvironmentLoss, value -> lastEnvironmentLoss = value));
    }

    @Nonnull
    @Override
    public CompoundTag getReducedUpdateTag() {
        CompoundTag updateTag = super.getReducedUpdateTag();
        updateTag.putFloat(NBTConstants.SOUND_SCALE, soundScale);
        return updateTag;
    }

    @Override
    public void handleUpdateTag(@Nonnull CompoundTag tag) {
        super.handleUpdateTag(tag);
        NBTUtils.setFloatIfPresent(tag, NBTConstants.SOUND_SCALE, value -> soundScale = value);
    }

    //Methods relating to IComputerTile
    @ComputerMethod
    private FloatingLong getEnergyUsage() {
        return energyContainer.getEnergyPerTick();
    }

    @ComputerMethod
    private void setEnergyUsage(FloatingLong usage) throws ComputerException {
        validateSecurityIsPublic();
        setEnergyUsageFromPacket(usage);
    }
    //End methods IComputerTile
}