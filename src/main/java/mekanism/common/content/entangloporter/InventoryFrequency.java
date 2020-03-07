package mekanism.common.content.entangloporter;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.NBTConstants;
import mekanism.api.TileNetworkList;
import mekanism.api.chemical.ChemicalUtils;
import mekanism.api.chemical.IChemicalTank;
import mekanism.api.chemical.gas.BasicGasTank;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.gas.IMekanismGasHandler;
import mekanism.api.fluid.IExtendedFluidTank;
import mekanism.api.fluid.IMekanismFluidHandler;
import mekanism.api.inventory.IInventorySlot;
import mekanism.api.inventory.IMekanismInventory;
import mekanism.common.capabilities.fluid.BasicFluidTank;
import mekanism.common.config.MekanismConfig;
import mekanism.common.frequency.Frequency;
import mekanism.common.inventory.slot.EntangloporterInventorySlot;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Direction;

public class InventoryFrequency extends Frequency implements IMekanismInventory, IMekanismGasHandler, IMekanismFluidHandler {

    public static final String ENTANGLOPORTER = "Entangloporter";

    public double storedEnergy;
    public BasicFluidTank storedFluid;
    public BasicGasTank storedGas;
    public double temperature;
    private IInventorySlot storedItem;

    public List<IInventorySlot> inventorySlots;
    public List<? extends IChemicalTank<Gas, GasStack>> gasTanks;
    public List<IExtendedFluidTank> fluidTanks;

    public InventoryFrequency(String n, UUID uuid) {
        super(n, uuid);
        presetVariables();
    }

    public InventoryFrequency(CompoundNBT nbtTags) {
        super(nbtTags);
    }

    public InventoryFrequency(PacketBuffer dataStream) {
        super(dataStream);
    }

    private void presetVariables() {
        storedFluid = BasicFluidTank.create(MekanismConfig.general.quantumEntangloporterFluidBuffer.get(), this);
        fluidTanks = Collections.singletonList(storedFluid);
        storedGas = BasicGasTank.create(MekanismConfig.general.quantumEntangloporterGasBuffer.get(), this);
        gasTanks = Collections.singletonList(storedGas);
        storedItem = EntangloporterInventorySlot.create(this);
        inventorySlots = Collections.singletonList(storedItem);
    }

    @Override
    public void write(CompoundNBT nbtTags) {
        super.write(nbtTags);
        nbtTags.putDouble(NBTConstants.ENERGY_STORED, storedEnergy);
        nbtTags.put(NBTConstants.FLUID_STORED, storedFluid.serializeNBT());
        nbtTags.put(NBTConstants.GAS_STORED, storedGas.serializeNBT());
        nbtTags.put(NBTConstants.ITEM, storedItem.serializeNBT());
        nbtTags.putDouble(NBTConstants.TEMPERATURE, temperature);
    }

    @Override
    protected void read(CompoundNBT nbtTags) {
        super.read(nbtTags);
        presetVariables();
        storedEnergy = nbtTags.getDouble(NBTConstants.ENERGY_STORED);
        storedFluid.deserializeNBT(nbtTags.getCompound(NBTConstants.FLUID_STORED));
        storedGas.deserializeNBT(nbtTags.getCompound(NBTConstants.GAS_STORED));
        storedItem.deserializeNBT(nbtTags.getCompound(NBTConstants.ITEM));
        temperature = nbtTags.getDouble(NBTConstants.TEMPERATURE);
    }

    @Override
    public void write(TileNetworkList data) {
        super.write(data);
        data.add(storedEnergy);
        data.add(storedFluid.getFluid());
        data.add(storedGas.getStack());
        data.add(storedItem.serializeNBT());
        data.add(temperature);
    }

    @Override
    protected void read(PacketBuffer dataStream) {
        super.read(dataStream);
        presetVariables();
        storedEnergy = dataStream.readDouble();
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
    public void onContentsChanged() {
    }
}