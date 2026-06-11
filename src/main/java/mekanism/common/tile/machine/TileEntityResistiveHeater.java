package mekanism.common.tile.machine;

import java.util.Set;
import mekanism.api.AutomationType;
import mekanism.api.IContentsListener;
import mekanism.api.RelativeSide;
import mekanism.api.SerializationConstants;
import mekanism.api.heat.HeatAPI.HeatTransfer;
import mekanism.api.heat.IHeatCapacitor;
import mekanism.api.inventory.IInventorySlot;
import mekanism.common.capabilities.energy.MachineEnergyContainer;
import mekanism.common.capabilities.energy.ResistiveHeaterEnergyContainer;
import mekanism.common.capabilities.heat.BasicHeatCapacitor;
import mekanism.common.capabilities.heat.CachedAmbientTemperature;
import mekanism.common.capabilities.holder.container.IContainerHolder;
import mekanism.common.capabilities.holder.container.MekContainerHelper;
import mekanism.common.capabilities.holder.energy.BasicEnergyHolder;
import mekanism.common.capabilities.holder.energy.IEnergyContainerHolder;
import mekanism.common.config.MekanismConfig;
import mekanism.common.integration.computer.ComputerException;
import mekanism.common.integration.computer.SpecialComputerMethodWrapper.ComputerHeatCapacitorWrapper;
import mekanism.common.integration.computer.SpecialComputerMethodWrapper.ComputerIInventorySlotWrapper;
import mekanism.common.integration.computer.annotation.ComputerMethod;
import mekanism.common.integration.computer.annotation.WrappingComputerMethod;
import mekanism.common.integration.computer.computercraft.ComputerConstants;
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.inventory.container.sync.SyncableDouble;
import mekanism.common.inventory.container.sync.SyncableInt;
import mekanism.common.inventory.slot.EnergyInventorySlot;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.registries.MekanismDataComponents;
import mekanism.common.tile.base.TileEntityMekanism;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import org.jetbrains.annotations.UnknownNullability;

public class TileEntityResistiveHeater extends TileEntityMekanism {

    public static final double HEAT_CAPACITY = 100;
    public static final double INVERSE_CONDUCTION_COEFFICIENT = 5;
    public static final double INVERSE_INSULATION_COEFFICIENT = 10;
    //TODO: Eventually make this into a config at some point?
    public static final int BASE_USAGE = 100;

    private float soundScale = 1;
    private double lastEnvironmentLoss;
    private double lastTransferLoss;
    private int clientEnergyUsed = 0;

    @UnknownNullability//Initialized via getInitialEnergyContainer
    private ResistiveHeaterEnergyContainer energyContainer;
    @UnknownNullability//Initialized via getInitialHeatCapacitors
    @WrappingComputerMethod(wrapper = ComputerHeatCapacitorWrapper.class, methodNames = "getTemperature", docPlaceholder = "heater")
    BasicHeatCapacitor heatCapacitor;
    @UnknownNullability//Initialized via getInitialInventory
    @WrappingComputerMethod(wrapper = ComputerIInventorySlotWrapper.class, methodNames = "getEnergyItem", docPlaceholder = "energy slot")
    EnergyInventorySlot energySlot;

    public TileEntityResistiveHeater(BlockPos pos, BlockState state) {
        super(MekanismBlocks.RESISTIVE_HEATER, pos, state);
    }

    @Override
    protected IEnergyContainerHolder getInitialEnergyContainer(IContentsListener listener) {
        energyContainer = ResistiveHeaterEnergyContainer.input(this, listener);
        return new BasicEnergyHolder(energyContainer, facingSupplier, Set.of(RelativeSide.LEFT, RelativeSide.RIGHT));
    }

    @Override
    protected IContainerHolder<IHeatCapacitor> getInitialHeatCapacitors(IContentsListener listener, CachedAmbientTemperature ambientTemperature) {
        MekContainerHelper<IHeatCapacitor> builder = MekContainerHelper.forSide(facingSupplier);
        builder.addContainer(heatCapacitor = BasicHeatCapacitor.create(HEAT_CAPACITY, INVERSE_CONDUCTION_COEFFICIENT, INVERSE_INSULATION_COEFFICIENT, ambientTemperature, listener));
        return builder.build();
    }

    @Override
    protected IContainerHolder<IInventorySlot> getInitialInventory(IContentsListener listener) {
        MekContainerHelper<IInventorySlot> builder = MekContainerHelper.forSide(facingSupplier);
        builder.addContainer(energySlot = EnergyInventorySlot.fillOrConvert(energyContainer, this::getLevel, listener, 15, 35));
        return builder.build();
    }

    @Override
    protected boolean onUpdateServer(ServerLevel level) {
        boolean sendUpdatePacket = super.onUpdateServer(level);
        energySlot.fillContainerOrConvert(null);
        int toUse = 0;
        if (canFunction()) {
            try (Transaction transaction = Transaction.openRoot()) {
                toUse = energyContainer.extract(energyContainer.getEnergyPerTick(), transaction, AutomationType.INTERNAL);
                if (toUse > 0) {
                    heatCapacitor.handleHeat(toUse * MekanismConfig.general.resistiveHeaterEfficiency.get());
                    transaction.commit();
                }
            }
        }
        setActive(toUse > 0);
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
    public int getEnergyUsed() {
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

    public void setEnergyUsageFromPacket(int usage) {
        energyContainer.updateEnergyUsage(usage);
        markForSave();
    }

    @Override
    public float getVolume() {
        return Mth.sqrt(soundScale);
    }

    public MachineEnergyContainer<TileEntityResistiveHeater> energyContainer() {
        return energyContainer;
    }

    @Override
    public void writeConfigurationData(ValueOutput output, Player player) {
        super.writeConfigurationData(output, player);
        output.putLong(SerializationConstants.ENERGY_USAGE, energyContainer.getEnergyPerTick());
    }

    @Override
    public void setConfigurationData(ValueInput input, Player player) {
        super.setConfigurationData(input, player);
        input.getInt(SerializationConstants.ENERGY_USAGE).ifPresent(energyContainer::updateEnergyUsage);
    }

    @Override
    public void addContainerTrackers(MekanismContainer container) {
        super.addContainerTrackers(container);
        container.track(SyncableDouble.create(this::getLastTransferLoss, value -> lastTransferLoss = value));
        container.track(SyncableDouble.create(this::getLastEnvironmentLoss, value -> lastEnvironmentLoss = value));
        container.track(SyncableInt.create(this::getEnergyUsed, value -> clientEnergyUsed = value));
    }

    @Override
    public void writeReducedUpdatedTag(ValueOutput output) {
        super.writeReducedUpdatedTag(output);
        output.putFloat(SerializationConstants.SOUND_SCALE, soundScale);
    }

    @Override
    public void handleUpdateTag(ValueInput input) {
        super.handleUpdateTag(input);
        soundScale = input.getFloatOr(SerializationConstants.SOUND_SCALE, soundScale);
    }

    @Override
    protected void collectImplicitComponents(DataComponentMap.Builder builder) {
        //Note: We copy the energy usage before handling super, in case it is necessary in order to set the proper value on the item
        builder.set(MekanismDataComponents.ENERGY_USAGE, energyContainer.getEnergyPerTick());
        super.collectImplicitComponents(builder);
    }

    @Override
    protected void applyImplicitComponents(DataComponentGetter input) {
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
    void setEnergyUsage(int usage) throws ComputerException {
        validateSecurityIsPublic();
        setEnergyUsageFromPacket(usage);
    }
    //End methods IComputerTile
}
