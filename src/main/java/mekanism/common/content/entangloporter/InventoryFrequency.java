package mekanism.common.content.entangloporter;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.TileNetworkList;
import mekanism.api.chemical.ChemicalUtils;
import mekanism.api.chemical.IChemicalTank;
import mekanism.api.gas.BasicGasTank;
import mekanism.api.gas.Gas;
import mekanism.api.gas.GasStack;
import mekanism.api.gas.IMekanismGasHandler;
import mekanism.api.inventory.IInventorySlot;
import mekanism.api.inventory.IMekanismInventory;
import mekanism.common.config.MekanismConfig;
import mekanism.common.frequency.Frequency;
import mekanism.common.inventory.slot.EntangloporterInventorySlot;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Direction;
import net.minecraftforge.fluids.capability.templates.FluidTank;

public class InventoryFrequency extends Frequency implements IMekanismInventory, IMekanismGasHandler {

    public static final String ENTANGLOPORTER = "Entangloporter";

    public double storedEnergy;
    public FluidTank storedFluid;
    public BasicGasTank storedGas;
    public double temperature;
    private IInventorySlot storedItem;

    public List<IInventorySlot> inventorySlots;
    public List<? extends IChemicalTank<Gas, GasStack>> gasTanks;

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
        storedFluid = new FluidTank(MekanismConfig.general.quantumEntangloporterFluidBuffer.get());
        storedGas = BasicGasTank.create(MekanismConfig.general.quantumEntangloporterGasBuffer.get(), this);
        gasTanks = Collections.singletonList(storedGas);
        storedItem = EntangloporterInventorySlot.create(this);
        inventorySlots = Collections.singletonList(storedItem);
    }

    @Override
    public void write(CompoundNBT nbtTags) {
        super.write(nbtTags);
        nbtTags.putDouble("storedEnergy", storedEnergy);
        if (!storedFluid.isEmpty()) {
            nbtTags.put("storedFluid", storedFluid.writeToNBT(new CompoundNBT()));
        }
        nbtTags.put("storedGas", storedGas.serializeNBT());
        nbtTags.put("storedItem", storedItem.serializeNBT());
        nbtTags.putDouble("temperature", temperature);
    }

    @Override
    protected void read(CompoundNBT nbtTags) {
        super.read(nbtTags);
        presetVariables();
        storedEnergy = nbtTags.getDouble("storedEnergy");
        if (nbtTags.contains("storedFluid")) {
            storedFluid.readFromNBT(nbtTags.getCompound("storedFluid"));
        }
        storedGas.deserializeNBT(nbtTags.getCompound("storedGas"));
        storedItem.deserializeNBT(nbtTags.getCompound("storedItem"));
        temperature = nbtTags.getDouble("temperature");
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
        storedFluid.setFluid(dataStream.readFluidStack());
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

    @Override
    public void onContentsChanged() {
    }
}