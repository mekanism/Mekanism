package mekanism.common.content.entangloporter;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.TileNetworkList;
import mekanism.api.chemical.IChemicalTank;
import mekanism.api.gas.BasicGasTank;
import mekanism.api.gas.Gas;
import mekanism.api.gas.GasStack;
import mekanism.api.gas.IMekanismGasHandler;
import mekanism.api.inventory.IMekanismInventory;
import mekanism.api.inventory.IInventorySlot;
import mekanism.common.PacketHandler;
import mekanism.common.config.MekanismConfig;
import mekanism.common.frequency.Frequency;
import mekanism.common.inventory.slot.EntangloporterInventorySlot;
import mekanism.common.tier.FluidTankTier;
import mekanism.common.tier.GasTankTier;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Direction;
import net.minecraftforge.fluids.capability.templates.FluidTank;

public class InventoryFrequency extends Frequency implements IMekanismInventory, IMekanismGasHandler {

    public static final String ENTANGLOPORTER = "Entangloporter";
    private static final Supplier<FluidTank> FLUID_TANK_SUPPLIER = () -> new FluidTank(MekanismConfig.general.quantumEntangloporterFluidBuffer.get());
    private final Supplier<BasicGasTank> GAS_TANK_SUPPLIER = () -> BasicGasTank.create(MekanismConfig.general.quantumEntangloporterGasBuffer.get(), this);

    public double storedEnergy;
    public FluidTank storedFluid;
    public BasicGasTank storedGas;
    public double temperature;
    private IInventorySlot storedItem;

    public List<IInventorySlot> inventorySlots;

    public InventoryFrequency(String n, UUID uuid) {
        super(n, uuid);
        storedFluid = FLUID_TANK_SUPPLIER.get();
        storedGas = GAS_TANK_SUPPLIER.get();
        storedItem = EntangloporterInventorySlot.create(this);
        inventorySlots = Collections.singletonList(storedItem);
    }

    public InventoryFrequency(CompoundNBT nbtTags) {
        super(nbtTags);
    }

    public InventoryFrequency(PacketBuffer dataStream) {
        super(dataStream);
    }

    @Override
    public void write(CompoundNBT nbtTags) {
        super.write(nbtTags);
        nbtTags.putDouble("storedEnergy", storedEnergy);
        if (!storedFluid.isEmpty()) {
            nbtTags.put("storedFluid", storedFluid.writeToNBT(new CompoundNBT()));
        }
        if (!storedGas.isEmpty()) {
            nbtTags.put("storedGas", storedGas.write(new CompoundNBT()));
        }
        nbtTags.put("storedItem", storedItem.serializeNBT());
        nbtTags.putDouble("temperature", temperature);
    }

    @Override
    protected void read(CompoundNBT nbtTags) {
        super.read(nbtTags);
        storedFluid = FLUID_TANK_SUPPLIER.get();
        storedGas = GAS_TANK_SUPPLIER.get();
        storedEnergy = nbtTags.getDouble("storedEnergy");
        storedItem = EntangloporterInventorySlot.create(this);
        inventorySlots = Collections.singletonList(storedItem);

        if (nbtTags.contains("storedFluid")) {
            storedFluid.readFromNBT(nbtTags.getCompound("storedFluid"));
        }
        if (nbtTags.contains("storedGas")) {
            storedGas.deserializeNBT(nbtTags.getCompound("storedGas"));
            storedGas.setCapacity(MekanismConfig.general.quantumEntangloporterGasBuffer.get());
        }

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
        storedItem = EntangloporterInventorySlot.create(this);
        inventorySlots = Collections.singletonList(storedItem);
        storedFluid = new FluidTank(FluidTankTier.ULTIMATE.getOutput());
        storedGas = new BasicGasTank(GasTankTier.ULTIMATE.getOutput());
        storedEnergy = dataStream.readDouble();
        storedFluid.setFluid(dataStream.readFluidStack());
        storedGas.setStack(PacketHandler.readGasStack(dataStream));
        storedItem.deserializeNBT(dataStream.readCompoundTag());
        temperature = dataStream.readDouble();
    }

    @Override
    @Nonnull
    public List<IInventorySlot> getInventorySlots(@Nullable Direction side) {
        return inventorySlots;
    }

    @Override
    public void onContentsChanged() {
    }
}