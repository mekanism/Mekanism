package mekanism.common.tile.transmitter;

import java.util.Collection;
import java.util.Optional;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.IHeatTransfer;
import mekanism.api.NBTConstants;
import mekanism.api.block.IHasTileEntity;
import mekanism.api.providers.IBlockProvider;
import mekanism.api.tier.AlloyTier;
import mekanism.api.tier.BaseTier;
import mekanism.api.transmitters.TransmissionType;
import mekanism.common.ColorRGBA;
import mekanism.common.block.states.BlockStateHelper;
import mekanism.common.block.states.TransmitterType;
import mekanism.common.block.transmitter.BlockThermodynamicConductor;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.tier.ConductorTier;
import mekanism.common.transmitters.grid.HeatNetwork;
import mekanism.common.upgrade.transmitter.ThermodynamicConductorUpgradeData;
import mekanism.common.upgrade.transmitter.TransmitterUpgradeData;
import mekanism.common.util.CapabilityUtils;
import mekanism.common.util.HeatUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.NBTUtils;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;

public class TileEntityThermodynamicConductor extends TileEntityTransmitter<IHeatTransfer, HeatNetwork, Void> implements IHeatTransfer {

    public final ConductorTier tier;

    public double temperature = 0;
    public double clientTemperature = 0;
    public double heatToAbsorb = 0;

    public TileEntityThermodynamicConductor(IBlockProvider blockProvider) {
        super(((IHasTileEntity<TileEntityThermodynamicConductor>) blockProvider.getBlock()).getTileType());
        this.tier = ((BlockThermodynamicConductor) blockProvider.getBlock()).getTier();
    }

    @Override
    public HeatNetwork createNewNetwork() {
        return new HeatNetwork();
    }

    @Override
    public HeatNetwork createNetworkByMerging(Collection<HeatNetwork> networks) {
        return new HeatNetwork(networks);
    }

    @Override
    public int getCapacity() {
        return 0;
    }

    @Override
    public Void getBuffer() {
        return null;
    }

    @Override
    public void takeShare() {
    }

    @Override
    public void updateShare() {
    }

    @Override
    public TransmitterType getTransmitterType() {
        return TransmitterType.THERMODYNAMIC_CONDUCTOR;
    }

    @Override
    public boolean isValidAcceptor(TileEntity tile, Direction side) {
        return CapabilityUtils.getCapability(tile, Capabilities.HEAT_TRANSFER_CAPABILITY, side.getOpposite()).isPresent();
    }

    @Override
    public TransmissionType getTransmissionType() {
        return TransmissionType.HEAT;
    }

    @Override
    public void read(CompoundNBT nbtTags) {
        super.read(nbtTags);
        NBTUtils.setDoubleIfPresent(nbtTags, NBTConstants.TEMPERATURE, temp -> temperature = temp);
    }

    @Nonnull
    @Override
    public CompoundNBT write(CompoundNBT nbtTags) {
        super.write(nbtTags);
        nbtTags.putDouble(NBTConstants.TEMPERATURE, temperature);
        return nbtTags;
    }

    @Override
    public IHeatTransfer getCachedAcceptor(Direction side) {
        return MekanismUtils.toOptional(CapabilityUtils.getCapability(getCachedTile(side), Capabilities.HEAT_TRANSFER_CAPABILITY, side.getOpposite())).orElse(null);
    }

    @Nonnull
    @Override
    public CompoundNBT getUpdateTag() {
        CompoundNBT updateTag = super.getUpdateTag();
        updateTag.putDouble(NBTConstants.TEMPERATURE, temperature);
        return updateTag;
    }

    @Override
    public void handleUpdateTag(@Nonnull CompoundNBT tag) {
        super.handleUpdateTag(tag);
        NBTUtils.setDoubleIfPresent(tag, NBTConstants.TEMPERATURE, temperature -> this.temperature = temperature);
    }

    public ColorRGBA getBaseColor() {
        return tier.getBaseColor();
    }

    @Override
    public double getTemp() {
        return temperature;
    }

    @Override
    public double getInverseConductionCoefficient() {
        return tier.getInverseConduction();
    }

    @Override
    public double getInsulationCoefficient(Direction side) {
        return tier.getInverseConductionInsulation();
    }

    @Override
    public void transferHeatTo(double heat) {
        heatToAbsorb += heat;
    }

    @Override
    public double[] simulateHeat() {
        return HeatUtils.simulate(this);
    }

    @Override
    public double applyTemperatureChange() {
        temperature += tier.getInverseHeatCapacity() * heatToAbsorb;
        heatToAbsorb = 0;
        if (Math.abs(temperature - clientTemperature) > (temperature / 20)) {
            clientTemperature = temperature;
            sendUpdatePacket();
        }
        return temperature;
    }

    @Nullable
    @Override
    public IHeatTransfer getAdjacent(Direction side) {
        if (connectionMapContainsSide(getAllCurrentConnections(), side)) {
            TileEntity adj = MekanismUtils.getTileEntity(getWorld(), getPos().offset(side));
            Optional<IHeatTransfer> capability = MekanismUtils.toOptional(CapabilityUtils.getCapability(adj, Capabilities.HEAT_TRANSFER_CAPABILITY, side.getOpposite()));
            if (capability.isPresent()) {
                return capability.get();
            }
        }
        return null;
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability, @Nullable Direction side) {
        if (capability == Capabilities.HEAT_TRANSFER_CAPABILITY) {
            return Capabilities.HEAT_TRANSFER_CAPABILITY.orEmpty(capability, LazyOptional.of(() -> this));
        }
        return super.getCapability(capability, side);
    }

    @Override
    protected boolean canUpgrade(AlloyTier alloyTier) {
        return alloyTier.getBaseTier().ordinal() == tier.getBaseTier().ordinal() + 1;
    }

    @Nonnull
    @Override
    protected BlockState upgradeResult(@Nonnull BlockState current, @Nonnull BaseTier tier) {
        switch (tier) {
            case BASIC:
                return BlockStateHelper.copyStateData(current, MekanismBlocks.BASIC_THERMODYNAMIC_CONDUCTOR.getBlock().getDefaultState());
            case ADVANCED:
                return BlockStateHelper.copyStateData(current, MekanismBlocks.ADVANCED_THERMODYNAMIC_CONDUCTOR.getBlock().getDefaultState());
            case ELITE:
                return BlockStateHelper.copyStateData(current, MekanismBlocks.ELITE_THERMODYNAMIC_CONDUCTOR.getBlock().getDefaultState());
            case ULTIMATE:
                return BlockStateHelper.copyStateData(current, MekanismBlocks.ULTIMATE_THERMODYNAMIC_CONDUCTOR.getBlock().getDefaultState());
        }
        return current;
    }

    @Nullable
    @Override
    protected ThermodynamicConductorUpgradeData getUpgradeData() {
        return new ThermodynamicConductorUpgradeData(redstoneReactive, connectionTypes, temperature);
    }

    @Override
    protected void parseUpgradeData(@Nonnull TransmitterUpgradeData upgradeData) {
        if (upgradeData instanceof ThermodynamicConductorUpgradeData) {
            ThermodynamicConductorUpgradeData data = (ThermodynamicConductorUpgradeData) upgradeData;
            redstoneReactive = data.redstoneReactive;
            connectionTypes = data.connectionTypes;
            temperature = data.temperature;
        } else {
            super.parseUpgradeData(upgradeData);
        }
    }
}