package mekanism.common.content.entangloporter;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.NBTConstants;
import mekanism.api.chemical.ChemicalUtils;
import mekanism.api.chemical.IChemicalTank;
import mekanism.api.chemical.gas.BasicGasTank;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.gas.IMekanismGasHandler;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.energy.IMekanismStrictEnergyHandler;
import mekanism.api.fluid.IExtendedFluidTank;
import mekanism.api.fluid.IMekanismFluidHandler;
import mekanism.api.inventory.IInventorySlot;
import mekanism.api.inventory.IMekanismInventory;
import mekanism.common.capabilities.energy.BasicEnergyContainer;
import mekanism.common.capabilities.fluid.BasicFluidTank;
import mekanism.common.config.MekanismConfig;
import mekanism.common.frequency.Frequency;
import mekanism.common.frequency.FrequencyType;
import mekanism.common.inventory.slot.EntangloporterInventorySlot;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Direction;

public class InventoryFrequency extends Frequency implements IMekanismInventory, IMekanismGasHandler, IMekanismFluidHandler, IMekanismStrictEnergyHandler {

    public static final String ENTANGLOPORTER = "Entangloporter";

    public BasicFluidTank storedFluid;
    public BasicGasTank storedGas;
    public double temperature;
    private IInventorySlot storedItem;
    private IEnergyContainer storedEnergy;

    private List<IInventorySlot> inventorySlots;
    private List<? extends IChemicalTank<Gas, GasStack>> gasTanks;
    private List<IExtendedFluidTank> fluidTanks;
    private List<IEnergyContainer> energyContainers;

    public InventoryFrequency(String n, UUID uuid) {
        super(FrequencyType.INVENTORY, n, uuid);
        presetVariables();
    }

    public InventoryFrequency(CompoundNBT nbtTags, boolean fromUpdate) {
        super(FrequencyType.INVENTORY, nbtTags, fromUpdate);
    }

    public InventoryFrequency(PacketBuffer dataStream) {
        super(FrequencyType.INVENTORY, dataStream);
    }

    private void presetVariables() {
        storedFluid = BasicFluidTank.create(MekanismConfig.general.quantumEntangloporterFluidBuffer.get(), this);
        fluidTanks = Collections.singletonList(storedFluid);
        storedGas = BasicGasTank.create(MekanismConfig.general.quantumEntangloporterGasBuffer.get(), this);
        gasTanks = Collections.singletonList(storedGas);
        storedItem = EntangloporterInventorySlot.create(this);
        inventorySlots = Collections.singletonList(storedItem);
        storedEnergy = BasicEnergyContainer.create(MekanismConfig.general.quantumEntangloporterEnergyBuffer.get(), this);
        energyContainers = Collections.singletonList(storedEnergy);
    }

    @Override
    public void write(CompoundNBT nbtTags) {
        super.write(nbtTags);
        nbtTags.put(NBTConstants.ENERGY_STORED, storedEnergy.serializeNBT());
        nbtTags.put(NBTConstants.FLUID_STORED, storedFluid.serializeNBT());
        nbtTags.put(NBTConstants.GAS_STORED, storedGas.serializeNBT());
        nbtTags.put(NBTConstants.ITEM, storedItem.serializeNBT());
        nbtTags.putDouble(NBTConstants.TEMPERATURE, temperature);
    }

    @Override
    protected void read(CompoundNBT nbtTags) {
        super.read(nbtTags);
        presetVariables();
        storedEnergy.deserializeNBT(nbtTags.getCompound(NBTConstants.ENERGY_STORED));
        storedFluid.deserializeNBT(nbtTags.getCompound(NBTConstants.FLUID_STORED));
        storedGas.deserializeNBT(nbtTags.getCompound(NBTConstants.GAS_STORED));
        storedItem.deserializeNBT(nbtTags.getCompound(NBTConstants.ITEM));
        temperature = nbtTags.getDouble(NBTConstants.TEMPERATURE);
    }

    @Override
    public void write(PacketBuffer buffer) {
        super.write(buffer);
        buffer.writeDouble(storedEnergy.getEnergy());
        buffer.writeFluidStack(storedFluid.getFluid());
        ChemicalUtils.writeChemicalStack(buffer, storedGas.getStack());
        buffer.writeCompoundTag(storedItem.serializeNBT());
        buffer.writeDouble(temperature);
    }

    @Override
    protected void read(PacketBuffer dataStream) {
        super.read(dataStream);
        presetVariables();
        storedEnergy.setEnergy(dataStream.readDouble());
        storedFluid.setStack(dataStream.readFluidStack());
        storedGas.setStack(ChemicalUtils.readGasStack(dataStream));
        storedItem.deserializeNBT(dataStream.readCompoundTag());
        temperature = dataStream.readDouble();
    }

    @Nonnull
    @Override
    public List<IInventorySlot> getInventorySlots(@Nullable Direction side) {
        return inventorySlots;
    }

    @Nonnull
    @Override
    public List<? extends IChemicalTank<Gas, GasStack>> getGasTanks(@Nullable Direction side) {
        return gasTanks;
    }

    @Nonnull
    @Override
    public List<IExtendedFluidTank> getFluidTanks(@Nullable Direction side) {
        return fluidTanks;
    }

    @Override
    public List<IEnergyContainer> getEnergyContainers(@Nullable Direction side) {
        return energyContainers;
    }

    @Override
    public void onContentsChanged() {
    }
}