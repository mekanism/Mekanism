package mekanism.common.content.entangloporter;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.NBTConstants;
import mekanism.api.chemical.ChemicalUtils;
import mekanism.api.chemical.gas.BasicGasTank;
import mekanism.api.chemical.gas.IGasTank;
import mekanism.api.chemical.gas.IMekanismGasHandler;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.energy.IMekanismStrictEnergyHandler;
import mekanism.api.fluid.IExtendedFluidTank;
import mekanism.api.fluid.IMekanismFluidHandler;
import mekanism.api.heat.IHeatCapacitor;
import mekanism.api.inventory.IInventorySlot;
import mekanism.api.inventory.IMekanismInventory;
import mekanism.api.math.FloatingLong;
import mekanism.common.capabilities.energy.BasicEnergyContainer;
import mekanism.common.capabilities.fluid.BasicFluidTank;
import mekanism.common.capabilities.heat.BasicHeatCapacitor;
import mekanism.common.capabilities.heat.ITileHeatHandler;
import mekanism.common.config.MekanismConfig;
import mekanism.common.inventory.slot.EntangloporterInventorySlot;
import mekanism.common.lib.frequency.Frequency;
import mekanism.common.lib.frequency.FrequencyType;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Direction;

public class InventoryFrequency extends Frequency implements IMekanismInventory, IMekanismGasHandler, IMekanismFluidHandler, IMekanismStrictEnergyHandler, ITileHeatHandler {

    public BasicFluidTank storedFluid;
    public BasicGasTank storedGas;
    private IInventorySlot storedItem;
    public IEnergyContainer storedEnergy;
    public BasicHeatCapacitor storedHeat;

    private List<IInventorySlot> inventorySlots;
    private List<IGasTank> gasTanks;
    private List<IExtendedFluidTank> fluidTanks;
    private List<IEnergyContainer> energyContainers;
    private List<IHeatCapacitor> heatCapacitors;

    public InventoryFrequency(String n, UUID uuid) {
        super(FrequencyType.INVENTORY, n, uuid);
        presetVariables();
    }

    public InventoryFrequency() {
        super(FrequencyType.INVENTORY);
    }

    private void presetVariables() {
        storedFluid = BasicFluidTank.create(MekanismConfig.general.entangloporterFluidBuffer.get(), this);
        fluidTanks = Collections.singletonList(storedFluid);
        storedGas = BasicGasTank.create(MekanismConfig.general.entangloporterGasBuffer.get(), this);
        gasTanks = Collections.singletonList(storedGas);
        storedItem = EntangloporterInventorySlot.create(this);
        inventorySlots = Collections.singletonList(storedItem);
        storedEnergy = BasicEnergyContainer.create(MekanismConfig.general.entangloporterEnergyBuffer.get(), this);
        energyContainers = Collections.singletonList(storedEnergy);
        storedHeat = BasicHeatCapacitor.create(1, 1, 1_000, this);
        heatCapacitors = Collections.singletonList(storedHeat);
    }

    @Override
    public void write(CompoundNBT nbtTags) {
        super.write(nbtTags);
        nbtTags.put(NBTConstants.ENERGY_STORED, storedEnergy.serializeNBT());
        nbtTags.put(NBTConstants.FLUID_STORED, storedFluid.serializeNBT());
        nbtTags.put(NBTConstants.GAS_STORED, storedGas.serializeNBT());
        nbtTags.put(NBTConstants.ITEM, storedItem.serializeNBT());
        nbtTags.put(NBTConstants.HEAT_STORED, storedHeat.serializeNBT());
    }

    @Override
    protected void read(CompoundNBT nbtTags) {
        super.read(nbtTags);
        presetVariables();
        storedEnergy.deserializeNBT(nbtTags.getCompound(NBTConstants.ENERGY_STORED));
        storedFluid.deserializeNBT(nbtTags.getCompound(NBTConstants.FLUID_STORED));
        storedGas.deserializeNBT(nbtTags.getCompound(NBTConstants.GAS_STORED));
        storedItem.deserializeNBT(nbtTags.getCompound(NBTConstants.ITEM));
        storedHeat.deserializeNBT(nbtTags.getCompound(NBTConstants.HEAT_STORED));
    }

    @Override
    public void write(PacketBuffer buffer) {
        super.write(buffer);
        storedEnergy.getEnergy().writeToBuffer(buffer);
        buffer.writeFluidStack(storedFluid.getFluid());
        ChemicalUtils.writeChemicalStack(buffer, storedGas.getStack());
        buffer.writeCompoundTag(storedItem.serializeNBT());
        buffer.writeDouble(storedHeat.getHeat());
    }

    @Override
    protected void read(PacketBuffer dataStream) {
        super.read(dataStream);
        presetVariables();
        storedEnergy.setEnergy(FloatingLong.readFromBuffer(dataStream));
        storedFluid.setStack(dataStream.readFluidStack());
        storedGas.setStack(ChemicalUtils.readGasStack(dataStream));
        storedItem.deserializeNBT(dataStream.readCompoundTag());
        storedHeat.setHeat(dataStream.readDouble());
    }

    @Nonnull
    @Override
    public List<IInventorySlot> getInventorySlots(@Nullable Direction side) {
        return inventorySlots;
    }

    @Nonnull
    @Override
    public List<IGasTank> getGasTanks(@Nullable Direction side) {
        return gasTanks;
    }

    @Nonnull
    @Override
    public List<IExtendedFluidTank> getFluidTanks(@Nullable Direction side) {
        return fluidTanks;
    }

    @Nonnull
    @Override
    public List<IEnergyContainer> getEnergyContainers(@Nullable Direction side) {
        return energyContainers;
    }

    @Nonnull
    @Override
    public List<IHeatCapacitor> getHeatCapacitors(@Nullable Direction side) {
        return heatCapacitors;
    }

    @Override
    public void onContentsChanged() {}
}